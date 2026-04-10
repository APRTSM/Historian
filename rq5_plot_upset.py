"""UpSet plots of patch overlap across APR tools (paper RQ5)."""
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
from rq5_llms import *


def report_data(df):
    print(f"Correct Tool Patches: {len(df[df['correctness'] == 'Correct'])}, Overfitting: {len(df[df['correctness'] == 'Overfitting'])}")


def get_selected_tool_patches(selected_tool_name):
    bugs, developer_patches, tool_patches = init(configure=False)

    selected_tool_patches = pd.read_pickle(os.path.join(RQ5_META_DATA_DIR, f"{selected_tool_name}_patches.pkl"))
    # print(f"READ: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = clean_and_save_patches(bugs, selected_tool_patches, os.path.join(RQ5_META_DATA_DIR, f"{selected_tool_name}_patches_cleaned.pkl"))
    # print(f"CLEANED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = get_methods_and_save(bugs, selected_tool_patches, os.path.join(RQ5_META_DATA_DIR, f"{selected_tool_name}_method_patches.pkl"))
    # print(f"METHODS: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = normalize_names_and_save(selected_tool_patches, os.path.join(RQ5_META_DATA_DIR, f"{selected_tool_name}_normalized_patches.pkl"))
    # print(f"NORMALIZED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = get_single_methods_and_save(selected_tool_patches, os.path.join(RQ5_META_DATA_DIR, f"{selected_tool_name}_single_hunk_patches.pkl"))
    # print(f"SINGLE METHODS: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    selected_tool_patches = deduplicate_patches_and_save(selected_tool_patches, os.path.join(RQ5_META_DATA_DIR, f"{selected_tool_name}_deduplicated_patches.pkl"))
    # print(f"DEDUPLICATED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    return selected_tool_patches

def get_bar_scores(tool_name, other_tools):
        selected_tool_patches = get_selected_tool_patches(tool_name).copy()

        # add a column 'match' initialized to False
        selected_tool_patches["match"] = False

        for other_tool in other_tools:
            pair_info_dir = os.path.join(RQ5_META_DATA_DIR, f"{other_tool}_{tool_name}_sourcerercc_labels.pkl")
            
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

#     # selected_tool_patches = pd.read_pickle(os.path.join(RQ5_META_DATA_DIR, f"{selected_tool_name}_patches.pkl"))

#     # print(f"READ: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")

#     # selected_tool_patches = clean_and_save_patches(bugs, selected_tool_patches, os.path.join(RQ5_META_DATA_DIR, f"{selected_tool_name}_patches_cleaned.pkl"))
    
#     # print(f"CLEANED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")

#     # selected_tool_patches = get_methods_and_save(bugs, selected_tool_patches, os.path.join(RQ5_META_DATA_DIR, f"{selected_tool_name}_method_patches.pkl"))
    
#     # print(f"METHODS: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")

#     # selected_tool_patches = normalize_names_and_save(selected_tool_patches, os.path.join(RQ5_META_DATA_DIR, f"{selected_tool_name}_normalized_patches.pkl"))
    
#     # print(f"NORMALIZED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    
#     # selected_tool_patches = get_single_methods_and_save(selected_tool_patches, os.path.join(RQ5_META_DATA_DIR, f"{selected_tool_name}_single_hunk_patches.pkl"))
    
#     # print(f"SINGLE METHODS: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")
    
#     # selected_tool_patches = deduplicate_patches_and_save(selected_tool_patches, os.path.join(RQ5_META_DATA_DIR, f"{selected_tool_name}_deduplicated_patches.pkl"))

#     # print(f"DEDUPLICATED: Correct Selected Tool Patches: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'])}")

#     # # Load deduplicated tool patches
#     # tool_patches = pd.read_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)
#     # print(f"Correct Tool Patches: {len(tool_patches[tool_patches['correctness'] == 'Correct'])}, Overfitting: {len(tool_patches[tool_patches['correctness'] == 'Overfitting'])}")
    
#     # results_df = assign_sourcerercc_labels(tool_patches, selected_tool_patches)
#     # results_df.to_pickle(os.path.join(RQ5_META_DATA_DIR, f"{selected_tool_name}_sourcerercc_labels.pkl"))
#     # results_df.to_html(os.path.join(RQ5_META_DATA_DIR, f"{selected_tool_name}_sourcerercc_labels.html"))

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
OUTPUT_DIR = "rq5"
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
        pair_info_dir = os.path.join(RQ5_META_DATA_DIR, f"{ref_tool}_{tool_name}_sourcerercc_labels.pkl")
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
    display_names = {
        'arjae': 'ARJA-e (2020)',
        'recoder': 'Recoder (2021)',
        'selfapr': 'SelfAPR (2022)',
        'knod': 'Knod (2023)',
        'tare': 'TARE (2023)',
        'transplantfix': 'TransplantFix (2023)',
        't5apr': 'T5APR (2024)',
    }
    
    # Define the category order: top to bottom in the UpSet matrix
    # UpSet plots the first category at the bottom, last at the top of the matrix,
    # so we reverse: we want ARJA-e at top, TransplantFix at bottom
    category_order = [
        'ARJA-e (2020)',
        'Recoder (2021)',
        'SelfAPR (2022)',
        'Knod (2023)',
        'TARE (2023)',
        'TransplantFix (2023)',
    ]
    
    data = {display_names.get(ref_tool, ref_tool.upper()): match_sets[ref_tool] 
            for ref_tool in reference_tools}
    
    upset_data = from_contents(data)
    
    # Filter category_order to only include tools actually present
    ordered_cats = [c for c in category_order if c in upset_data.index.names]
    
    fig = plt.figure(figsize=(14, 6))
    
    upset = UpSet(upset_data, 
                  subset_size='count', 
                  show_counts=True,
                  show_percentages=False,
                  sort_by='degree',
                  sort_categories_by=None,  # disable auto-sorting to use our custom order
                  facecolor='black',
                  element_size=46)
    
    # Reorder categories manually
    upset._category_order = ordered_cats
    
    upset.plot(fig=fig)
    
    filename = f'upset_{tool_name}_{patch_type.lower()}'
    pdf_path = os.path.join(OUTPUT_DIR, f'{filename}.pdf')
    png_path = os.path.join(OUTPUT_DIR, f'{filename}.png')
    plt.savefig(pdf_path, dpi=300, bbox_inches='tight')
    plt.savefig(png_path, dpi=300, bbox_inches='tight')
    plt.close()
    
    print(f"Saved: {pdf_path}")
    print(f"Saved: {png_path}")
















def plot_upset(match_sets, all_uids, tool_name, patch_type, reference_tools):
    display_names = {
        'arjae': 'ARJA-e (2020)',
        'recoder': 'Recoder (2021)',
        'selfapr': 'SelfAPR (2022)',
        'knod': 'Knod (2023)',
        'tare': 'TARE (2023)',
        'transplantfix': 'TransplantFix (2023)',
        't5apr': 'T5APR (2024)',
    }
    
    # Define the category order: top to bottom in the UpSet matrix
    # UpSet plots the first category at the bottom, last at the top of the matrix,
    # so we reverse: we want ARJA-e at top, TransplantFix at bottom
    category_order = [
        'TransplantFix (2023)',
        'TARE (2023)',
        'Knod (2023)',
        'SelfAPR (2022)',
        'Recoder (2021)',
        'ARJA-e (2020)',
    ]
    
    data = {display_names.get(ref_tool, ref_tool.upper()): match_sets[ref_tool] 
            for ref_tool in reference_tools}
    
    upset_data = from_contents(data)
    
    # Filter category_order to only include tools actually present
    ordered_cats = [c for c in category_order if c in upset_data.index.names]
    
    fig = plt.figure(figsize=(14, 6))
    
    upset = UpSet(upset_data, 
                  subset_size='count', 
                  show_counts=True,
                  show_percentages=False,
                  sort_by='degree',
                  sort_categories_by=None,  # disable auto-sorting to use our custom order
                  facecolor='black',
                  element_size=46)
    
    # Reorder categories manually
    upset._category_order = ordered_cats
    
    upset.plot(fig=fig)
    
    filename = f'upset_{tool_name}_{patch_type.lower()}'
    pdf_path = os.path.join(OUTPUT_DIR, f'{filename}.pdf')
    png_path = os.path.join(OUTPUT_DIR, f'{filename}.png')
    plt.savefig(pdf_path, dpi=300, bbox_inches='tight')
    plt.savefig(png_path, dpi=300, bbox_inches='tight')
    plt.close()
    
    print(f"Saved: {pdf_path}")
    print(f"Saved: {png_path}")




def plot_upset_all_degree(match_sets, all_uids, tool_name, patch_type, reference_tools):
    display_names = {
        'arjae': 'ARJA-e (2020)',
        'recoder': 'Recoder (2021)',
        'selfapr': 'SelfAPR (2022)',
        'knod': 'Knod (2023)',
        'tare': 'TARE (2023)',
        'transplantfix': 'TransplantFix (2023)',
        't5apr': 'T5APR (2024)',
    }

    category_order = [
        'ARJA-e (2020)',
        'Recoder (2021)',
        'SelfAPR (2022)',
        'Knod (2023)',
        'TARE (2023)',
        'TransplantFix (2023)',
    ]

    data = {display_names.get(ref_tool, ref_tool.upper()): match_sets[ref_tool] 
            for ref_tool in reference_tools}

    # Degree-0: patches in none of the reference tools
    all_matched = set.union(*match_sets.values()) if any(match_sets.values()) else set()
    degree_0 = all_uids - all_matched
    degree_0_count = len(degree_0)

    print(f"\n--- {patch_type} Degree Analysis ---")
    print(f"Total patches: {len(all_uids)}")
    print(f"Degree 0 (only in {tool_name}, no match): {degree_0_count}")

    upset_data = from_contents(data)
    
    # Print degree breakdown
    degrees = upset_data.index.to_frame().sum(axis=1)
    for d in sorted(degrees.unique()):
        count = (degrees == d).sum()
        print(f"Degree {d}: {count} patches")
    print(f"Sum (degree>=1): {len(all_matched)}")
    print(f"Degree 0 + Sum = {degree_0_count + len(all_matched)} (should equal {len(all_uids)})")

    # Filter category_order to only include tools actually present
    ordered_cats = [c for c in category_order if c in upset_data.index.names]

    fig = plt.figure(figsize=(14, 6))

    upset = UpSet(upset_data, 
                  subset_size='count', 
                  show_counts=True,
                  show_percentages=False,
                  sort_by='degree',
                  sort_categories_by=None,
                  facecolor='black',
                  element_size=46)

    upset._category_order = ordered_cats

    axes = upset.plot(fig=fig)
    
    # Get axes
    intersections_ax = axes['intersections']
    matrix_ax = axes['matrix']
    
    # Get current bars
    bars = intersections_ax.patches
    n_bars = len(bars)
    
    # Shift every bar's x position by 1 to make room for degree-0
    for bar in bars:
        bar.set_x(bar.get_x() + 1)
    
    # Shift existing count text labels
    for txt in intersections_ax.texts:
        txt.set_x(txt.get_position()[0] + 1)
    
    # Add the degree-0 bar at position 0
    bar_width = bars[0].get_width() if bars else 0.5
    intersections_ax.bar(0, degree_0_count, width=bar_width, color='black')
    intersections_ax.text(0, degree_0_count, str(degree_0_count), 
                          ha='center', va='bottom', fontsize=9)
    
    # Shift PathCollections (scatter dots) to the right
    from matplotlib.collections import LineCollection, PathCollection
    for collection in matrix_ax.collections:
        if isinstance(collection, PathCollection):
            offsets = collection.get_offsets()
            if len(offsets) > 0:
                offsets[:, 0] += 1
                collection.set_offsets(offsets)
        elif isinstance(collection, LineCollection):
            segments = collection.get_segments()
            new_segments = []
            for seg in segments:
                seg = np.array(seg)
                seg[:, 0] += 1
                new_segments.append(seg)
            collection.set_segments(new_segments)
    
    # Shift Line2D objects
    for line in matrix_ax.lines:
        xdata = np.array(line.get_xdata())
        line.set_xdata(xdata + 1)
    
    # Add empty circles (all unfilled) at x=0 for degree-0
    n_cats = len(ordered_cats)
    for i in range(n_cats):
        matrix_ax.plot(0, i, 'o', color='lightgrey', markersize=8, zorder=5)
    
    # Adjust x-limits for both axes
    intersections_ax.set_xlim(-0.5, n_bars + 0.5)
    matrix_ax.set_xlim(-0.5, n_bars + 0.5)

    filename = f'upset_all_{tool_name}_{patch_type.lower()}'
    pdf_path = os.path.join(OUTPUT_DIR, f'{filename}.pdf')
    png_path = os.path.join(OUTPUT_DIR, f'{filename}.png')
    plt.savefig(pdf_path, dpi=300, bbox_inches='tight')
    plt.savefig(png_path, dpi=300, bbox_inches='tight')
    plt.close()

    print(f"Saved: {pdf_path}")
    print(f"Saved: {png_path}")











# pip install supervenn

from supervenn import supervenn

def plot_supervenn(match_sets, all_uids, tool_name, patch_type, reference_tools):
    display_names = {
        'dlfix': 'DLFix',
        'arjae': 'ARJA-e', 
        'recoder': 'Recoder',
        'circle': 'CIRCLE',
        'transplantfix': 'TransplantFix',
        'selfapr': 'SelfAPR',
        'knod': 'Knod',
        'tare': 'TARE',
    }
    
    sets = [match_sets[ref_tool] for ref_tool in reference_tools]
    labels = [display_names.get(ref_tool, ref_tool.upper()) for ref_tool in reference_tools]
    
    fig, ax = plt.subplots(figsize=(16, 8))
    supervenn(sets, labels, 
             ax=ax,
             side_plots=True,
             chunks_ordering='size',
             sets_ordering='size',
             widths_minmax_ratio=0.05,
             color_cycle=['black'],
             fontsize=12)
    
    ax.set_title(f'{tool_name.upper()} - {patch_type} Patches', fontsize=16, fontweight='bold')
    
    filename = f'supervenn_{tool_name}_{patch_type.lower()}'
    pdf_path = os.path.join(OUTPUT_DIR, f'{filename}.pdf')
    png_path = os.path.join(OUTPUT_DIR, f'{filename}.png')
    plt.savefig(pdf_path, dpi=300, bbox_inches='tight')
    plt.savefig(png_path, dpi=300, bbox_inches='tight')
    plt.close()
    print(f"Saved: {pdf_path}")
    print(f"Saved: {png_path}")






# pip install venn

from venn import venn

def plot_venn6(match_sets, all_uids, tool_name, patch_type, reference_tools):
    display_names = {
        'dlfix': 'DLFix',
        'arjae': 'ARJA-e', 
        'recoder': 'Recoder',
        'circle': 'CIRCLE',
        'transplantfix': 'TransplantFix',
        'selfapr': 'SelfAPR',
        'knod': 'Knod',
        'tare': 'TARE',
    }
    
    data = {display_names.get(ref_tool, ref_tool.upper()): match_sets[ref_tool] 
            for ref_tool in reference_tools}
    
    fig, ax = plt.subplots(figsize=(12, 10))
    venn(data, ax=ax, fontsize=10)
    ax.set_title(f'{tool_name.upper()} - {patch_type} Patches', fontsize=16, fontweight='bold')
    
    filename = f'venn_{tool_name}_{patch_type.lower()}'
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
        plot_supervenn(correct_match_sets, all_correct_uids, tool_name, 'Correct', reference_tools)
        plot_venn6(correct_match_sets, all_correct_uids, tool_name, 'Correct', reference_tools)
        plot_upset_all_degree(correct_match_sets, all_correct_uids, tool_name, 'Correct', reference_tools)
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
        plot_supervenn(overfitting_match_sets, all_overfitting_uids, tool_name, 'Overfitting', reference_tools)
        plot_venn6(overfitting_match_sets, all_overfitting_uids, tool_name, 'Overfitting', reference_tools) 
        plot_upset_all_degree(overfitting_match_sets, all_overfitting_uids, tool_name, 'Overfitting', reference_tools)
    else:
        print("No overfitting patches - skipping diagram.")


if __name__ == "__main__":
    # analyze_and_plot("iter", ["dlfix", "arjae", "recoder", "circle", "transplantfix"])
    analyze_and_plot("t5apr", ["arjae", "recoder", "selfapr", "knod", "tare", "transplantfix"])

    






































def plot_upset(match_sets, all_uids, tool_name, patch_type, reference_tools):
    display_names = {
        'arjae': 'ARJA-e (2020)',
        'recoder': 'Recoder (2021)',
        'selfapr': 'SelfAPR (2022)',
        'knod': 'Knod (2023)',
        'tare': 'TARE (2023)',
        'transplantfix': 'TransplantFix (2023)',
        't5apr': 'T5APR (2024)',
    }

    category_order = [
        'T5APR (2024)',
        'TransplantFix (2023)',
        'TARE (2023)',
        'Knod (2023)',
        'SelfAPR (2022)',
        'Recoder (2021)',
        'ARJA-e (2020)',
    ]

    # Make IDs unique within each category by appending occurrence count
    # e.g. if "patch-123" appears 3 times in arjae, they become
    # "patch-123__dup0", "patch-123__dup1", "patch-123__dup2"
    data = {}
    for ref_tool in reference_tools:
        display_name = display_names.get(ref_tool, ref_tool.upper())
        uid_list = match_sets[ref_tool]
        unique_list = []
        counts = {}
        for uid in uid_list:
            if uid not in counts:
                counts[uid] = 0
            else:
                counts[uid] += 1
            unique_list.append(f"{uid}__dup{counts[uid]}")
        data[display_name] = unique_list

    upset_data = from_contents(data)

    # Filter category_order to only include tools actually present
    ordered_cats = [c for c in category_order if c in upset_data.index.names]

    fig = plt.figure(figsize=(14, 6))

    upset = UpSet(upset_data, 
                  subset_size='count', 
                  show_counts=True,
                  show_percentages=False,
                  sort_by='degree',
                  sort_categories_by=None,
                  facecolor='black',
                  element_size=46)

    upset._category_order = ordered_cats

    upset.plot(fig=fig)

    filename = f'upset_{tool_name}_{patch_type.lower()}'
    pdf_path = os.path.join(OUTPUT_DIR, f'{filename}.pdf')
    png_path = os.path.join(OUTPUT_DIR, f'{filename}.png')
    plt.savefig(pdf_path, dpi=300, bbox_inches='tight')
    plt.savefig(png_path, dpi=300, bbox_inches='tight')
    plt.close()

    print(f"Saved: {pdf_path}")
    print(f"Saved: {png_path}")




def get_venn_data_both_baseline(reference_tools):
    """
    Get lists of baseline pre-uids matched by each reference tool.
    Uses lists (not sets) to preserve duplicates.
    Correctness is determined by the selected tool's patch, not the baseline patch.
    
    Algorithm:
    1. Load baseline tool_patches (from TMP_DEDUPLICATED_TOOL_PATHCES_PKL)
    2. For each reference tool, load {tool}_sourcerercc_labels.pkl
       - This has columns: uid, pre-uid, sourcerercc_label
    3. For each tool, get selected_tool_patches to know correctness of each uid
    4. For rows where sourcerercc_label == True, classify the pre-uid as correct/overfitting
       based on the selected tool's uid correctness
    5. The UpSet diagram shows intersections of these pre-uid lists across tools
    """
    # Load baseline tool patches (just for the full set of pre-uids)
    tool_patches = pd.read_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)
    all_pre_uids = list(tool_patches.index)
    
    print(f"Baseline tool patches: Total={len(all_pre_uids)}")
    
    correct_match_sets = {}
    overfitting_match_sets = {}
    all_correct_pre_uids = set()
    all_overfitting_pre_uids = set()
    
    for ref_tool in reference_tools:
        baseline_labels_path = os.path.join(RQ5_META_DATA_DIR, f"{ref_tool}_sourcerercc_labels.pkl")
        if not os.path.exists(baseline_labels_path):
            raise FileNotFoundError(f"Missing file: {baseline_labels_path}")
        
        results_df = pd.read_pickle(baseline_labels_path)
        
        # Get selected tool patches to know correctness of each uid
        selected_tool_patches = get_selected_tool_patches(ref_tool).copy()
        
        correct_selected = set(selected_tool_patches[selected_tool_patches['correctness'] == 'Correct'].index)
        overfitting_selected = set(selected_tool_patches[selected_tool_patches['correctness'] == 'Overfitting'].index)
        
        # Filter to matched rows only
        matched_rows = results_df[results_df['sourcerercc_label'] == True]
        
        print(f"  {ref_tool}: total matched rows = {len(matched_rows)}")
        
        # Split pre-uids based on the selected tool's uid correctness
        correct_matched = []
        overfitting_matched = []
        
        for _, row in matched_rows.iterrows():
            uid = row['uid']
            pre_uid = row['pre-uid']
            
            if uid in correct_selected:
                correct_matched.append(pre_uid)
                all_correct_pre_uids.add(pre_uid)
            elif uid in overfitting_selected:
                overfitting_matched.append(pre_uid)
                all_overfitting_pre_uids.add(pre_uid)
        
        correct_match_sets[ref_tool] = correct_matched
        overfitting_match_sets[ref_tool] = overfitting_matched
        
        print(f"  {ref_tool}: matched pre-uids "
              f"(correct={len(correct_matched)}, overfitting={len(overfitting_matched)})")
    
    # all_uids for the plot = all pre-uids that were matched by at least one tool (per correctness)
    # Use the full baseline set so degree-0 is visible if needed
    all_correct_uids = list(all_correct_pre_uids)
    all_overfitting_uids = list(all_overfitting_pre_uids)
    
    return (correct_match_sets, overfitting_match_sets, 
            all_correct_uids, all_overfitting_uids)



def analyze_and_plot(tool_name, reference_tools):
    """
    Analyze a tool and create UpSet diagrams for both correct and overfitting patches.
    If tool_name is 'baseline', uses pre-match labels against baseline tool patches.
    """
    print(f"\n{'='*60}")
    print(f"Diagram Analysis for {tool_name.upper()}")
    print(f"Reference tools: {', '.join(reference_tools)}")
    print(f"Output directory: {os.path.abspath(OUTPUT_DIR)}")
    print(f"{'='*60}")
    
    if tool_name == "baseline":
        (correct_match_sets, overfitting_match_sets, 
         all_correct_uids, all_overfitting_uids) = get_venn_data_both_baseline(reference_tools)
    else:
        (correct_match_sets, overfitting_match_sets, 
         all_correct_uids, all_overfitting_uids) = get_venn_data_both(tool_name, reference_tools)
    
    # --- CORRECT PATCHES ---
    print(f"\n--- CORRECT PATCHES ---")
    print(f"Total correct patches: {len(all_correct_uids)}")
    
    if len(all_correct_uids) > 0:
        print("Matches per reference tool:")
        for ref_tool in reference_tools:
            print(f"  {ref_tool}: {len(correct_match_sets[ref_tool])}")
        all_correct_matched = []
        for v in correct_match_sets.values():
            all_correct_matched.extend(v)
        total_correct_matched = len(all_correct_matched)
        unique_correct_matched = len(set(all_correct_matched))
        print(f"Total matches (with duplicates): {total_correct_matched}")
        print(f"Total unique matches: {unique_correct_matched}")
        print(f"Novel (no match): {len(all_correct_uids) - unique_correct_matched}")
        
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
        all_overfitting_matched = []
        for v in overfitting_match_sets.values():
            all_overfitting_matched.extend(v)
        total_overfitting_matched = len(all_overfitting_matched)
        unique_overfitting_matched = len(set(all_overfitting_matched))
        print(f"Total matches (with duplicates): {total_overfitting_matched}")
        print(f"Total unique matches: {unique_overfitting_matched}")
        print(f"Novel (no match): {len(all_overfitting_uids) - unique_overfitting_matched}")
        
        plot_upset(overfitting_match_sets, all_overfitting_uids, tool_name, 'Overfitting', reference_tools)
    else:
        print("No overfitting patches - skipping diagram.")


if __name__ == "__main__":
    # Baseline (pre) as base, all tools including T5APR as reference tools
    analyze_and_plot("baseline", ["arjae", "recoder", "selfapr", "knod", "tare", "transplantfix", "t5apr"])
























    














"""
UpSet diagram with 8 groups: baseline + 7 reference tools.
Baseline is treated as a category (not just the universe).
Items are baseline pre-uids.
"""

import warnings
warnings.filterwarnings('ignore')

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
import os

from upsetplot import UpSet, from_contents

from utils.config import *
from utils.benchmark import *
from utils.utils import *
from utils.tool import *
from utils.dataset import *
from rq5_sourcerercc import get_selected_tool_patches

OUTPUT_DIR = "rq5"
os.makedirs(OUTPUT_DIR, exist_ok=True)


def get_venn_data_8groups(reference_tools):
    """
    8-group version: baseline + reference tools are ALL treated as categories.
    
    Items are baseline pre-uids.
    - Baseline group = all baseline pre-uids (split by baseline's own correctness).
    - Each reference tool group = baseline pre-uids that SourcererCC-matched to that
      tool's patches (split by baseline's own correctness, so the correct/overfitting
      diagrams are consistent).
    
    Returns:
        correct_match_sets: dict[tool_name -> list of pre-uids] (includes 'baseline')
        overfitting_match_sets: same
        all_correct_uids: list of all correct baseline pre-uids
        all_overfitting_uids: list of all overfitting baseline pre-uids
    """
    # Load baseline tool patches
    tool_patches = pd.read_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)
    
    baseline_correct_set = set(tool_patches[tool_patches['correctness'] == 'Correct'].index)
    baseline_overfitting_set = set(tool_patches[tool_patches['correctness'] == 'Overfitting'].index)
    
    print(f"Baseline tool patches: Correct={len(baseline_correct_set)}, "
          f"Overfitting={len(baseline_overfitting_set)}, "
          f"Total={len(tool_patches)}")
    
    # Baseline is a group that contains ALL its own pre-uids
    correct_match_sets = {'baseline': list(baseline_correct_set)}
    overfitting_match_sets = {'baseline': list(baseline_overfitting_set)}
    
    for ref_tool in reference_tools:
        baseline_labels_path = os.path.join(RQ5_META_DATA_DIR, f"{ref_tool}_sourcerercc_labels.pkl")
        if not os.path.exists(baseline_labels_path):
            raise FileNotFoundError(f"Missing file: {baseline_labels_path}")
        
        results_df = pd.read_pickle(baseline_labels_path)
        
        # Filter to matched rows only
        matched_rows = results_df[results_df['sourcerercc_label'] == True]
        matched_pre_uids = set(matched_rows['pre-uid'])
        
        # Split by BASELINE's correctness (not the reference tool's)
        correct_matched = [uid for uid in matched_pre_uids if uid in baseline_correct_set]
        overfitting_matched = [uid for uid in matched_pre_uids if uid in baseline_overfitting_set]
        
        correct_match_sets[ref_tool] = correct_matched
        overfitting_match_sets[ref_tool] = overfitting_matched
        
        print(f"  {ref_tool}: matched pre-uids "
              f"(correct={len(correct_matched)}, overfitting={len(overfitting_matched)})")
    
    return (correct_match_sets, overfitting_match_sets,
            list(baseline_correct_set), list(baseline_overfitting_set))


def plot_upset_8groups(match_sets, all_uids, tool_name, patch_type, all_tools):
    """
    Plot UpSet diagram with 8 categories (baseline + 7 tools).
    match_sets keys include 'baseline' plus the reference tool names.
    """
    display_names = {
        'baseline': 'Baseline',
        'arjae': 'ARJA-e (2020)',
        'recoder': 'Recoder (2021)',
        'selfapr': 'SelfAPR (2022)',
        'knod': 'Knod (2023)',
        'tare': 'TARE (2023)',
        'transplantfix': 'TransplantFix (2023)',
        't5apr': 'T5APR (2024)',
    }

    # Category order: bottom to top in the UpSet matrix
    category_order = [
        'T5APR (2024)',
        'TransplantFix (2023)',
        'TARE (2023)',
        'Knod (2023)',
        'SelfAPR (2022)',
        'Recoder (2021)',
        'ARJA-e (2020)',
        'Baseline',
    ]

    # Build data dict with deduplication (same logic as your original code)
    data = {}
    for tool_key in all_tools:
        display_name = display_names.get(tool_key, tool_key.upper())
        uid_list = match_sets[tool_key]
        unique_list = []
        counts = {}
        for uid in uid_list:
            if uid not in counts:
                counts[uid] = 0
            else:
                counts[uid] += 1
            unique_list.append(f"{uid}__dup{counts[uid]}")
        data[display_name] = unique_list

    upset_data = from_contents(data)

    # Filter category_order to only include tools actually present
    ordered_cats = [c for c in category_order if c in upset_data.index.names]

    fig = plt.figure(figsize=(18, 7))

    upset = UpSet(upset_data,
                  subset_size='count',
                  show_counts=True,
                  show_percentages=False,
                  sort_by='degree',
                  sort_categories_by=None,
                  facecolor='black',
                  element_size=46)

    upset._category_order = ordered_cats

    upset.plot(fig=fig)

    filename = f'upset_8groups_{tool_name}_{patch_type.lower()}'
    pdf_path = os.path.join(OUTPUT_DIR, f'{filename}.pdf')
    png_path = os.path.join(OUTPUT_DIR, f'{filename}.png')
    plt.savefig(pdf_path, dpi=300, bbox_inches='tight')
    plt.savefig(png_path, dpi=300, bbox_inches='tight')
    plt.close()

    print(f"Saved: {pdf_path}")
    print(f"Saved: {png_path}")


def analyze_and_plot_8groups(reference_tools):
    """
    Create UpSet diagrams with baseline as an 8th group alongside reference tools.
    """
    all_tools = ['baseline'] + reference_tools  # 8 groups total

    print(f"\n{'='*60}")
    print(f"8-Group UpSet Diagram Analysis")
    print(f"Groups: {', '.join(all_tools)}")
    print(f"Output directory: {os.path.abspath(OUTPUT_DIR)}")
    print(f"{'='*60}")

    (correct_match_sets, overfitting_match_sets,
     all_correct_uids, all_overfitting_uids) = get_venn_data_8groups(reference_tools)

    # --- CORRECT PATCHES ---
    print(f"\n--- CORRECT PATCHES ---")
    print(f"Total correct baseline patches: {len(all_correct_uids)}")

    if len(all_correct_uids) > 0:
        print("Matches per group:")
        for tool_key in all_tools:
            print(f"  {tool_key}: {len(correct_match_sets[tool_key])}")

        all_correct_matched = []
        for k, v in correct_match_sets.items():
            if k != 'baseline':
                all_correct_matched.extend(v)
        unique_correct_matched = len(set(all_correct_matched))
        print(f"Total unique matches (excl. baseline): {unique_correct_matched}")
        print(f"Novel (only in baseline): {len(all_correct_uids) - unique_correct_matched}")

        plot_upset_8groups(correct_match_sets, all_correct_uids, 'baseline', 'Correct', all_tools)
    else:
        print("No correct patches - skipping diagram.")

    # --- OVERFITTING PATCHES ---
    print(f"\n--- OVERFITTING PATCHES ---")
    print(f"Total overfitting baseline patches: {len(all_overfitting_uids)}")

    if len(all_overfitting_uids) > 0:
        print("Matches per group:")
        for tool_key in all_tools:
            print(f"  {tool_key}: {len(overfitting_match_sets[tool_key])}")

        all_overfitting_matched = []
        for k, v in overfitting_match_sets.items():
            if k != 'baseline':
                all_overfitting_matched.extend(v)
        unique_overfitting_matched = len(set(all_overfitting_matched))
        print(f"Total unique matches (excl. baseline): {unique_overfitting_matched}")
        print(f"Novel (only in baseline): {len(all_overfitting_uids) - unique_overfitting_matched}")

        plot_upset_8groups(overfitting_match_sets, all_overfitting_uids, 'baseline', 'Overfitting', all_tools)
    else:
        print("No overfitting patches - skipping diagram.")


if __name__ == "__main__":
    analyze_and_plot_8groups(
        ["arjae", "recoder", "selfapr", "knod", "tare", "transplantfix", "t5apr"]
    )