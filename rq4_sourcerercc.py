"""
Docstring for rq4_sourcerercc
This module is responsible for assigning SourcererCC similarity scores to the patches in RQ4.
Final.
Reads rq4/patches_metadata and compares each pare with existing tool_patches.
"""
import os
import logging
import re
import json
import ollama
import time
import pandas as pd
from utils.config import *
from utils.benchmark import *
from utils.utils import *
from utils.tool import *
from utils.dataset import *
import argparse
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from rq4_llms import *


def report_data(df):
    print(f"Correct Tool Patches: {len(df[df['correctness'] == 'Correct'])}, Overfitting: {len(df[df['correctness'] == 'Overfitting'])}")



def assign_sourcerercc_labels(tool_patches, selected_tool_patches):
    """
    Assigns SourcererCC clone labels to selected patches by comparing against
    all patches with matching bug_uid in tool_patches.
    Args:
        tool_patches: DataFrame with all tool patches
        selected_tool_patches: DataFrame with selected patches to label
    Returns:
        DataFrame with columns: 'uid', 'sourcerercc_label' (True/False)
    """
    logging.info("Assigning SourcererCC labels to selected patches...")
    
    # Ensure content is available
    if 'content' not in tool_patches.columns:
        tool_patches['content'] = tool_patches.apply(get_single_hunk_method, axis=1)
    if 'content' not in selected_tool_patches.columns:
        selected_tool_patches['content'] = selected_tool_patches.apply(get_single_hunk_method, axis=1)
    
    # Remove duplicate content within each bug_uid group from tool_patches
    original_count = len(tool_patches)
    tool_patches = tool_patches.drop_duplicates(subset=['bug_uid', 'content'], keep='first')
    dedupe_count = len(tool_patches)
    logging.info(f"Deduplicated tool_patches (REMOVE SAME BUG_UID CONTENT TO REDUCE COMPARISON SIZE): {original_count} -> {dedupe_count} (removed {original_count - dedupe_count} duplicates)")
    print(f"Deduplicated tool_patches (REMOVE SAME BUG_UID CONTENT TO REDUCE COMPARISON SIZE): {original_count} -> {dedupe_count} (removed {original_count - dedupe_count} duplicates)")

    # Pre-calculate all comparisons to get accurate total
    comparisons = []
    for _, row in selected_tool_patches.iterrows():
        uid = row.name
        bug_uid = row['bug_uid']
        selected_content = row['content']
        
        matching_patches = tool_patches[tool_patches['bug_uid'] == bug_uid]
        # if len(matching_patches) > 10:
        #     matching_patches = matching_patches.head(10)
        
        for _, match_row in matching_patches.iterrows():
            comparisons.append({
                'uid': uid,
                'selected_content': selected_content,
                'match_uid': match_row.name,
                'match_content': match_row['content']
            })
    
    total_comparisons = len(comparisons)
    logging.info(f"Total comparisons to perform: {total_comparisons}")
    
    # Track results per uid
    uid_results = {row.name: False for _, row in selected_tool_patches.iterrows()}
    
    # Perform comparisons with accurate progress bar
    for comp in tqdm(comparisons, total=total_comparisons, desc="Detecting SourcererCC clones"):
        uid = comp['uid']
        
        # Skip if already found a clone for this uid
        if uid_results[uid]:
            continue
        
        label = sourcerercc_are_clones(comp['selected_content'], comp['match_content'])
        logging.info(f"Comparing UID {uid} with matching patch UID {comp['match_uid']}: Clone={label}")
        
        if label:
            uid_results[uid] = True
    
    # Build result DataFrame
    results = [{'uid': uid, 'sourcerercc_label': is_clone} for uid, is_clone in uid_results.items()]
    result_df = pd.DataFrame(results)
    
    logging.info(f"Completed! Processed {len(result_df)} patches with {total_comparisons} comparisons.")
    return result_df

def get_selected_tool_patches(selected_tool_name):
    bugs, developer_patches, tool_patches = init(configure=False)

    selected_tool_patches = pd.read_pickle(os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_patches.pkl"))
    print(f"READ: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = clean_and_save_patches(bugs, selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_patches_cleaned.pkl"))
    print(f"CLEANED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = get_methods_and_save(bugs, selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_method_patches.pkl"))
    print(f"METHODS: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = normalize_names_and_save(selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_normalized_patches.pkl"))
    print(f"NORMALIZED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = get_single_methods_and_save(selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_single_hunk_patches.pkl"))
    print(f"SINGLE METHODS: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = deduplicate_patches_and_save(selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_deduplicated_patches.pkl"))
    print(f"DEDUPLICATED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    return selected_tool_patches


if __name__ == "__main__":
    # bugs, developer_patches, tool_patches = init(configure=False)

    # # selected_tool_name = "transplantfix"
    # # selected_tool_name = "recoder"
    # selected_tool_name = "circle"

    # selected_tool_patches = pd.read_pickle(os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_patches.pkl"))

    # print(f"READ: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")

    # selected_tool_patches = clean_and_save_patches(bugs, selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_patches_cleaned.pkl"))
    
    # print(f"CLEANED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")

    # selected_tool_patches = get_methods_and_save(bugs, selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_method_patches.pkl"))
    
    # print(f"METHODS: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")

    # selected_tool_patches = normalize_names_and_save(selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_normalized_patches.pkl"))
    
    # print(f"NORMALIZED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    
    # selected_tool_patches = get_single_methods_and_save(selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_single_hunk_patches.pkl"))
    
    # print(f"SINGLE METHODS: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    
    # selected_tool_patches = deduplicate_patches_and_save(selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_deduplicated_patches.pkl"))

    # print(f"DEDUPLICATED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")

    # # Load deduplicated tool patches
    # tool_patches = pd.read_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)
    # print(f"Correct Tool Patches: {len(tool_patches[tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(tool_patches[tool_patches['correctness'] == 'Overfitting'])}")
    
    # results_df = assign_sourcerercc_labels(tool_patches, selected_tool_patches)
    # results_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_sourcerercc_labels.pkl"))
    # results_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_sourcerercc_labels.html"))

    """"""
    """"""
    """"""

    # Just force sudo
    bugs, developer_patches, tool_patches = init(configure=False)
    bug = bugs.loc['defects4j-Closure-63'].copy()
    bug['uid'] = bug.name
    checkout_dir = checkout_bug(bug)
    if os.path.exists(checkout_dir):
        shutil.rmtree(checkout_dir)


    # tool_1_name, tool_2_name = "recoder", "circle"
    # tool_1_name, tool_2_name = "recoder", "transplantfix"
    tool_2_name, tool_1_name = "recoder", "circle"
    # tool_2_name, tool_1_name = "recoder", "transplantfix"
    selected_tool_patches_1 = get_selected_tool_patches(tool_1_name)
    report_data(selected_tool_patches_1)
    selected_tool_patches_2 = get_selected_tool_patches(tool_2_name)
    report_data(selected_tool_patches_2)

    results_df = assign_sourcerercc_labels(selected_tool_patches_1, selected_tool_patches_2)
    results_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{tool_1_name}_{tool_2_name}_sourcerercc_labels.pkl"))
    results_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{tool_1_name}_{tool_2_name}_sourcerercc_labels.html"))

    """"""

    # tool_1_name, tool_2_name = "circle", "transplantfix"
    tool_2_name, tool_1_name = "circle", "transplantfix"
    selected_tool_patches_1 = get_selected_tool_patches(tool_1_name)
    report_data(selected_tool_patches_1)
    selected_tool_patches_2 = get_selected_tool_patches(tool_2_name)
    report_data(selected_tool_patches_2)

    results_df = assign_sourcerercc_labels(selected_tool_patches_1, selected_tool_patches_2)
    results_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{tool_1_name}_{tool_2_name}_sourcerercc_labels.pkl"))
    results_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{tool_1_name}_{tool_2_name}_sourcerercc_labels.html"))
