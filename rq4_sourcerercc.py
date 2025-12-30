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
    
    for _, row in tqdm(
        selected_tool_patches.iterrows(),
        total=len(selected_tool_patches),
        desc="Detecting SourcererCC clones"
    ):
        uid = row['uid']
        bug_uid = row['bug_uid']
        selected_content = row['content']
        
        # Find all patches with matching bug_uid in tool_patches
        matching_patches = tool_patches[tool_patches['bug_uid'] == bug_uid]
        
        # Check if any matching patch is a clone
        is_clone = False
        for _, match_row in matching_patches.iterrows():
            match_content = match_row['content']
            if sourcerercc_are_clones(selected_content, match_content):
                is_clone = True
                break
        
        results.append({'uid': uid, 'sourcerercc_label': is_clone})
    
    result_df = pd.DataFrame(results)
    logging.info(f"Completed! Processed {len(result_df)} patches.")
    
    return result_df








if __name__ == "__main__":
    tool_patches = pd.read_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)

    bugs, developer_patches, tool_patches = init(configure=False)

    selected_tool_name = "transplantfix"
    selected_tool_patches = pd.read_pickle(os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_patches.pkl"))


    selected_tool_patches = clean_and_save_patches(bugs, selected_tool_patches, f"{selected_tool_name}_patches_cleaned.pkl")
    selected_tool_patches = get_methods_and_save(bugs, selected_tool_patches, f"{selected_tool_name}_method_patches.pkl")
    selected_tool_patches = normalize_names_and_save(selected_tool_patches, f"{selected_tool_name}_normalized_patches.pkl")
    selected_tool_patches = get_single_methods_and_save(selected_tool_patches, f"{selected_tool_name}_single_hunk_patches.pkl")
    selected_tool_patches = deduplicate_patches_and_save(selected_tool_patches, f"{selected_tool_name}_deduplicated_patches.pkl")


        are_clones = sourcerercc_are_clones(target_method_content, target_method_groundtruth_content)

