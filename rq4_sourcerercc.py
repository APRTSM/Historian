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
    
    results = []
    total = len(selected_tool_patches)
    
    for idx, (_, row) in enumerate(tqdm(
        selected_tool_patches.iterrows(),
        total=total,
        desc="Detecting SourcererCC clones"
    )):
        uid = row.name
        bug_uid = row['bug_uid']
        selected_content = row['content']

        # log the index out of total
        logging.info(f"Processing patch {idx + 1}/{total} (UID: {uid})")
        
        # Find all patches with matching bug_uid in tool_patches
        matching_patches = tool_patches[tool_patches['bug_uid'] == bug_uid]

        if len(matching_patches) > 10:
            matching_patches = matching_patches.head(10)
        
        # Check if any matching patch is a clone
        is_clone = False
        for _, match_row in matching_patches.iterrows():
            match_content = match_row['content']
            label = sourcerercc_are_clones(selected_content, match_content)
            logging.info(f"Comparing UID {uid} with matching patch UID {match_row.name}: Clone={label}")
            if label:
                is_clone = True
                break
        
        results.append({'uid': uid, 'sourcerercc_label': is_clone})
    
    result_df = pd.DataFrame(results)
    logging.info(f"Completed! Processed {len(result_df)} patches.")
    
    return result_df


if __name__ == "__main__":
    bugs, developer_patches, tool_patches = init(configure=False)

    selected_tool_name = "transplantfix"
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
    
    results_df = assign_sourcerercc_labels(tool_patches, selected_tool_patches)
    results_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_sourcerercc_labels.pkl"))
    results_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_sourcerercc_labels.html"))

