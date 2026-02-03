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

# if __name__ == "__main__":
#     # bugs, developer_patches, tool_patches = init(configure=False)

#     # # selected_tool_name = "transplantfix"
#     # # selected_tool_name = "recoder"
#     # # selected_tool_name = "circle"
#     # # selected_tool_name = "dlfix"
#     # # selected_tool_name = "iter"
#     # selected_tool_name = "arjae"

#     # selected_tool_patches = pd.read_pickle(os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_patches.pkl"))

#     # print(f"READ: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")

#     # selected_tool_patches = clean_and_save_patches(bugs, selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_patches_cleaned.pkl"))
    
#     # print(f"CLEANED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")

#     # selected_tool_patches = get_methods_and_save(bugs, selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_method_patches.pkl"))
    
#     # print(f"METHODS: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")

#     # selected_tool_patches = normalize_names_and_save(selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_normalized_patches.pkl"))
    
#     # print(f"NORMALIZED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    
#     # selected_tool_patches = get_single_methods_and_save(selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_single_hunk_patches.pkl"))
    
#     # print(f"SINGLE METHODS: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    
#     # selected_tool_patches = deduplicate_patches_and_save(selected_tool_patches, os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_deduplicated_patches.pkl"))

#     # print(f"DEDUPLICATED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")

#     # # Load deduplicated tool patches
#     # tool_patches = pd.read_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)
#     # print(f"Correct Tool Patches: {len(tool_patches[tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(tool_patches[tool_patches['correctness'] == 'Overfitting'])}")
    
#     # results_df = assign_sourcerercc_labels(tool_patches, selected_tool_patches)
#     # results_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_sourcerercc_labels.pkl"))
#     # results_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{selected_tool_name}_sourcerercc_labels.html"))

#     """"""
#     """"""
#     """"""

#     # # Just force sudo
#     # bugs, developer_patches, tool_patches = init(configure=False)
#     # bug = bugs.loc['defects4j-Closure-63'].copy()
#     # bug['uid'] = bug.name
#     # checkout_dir = checkout_bug(bug)
#     # if os.path.exists(checkout_dir):
#     #     shutil.rmtree(checkout_dir)


#     # get_sourcerercc_labels("dlfix", "recoder")
#     # get_sourcerercc_labels("dlfix", "circle")
#     # get_sourcerercc_labels("dlfix", "transplantfix")
#     # get_sourcerercc_labels("dlfix", "iter")

#     # get_sourcerercc_labels("arjae", "recoder")
#     # get_sourcerercc_labels("arjae", "circle")
#     # get_sourcerercc_labels("arjae", "transplantfix")
#     # get_sourcerercc_labels("arjae", "iter")
    
#     # get_sourcerercc_labels("recoder", "circle")
#     # get_sourcerercc_labels("recoder", "transplantfix")
#     # get_sourcerercc_labels("recoder", "iter")

#     # get_sourcerercc_labels("circle", "transplantfix")
#     # get_sourcerercc_labels("circle", "iter")

#     # get_sourcerercc_labels("transplantfix", "iter")


#     """"""
#     """"""
#     """"""
#     tool_name = "dlfix"
#     selected_tool_patches = get_selected_tool_patches(tool_name)
#     correct_tool_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Correct']
#     overfitting_tool_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting']
#     print(f"{tool_name}: Correct Patches: {len(correct_tool_patches)}, Overfitting Patches: {len(overfitting_tool_patches)}")

#     tool_name = "arjae"
#     selected_tool_patches = get_selected_tool_patches(tool_name)
#     correct_tool_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Correct']
#     overfitting_tool_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting']
#     print(f"{tool_name}: Correct Patches: {len(correct_tool_patches)}, Overfitting Patches: {len(overfitting_tool_patches)}")


#     get_bar_scores("iter", ["recoder", "circle", "dlfix", "arjae", "transplantfix"])
#     get_bar_scores("transplantfix", ["recoder", "circle", "dlfix", "arjae"])
#     get_bar_scores("circle", ["recoder", "dlfix", "arjae"])
#     get_bar_scores("recoder", ["dlfix", "arjae"])



import warnings
warnings.filterwarnings('ignore')

import matplotlib
matplotlib.use('Agg')  # Non-interactive backend
import matplotlib.pyplot as plt
import pandas as pd
import os

from upsetplot import UpSet, from_contents

# Output directory
OUTPUT_DIR = "rq4"
os.makedirs(OUTPUT_DIR, exist_ok=True)


def get_venn_data_both(tool_name, reference_tools):
    """
    Get sets of matched UIDs for each reference tool for BOTH correct and overfitting.
    """
    selected_tool_patches = get_selected_tool_patches(tool_name).copy()
    
    correct_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Correct']
    overfitting_patches = selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting']
    
    correct_match_sets = {}
    overfitting_match_sets = {}
    
    for ref_tool in reference_tools:
        pair_info_dir = os.path.join(RQ4_META_DATA_DIR, f"{ref_tool}_{tool_name}_sourcerercc_labels.pkl")
        results_df = pd.read_pickle(pair_info_dir)
        
        # Correct patches
        correct_matched_uids = set()
        for idx, row in correct_patches.iterrows():
            uid = row.name
            match_row = results_df[results_df['uid'] == uid]
            if not match_row.empty and match_row.iloc[0]['sourcerercc_label'] == True:
                correct_matched_uids.add(uid)
        correct_match_sets[ref_tool] = correct_matched_uids
        
        # Overfitting patches
        overfitting_matched_uids = set()
        for idx, row in overfitting_patches.iterrows():
            uid = row.name
            match_row = results_df[results_df['uid'] == uid]
            if not match_row.empty and match_row.iloc[0]['sourcerercc_label'] == True:
                overfitting_matched_uids.add(uid)
        overfitting_match_sets[ref_tool] = overfitting_matched_uids
    
    return (correct_match_sets, overfitting_match_sets, 
            set(correct_patches.index), set(overfitting_patches.index))


def plot_upset(match_sets, all_uids, tool_name, patch_type, reference_tools):
    """
    Plot an UpSet diagram - clear visualization for 5+ sets.
    """
    display_names = {
        'dlfix': 'DLFix',
        'arjae': 'ARJA-e', 
        'recoder': 'Recoder',
        'circle': 'CIRCLE',
        'transplantfix': 'TransplantFix'
    }
    
    data = {display_names.get(ref_tool, ref_tool.upper()): match_sets[ref_tool] 
            for ref_tool in reference_tools}
    
    upset_data = from_contents(data)
    
    fig = plt.figure(figsize=(12, 6))
    
    upset = UpSet(upset_data, 
                  subset_size='count', 
                  show_counts=True,
                  show_percentages=False,
                  sort_by='cardinality',
                  sort_categories_by='cardinality',
                  facecolor='steelblue',
                  element_size=46)
    
    upset.plot(fig=fig)
    
    # fig.suptitle(f'{tool_name.upper()} - {patch_type} Patches', fontsize=18, fontweight='bold', y=1.02)
    
    filename = f'upset_{tool_name}_{patch_type.lower()}'
    pdf_path = os.path.join(OUTPUT_DIR, f'{filename}.pdf')
    png_path = os.path.join(OUTPUT_DIR, f'{filename}.png')
    plt.savefig(pdf_path, dpi=300, bbox_inches='tight')
    plt.savefig(png_path, dpi=300, bbox_inches='tight')
    plt.close()
    
    print(f"Saved: {pdf_path}")
    print(f"Saved: {png_path}")


def analyze_and_plot(tool_name, reference_tools):
    """
    Analyze a tool and create UpSet diagrams for both correct and overfitting patches.
    """
    print(f"\n{'='*60}")
    print(f"Diagram Analysis for {tool_name.upper()}")
    print(f"Reference tools: {', '.join(reference_tools)}")
    print(f"Output directory: {os.path.abspath(OUTPUT_DIR)}")
    print(f"{'='*60}")
    
    (correct_match_sets, overfitting_match_sets, 
     all_correct_uids, all_overfitting_uids) = get_venn_data_both(tool_name, reference_tools)
    
    # --- CORRECT PATCHES ---
    print(f"\n--- CORRECT PATCHES ---")
    print(f"Total correct patches: {len(all_correct_uids)}")
    
    if len(all_correct_uids) > 0:
        print("Matches per reference tool:")
        for ref_tool in reference_tools:
            print(f"  {ref_tool}: {len(correct_match_sets[ref_tool])}")
        total_correct_matched = len(set.union(*correct_match_sets.values())) if any(correct_match_sets.values()) else 0
        print(f"Total unique matches: {total_correct_matched}")
        print(f"Novel (no match): {len(all_correct_uids) - total_correct_matched}")
        
        plot_upset(correct_match_sets, all_correct_uids, tool_name, 'Correct', reference_tools)
    else:
        print("No correct patches - skipping diagram.")
    
    # --- OVERFITTING PATCHES ---
    print(f"\n--- OVERFITTING PATCHES ---")
    print(f"Total overfitting patches: {len(all_overfitting_uids)}")
    
    if len(all_overfitting_uids) > 0:
        print("Matches per reference tool:")
        for ref_tool in reference_tools:
            print(f"  {ref_tool}: {len(overfitting_match_sets[ref_tool])}")
        total_overfitting_matched = len(set.union(*overfitting_match_sets.values())) if any(overfitting_match_sets.values()) else 0
        print(f"Total unique matches: {total_overfitting_matched}")
        print(f"Novel (no match): {len(all_overfitting_uids) - total_overfitting_matched}")
        
        plot_upset(overfitting_match_sets, all_overfitting_uids, tool_name, 'Overfitting', reference_tools)
    else:
        print("No overfitting patches - skipping diagram.")


if __name__ == "__main__":
    # analyze_and_plot("iter", ["dlfix", "arjae", "recoder", "circle", "transplantfix"])
    analyze_and_plot("t5apr", ["arjae", "recoder", "selfapr", "knod", "tare", "transplantfix"])