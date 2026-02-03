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
        
        try:
            label = match_type2(comp['selected_content'], comp['match_content'])
        except Exception as e:
            logging.error(f"Error comparing UID {uid} with matching patch UID {comp['match_uid']}: {e}")
            label = False

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
    # print(f"READ: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = clean_and_save_patches(bugs, selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_patches_cleaned.pkl"))
    # print(f"CLEANED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = get_methods_and_save(bugs, selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_method_patches.pkl"))
    # print(f"METHODS: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = normalize_names_and_save(selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_normalized_patches.pkl"))
    # print(f"NORMALIZED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = get_single_methods_and_save(selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_single_hunk_patches.pkl"))
    # print(f"SINGLE METHODS: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = deduplicate_patches_and_save(selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_deduplicated_patches.pkl"))
    # print(f"DEDUPLICATED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    return selected_tool_patches


def get_sourcerercc_labels(tool_1_name, tool_2_name):
    selected_tool_patches_1 = get_selected_tool_patches(tool_1_name)
    report_data(selected_tool_patches_1)
    selected_tool_patches_2 = get_selected_tool_patches(tool_2_name)
    report_data(selected_tool_patches_2)

    if os.path.exists(os.path.join(RQ4_META_DATA_DIR, f"{tool_1_name}_{tool_2_name}_sourcerercc_labels.pkl")):
        print(f"✅ SourcererCC labels already exist for {tool_1_name} vs {tool_2_name}.")
    
    else:
        results_df = assign_sourcerercc_labels(selected_tool_patches_1, selected_tool_patches_2)
        results_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{tool_1_name}_{tool_2_name}_sourcerercc_labels.pkl"))
        results_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{tool_1_name}_{tool_2_name}_sourcerercc_labels.html"))

    print("HTML file saved. Location: " + os.path.join(RQ4_META_DATA_DIR, f"{tool_1_name}_{tool_2_name}_sourcerercc_labels.html"))

def get_bar_scores(tool_name, other_tools):
        selected_tool_patches = get_selected_tool_patches(tool_name).copy()

        # add a column 'match' initialized to False
        selected_tool_patches["match"] = False

        for other_tool in other_tools:
            pair_info_dir = os.path.join(RQ4_META_DATA_DIR, f"{other_tool}_{tool_name}_sourcerercc_labels.pkl")
            
            results_df = pd.read_pickle(pair_info_dir)
            
            # Iterate over selected_tool_patches and set 'match' to True if uid in results_df with sourcerercc_label True
            for idx, row in selected_tool_patches.iterrows():
                uid = row.name
                match_row = results_df[results_df['uid'] == uid]
                if not match_row.empty and match_row.iloc[0]['sourcerercc_label'] == True:
                    selected_tool_patches.at[idx, 'match'] = True

        print("="*50)
        print(f"Selected Tool: {tool_name}")

        correct_selected_tool_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Correct']
        overfitting_selected_tool_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting']
        print(f"Selected Tool Patches for {tool_name}: Correct: {len(correct_selected_tool_patches)}, Overfitting: {len(overfitting_selected_tool_patches)}")

        matched_correct = len(correct_selected_tool_patches[correct_selected_tool_patches['match'] == True])
        matched_overfitting = len(overfitting_selected_tool_patches[overfitting_selected_tool_patches['match'] == True])    
        print(f"Matched Correct: {matched_correct}, Matched Overfitting: {matched_overfitting}")

def get_bar_scores_detailed(tool_name, other_tools):
    """
    Reports matches with baseline, added tools, and both (intersection).
    - Baseline: matches from {tool_name}_sourcerercc_labels.pkl (vs TMP_DEDUPLICATED_TOOL_PATHCES_PKL)
    - Added tools: matches from {other_tool}_{tool_name}_sourcerercc_labels.pkl
    """
    selected_tool_patches = get_selected_tool_patches(tool_name).copy()
    
    # Initialize columns
    selected_tool_patches["match_baseline"] = False
    selected_tool_patches["match_added_tools"] = False
    
    # 1. Check matches with BASELINE (original tool_patches from TMP_DEDUPLICATED_TOOL_PATHCES_PKL)
    baseline_labels_path = os.path.join(RQ4_META_DATA_DIR, f"{tool_name}_sourcerercc_labels.pkl")
    if os.path.exists(baseline_labels_path):
        baseline_df = pd.read_pickle(baseline_labels_path)
        for idx, row in selected_tool_patches.iterrows():
            uid = row.name
            match_row = baseline_df[baseline_df['uid'] == uid]
            if not match_row.empty and match_row.iloc[0]['sourcerercc_label'] == True:
                selected_tool_patches.at[idx, 'match_baseline'] = True
    else:
        print(f"Warning: Baseline labels not found at {baseline_labels_path}")
    
    # 2. Check matches with ADDED TOOLS
    for other_tool in other_tools:
        pair_info_dir = os.path.join(RQ4_META_DATA_DIR, f"{other_tool}_{tool_name}_sourcerercc_labels.pkl")
        if not os.path.exists(pair_info_dir):
            print(f"Warning: {pair_info_dir} not found")
            continue
        results_df = pd.read_pickle(pair_info_dir)
        for idx, row in selected_tool_patches.iterrows():
            uid = row.name
            match_row = results_df[results_df['uid'] == uid]
            if not match_row.empty and match_row.iloc[0]['sourcerercc_label'] == True:
                selected_tool_patches.at[idx, 'match_added_tools'] = True
    
    # Calculate categories for CORRECT
    correct = selected_tool_patches[selected_tool_patches['correctness'] == 'Correct']
    c_baseline_only = len(correct[(correct['match_baseline'] == True) & (correct['match_added_tools'] == False)])
    c_added_only = len(correct[(correct['match_baseline'] == False) & (correct['match_added_tools'] == True)])
    c_both = len(correct[(correct['match_baseline'] == True) & (correct['match_added_tools'] == True)])
    c_total = len(correct)
    
    # Calculate categories for OVERFITTING
    overfitting = selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting']
    o_baseline_only = len(overfitting[(overfitting['match_baseline'] == True) & (overfitting['match_added_tools'] == False)])
    o_added_only = len(overfitting[(overfitting['match_baseline'] == False) & (overfitting['match_added_tools'] == True)])
    o_both = len(overfitting[(overfitting['match_baseline'] == True) & (overfitting['match_added_tools'] == True)])
    o_total = len(overfitting)
    
    print("="*60)
    print(f"Tool: {tool_name}")
    print(f"CORRECT (n={c_total}): Baseline={c_baseline_only}, Added={c_added_only}, Both={c_both}, Total={len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}")
    print(f"OVERFITTING (n={o_total}): Baseline={o_baseline_only}, Added={o_added_only}, Both={o_both}, Total={len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    
    return {
        'tool': tool_name,
        'c_total': c_total, 'c_baseline_only': c_baseline_only, 'c_added_only': c_added_only, 'c_both': c_both,
        'o_total': o_total, 'o_baseline_only': o_baseline_only, 'o_added_only': o_added_only, 'o_both': o_both,
    }

def get_pre_matches(selected_tool_name):
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

    # Load deduplicated tool patches
    tool_patches = pd.read_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)
    print(f"Correct Tool Patches: {len(tool_patches[tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(tool_patches[tool_patches['correctness'] == 'Overfitting'])}")
    
    result_dir = os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_sourcerercc_labels.pkl")
    if os.path.exists(result_dir):
        print(f"✅ SourcererCC labels already exist for {selected_tool_name}. Location: {result_dir} HTML file saved. Location: " + os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_sourcerercc_labels.html"))
    
    else:
        results_df = assign_sourcerercc_labels(tool_patches, selected_tool_patches)
        results_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_sourcerercc_labels.pkl"))
        results_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_sourcerercc_labels.html"))
        print("HTML file saved. Location: " + os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_sourcerercc_labels.html"))

if __name__ == "__main__":


    # For Pre
    # selected_tool_name = "transplantfix"
    # selected_tool_name = "recoder"
    # selected_tool_name = "circle"
    # selected_tool_name = "dlfix"
    # selected_tool_name = "iter"
    # selected_tool_name = "arjae"
    # selected_tool_name = "t5apr"
    # selected_tool_name = "selfapr"
    # selected_tool_name = "knod"
    # selected_tool_name = "tare"

    # get_pre_matches(selected_tool_name)


    """"""
    """"""
    """"""

    # without pre
    # Just force sudo
    bugs, developer_patches, tool_patches = init(configure=False)
    bug = bugs.loc['defects4j-Closure-63'].copy()
    bug['uid'] = bug.name
    checkout_dir = checkout_bug(bug)
    if os.path.exists(checkout_dir):
        shutil.rmtree(checkout_dir)


    # get_sourcerercc_labels("dlfix", "recoder")
    # get_sourcerercc_labels("dlfix", "circle")
    # get_sourcerercc_labels("dlfix", "transplantfix")
    # get_sourcerercc_labels("dlfix", "iter")
    # get_sourcerercc_labels("dlfix", "t5apr")

    # get_sourcerercc_labels("arjae", "recoder")
    # get_sourcerercc_labels("arjae", "circle")
    # get_sourcerercc_labels("arjae", "transplantfix")
    # get_sourcerercc_labels("arjae", "iter")
    # get_sourcerercc_labels("arjae", "t5apr")

    # get_sourcerercc_labels("recoder", "circle")
    # get_sourcerercc_labels("recoder", "transplantfix")
    # get_sourcerercc_labels("recoder", "iter")
    # get_sourcerercc_labels("recoder", "t5apr")

    # get_sourcerercc_labels("circle", "transplantfix")
    # get_sourcerercc_labels("circle", "iter")
    # get_sourcerercc_labels("circle", "t5apr")

    # get_sourcerercc_labels("transplantfix", "iter")
    # get_sourcerercc_labels("transplantfix", "t5apr")

    # # v2
    # # 2020
    # get_sourcerercc_labels("arjae", "recoder")
    # get_sourcerercc_labels("arjae", "selfapr")
    # get_sourcerercc_labels("arjae", "knod")
    # get_sourcerercc_labels("arjae", "tare")
    # get_sourcerercc_labels("arjae", "transplantfix")
    # get_sourcerercc_labels("arjae", "t5apr")

    # # 2021
    # get_sourcerercc_labels("recoder", "selfapr")
    # get_sourcerercc_labels("recoder", "knod")
    # get_sourcerercc_labels("recoder", "tare")
    # get_sourcerercc_labels("recoder", "transplantfix")
    # get_sourcerercc_labels("recoder", "t5apr")

    # # 2022
    # get_sourcerercc_labels("selfapr", "knod")
    # get_sourcerercc_labels("selfapr", "tare")
    # get_sourcerercc_labels("selfapr", "transplantfix")
    # get_sourcerercc_labels("selfapr", "t5apr")

    # # 2023
    # get_sourcerercc_labels("knod", "t5apr")
    # get_sourcerercc_labels("tare", "t5apr")
    # get_sourcerercc_labels("transplantfix", "t5apr")

    

    """"""
    """"""
    """"""

    # # Quick checks Get Bars
    # tool_name = "dlfix"
    # selected_tool_patches = get_selected_tool_patches(tool_name)
    # correct_tool_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Correct']
    # overfitting_tool_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting']
    # print(f"{tool_name}: Correct Patches: {len(correct_tool_patches)}, Overfitting Patches: {len(overfitting_tool_patches)}")

    # tool_name = "arjae"
    # selected_tool_patches = get_selected_tool_patches(tool_name)
    # correct_tool_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Correct']
    # overfitting_tool_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting']
    # print(f"{tool_name}: Correct Patches: {len(correct_tool_patches)}, Overfitting Patches: {len(overfitting_tool_patches)}")

    # # get_bar_scores("iter", ["recoder", "circle", "dlfix", "arjae", "transplantfix"])
    # # get_bar_scores("transplantfix", ["recoder", "circle", "dlfix", "arjae"])
    # # get_bar_scores("circle", ["recoder", "dlfix", "arjae"])
    # # get_bar_scores("recoder", ["dlfix", "arjae"])

    """"""
    """"""
    """"""

    # Get overall stats
    tool_patches = pd.read_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)
    print(f"Correct Tool Patches: {len(tool_patches[tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(tool_patches[tool_patches['correctness'] == 'Overfitting'])}")
    

    # tool_name = "dlfix"
    # selected_tool_patches = get_selected_tool_patches(tool_name)
    # correct_tool_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Correct']
    # overfitting_tool_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting']
    # print(f"{tool_name}: Correct Patches: {len(correct_tool_patches)}, Overfitting Patches: {len(overfitting_tool_patches)}")

    # # v2
    tool_name = "arjae"
    selected_tool_patches = get_selected_tool_patches(tool_name)
    correct_tool_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Correct']
    overfitting_tool_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting']
    print(f"{tool_name}: Correct Patches: {len(correct_tool_patches)}, Overfitting Patches: {len(overfitting_tool_patches)}")

    # Get detailed scores with baseline/added/both breakdown
    results = []
    # results.append(get_bar_scores_detailed("dlfix", []))
    # results.append(get_bar_scores_detailed("arjae", []))
    # results.append(get_bar_scores_detailed("recoder", ["dlfix", "arjae"]))
    # results.append(get_bar_scores_detailed("circle", ["recoder", "dlfix", "arjae"]))
    # results.append(get_bar_scores_detailed("transplantfix", ["recoder", "circle", "dlfix", "arjae"]))
    # results.append(get_bar_scores_detailed("iter", ["recoder", "circle", "dlfix", "arjae", "transplantfix"]))
    
    # # v2
    results.append(get_bar_scores_detailed("arjae", []))
    results.append(get_bar_scores_detailed("recoder", ["arjae"]))
    results.append(get_bar_scores_detailed("selfapr", ["arjae", "recoder"]))
    results.append(get_bar_scores_detailed("knod", ["arjae", "recoder", "selfapr"]))
    results.append(get_bar_scores_detailed("tare", ["arjae", "recoder", "selfapr"]))
    results.append(get_bar_scores_detailed("transplantfix", ["arjae", "recoder", "selfapr"]))
    results.append(get_bar_scores_detailed("t5apr", ["arjae", "recoder", "selfapr", "knod", "tare", "transplantfix"]))

    # Print summary
    print("\n" + "="*80)
    print("SUMMARY FOR STACKED BAR CHART")
    print("="*80)
    for r in results:
        c_pct_base = 100*r['c_baseline_only']/r['c_total'] if r['c_total'] > 0 else 0
        c_pct_added = 100*r['c_added_only']/r['c_total'] if r['c_total'] > 0 else 0
        c_pct_both = 100*r['c_both']/r['c_total'] if r['c_total'] > 0 else 0
        print(f"{r['tool']}: Correct baseline={c_pct_base:.1f}%, added={c_pct_added:.1f}%, both={c_pct_both:.1f}%")