import pandas as pd
import os

import pandas as pd
import logging
import pandas as pd
from utils.config import *
from utils.benchmark import *
from utils.utils import *
from utils.tool import *
from utils.dataset import *
from build import init, clean_patches, get_methods, get_patch_processors, get_tool_settings, normalaize_names, deduplicate_patches



# Assuming you have the TMP_RESULTS_DIR defined
# TMP_RESULTS_DIR = "your_tmp_results_directory_path"

# Load the two pickle files
pairs1 = pd.read_pickle(os.path.join(TMP_RESULTS_DIR, "EXP2-tbar-method-hermes3:8b-0.1-llm4cc-similarity_line-patch-identical.pkl"))
pairs2 = pd.read_pickle(os.path.join(TMP_RESULTS_DIR, "expert", "EXP2-labeled-tbar.pkl"))

print("Columns in first dataframe:", pairs1.columns.tolist())
print("Columns in second dataframe:", pairs2.columns.tolist())
print()

print("First few rows of each dataframe:")
print(pairs1.head())
print("\n")
print(pairs2.head())
print("\n")

print("First dataframe shape:", pairs1.shape)
print("Second dataframe shape:", pairs2.shape)
print()

# ============================================================================
# COMPARISON 1: First columns - tool_patch_uid vs uid
# ============================================================================
print("="*70)
print("COMPARING FIRST COLUMNS: tool_patch_uid vs uid")
print("="*70)

tool_patch_uids1 = set(pairs1['tool_patch_uid'])
uids2 = set(pairs2['uid'])

print(f"Number of unique tool_patch_uids in first dataframe: {len(tool_patch_uids1)}")
print(f"Number of unique uids in second dataframe: {len(uids2)}")
print()

if tool_patch_uids1 == uids2:
    print("✅ The first columns have identical values!")
else:
    print("❌ The first columns have different values")
    
    only_in_first = tool_patch_uids1 - uids2
    only_in_second = uids2 - tool_patch_uids1
    
    if only_in_first:
        print(f"\n🔍 Values only in first dataframe tool_patch_uid ({len(only_in_first)}):")
        for uid in sorted(list(only_in_first)[:10]):
            print(f"  - {uid}")
        if len(only_in_first) > 10:
            print(f"  ... and {len(only_in_first) - 10} more")
    
    if only_in_second:
        print(f"\n🔍 Values only in second dataframe uid ({len(only_in_second)}):")
        for uid in sorted(list(only_in_second)[:10]):
            print(f"  - {uid}")
        if len(only_in_second) > 10:
            print(f"  ... and {len(only_in_second) - 10} more")
    
    common = tool_patch_uids1.intersection(uids2)
    print(f"\n📊 Common values: {len(common)}")

# ============================================================================
# COMPARISON 2: Second columns - groundtruth_patch_uid vs groundtruth_index
# ============================================================================
print("\n" + "="*70)
print("COMPARING SECOND COLUMNS: groundtruth_patch_uid vs groundtruth_index")
print("="*70)

groundtruth_uids1 = set(pairs1['groundtruth_patch_uid'])
groundtruth_index2 = set(pairs2['groundtruth_index'])

print(f"Number of unique groundtruth_patch_uid in first dataframe: {len(groundtruth_uids1)}")
print(f"Number of unique groundtruth_index in second dataframe: {len(groundtruth_index2)}")
print()

if groundtruth_uids1 == groundtruth_index2:
    print("✅ The second columns have identical values!")
else:
    print("❌ The second columns have different values")
    
    only_in_first_gt = groundtruth_uids1 - groundtruth_index2
    only_in_second_gt = groundtruth_index2 - groundtruth_uids1
    
    if only_in_first_gt:
        print(f"\n🔍 Values only in first dataframe groundtruth_patch_uid ({len(only_in_first_gt)}):")
        for uid in sorted(list(only_in_first_gt)[:10]):
            print(f"  - {uid}")
        if len(only_in_first_gt) > 10:
            print(f"  ... and {len(only_in_first_gt) - 10} more")
    
    if only_in_second_gt:
        print(f"\n🔍 Values only in second dataframe groundtruth_index ({len(only_in_second_gt)}):")
        for uid in sorted(list(only_in_second_gt)[:10]):
            print(f"  - {uid}")
        if len(only_in_second_gt) > 10:
            print(f"  ... and {len(only_in_second_gt) - 10} more")
    
    common_gt = groundtruth_uids1.intersection(groundtruth_index2)
    print(f"\n📊 Common values: {len(common_gt)}")

# ============================================================================
# COMPARISON 3: Combinations (as sets - order doesn't matter)
# ============================================================================
print("\n" + "="*70)
print("COMPARING COMBINATIONS (as sets): (tool_patch_uid, groundtruth_patch_uid) vs (uid, groundtruth_index)")
print("="*70)

combinations1 = set(zip(pairs1['tool_patch_uid'], pairs1['groundtruth_patch_uid']))
combinations2 = set(zip(pairs2['uid'], pairs2['groundtruth_index']))

print(f"Number of unique combinations in first dataframe: {len(combinations1)}")
print(f"Number of unique combinations in second dataframe: {len(combinations2)}")
print()

if combinations1 == combinations2:
    print("✅ The dataframes have identical combinations!")
else:
    print("❌ The dataframes have different combinations")
    
    only_in_first_combo = combinations1 - combinations2
    only_in_second_combo = combinations2 - combinations1
    
    if only_in_first_combo:
        print(f"\nCombinations only in first dataframe ({len(only_in_first_combo)}):")
        for combo in sorted(list(only_in_first_combo)[:5]):
            print(f"  {combo}")
        if len(only_in_first_combo) > 5:
            print(f"  ... and {len(only_in_first_combo) - 5} more")
    
    if only_in_second_combo:
        print(f"\nCombinations only in second dataframe ({len(only_in_second_combo)}):")
        for combo in sorted(list(only_in_second_combo)[:5]):
            print(f"  {combo}")
        if len(only_in_second_combo) > 5:
            print(f"  ... and {len(only_in_second_combo) - 5} more")
    
    common_combos = combinations1.intersection(combinations2)
    print(f"\n📊 Common combinations: {len(common_combos)}")

# ============================================================================
# COMPARISON 4: Ordered combinations (as lists - order matters)
# ============================================================================
print("\n" + "="*70)
print("COMPARING ORDERED COMBINATIONS: Same combinations in same order")
print("="*70)

# Create lists of tuples preserving order
ordered_combos1 = list(zip(pairs1['tool_patch_uid'], pairs1['groundtruth_patch_uid']))
ordered_combos2 = list(zip(pairs2['uid'], pairs2['groundtruth_index']))

print(f"Number of rows in first dataframe: {len(ordered_combos1)}")
print(f"Number of rows in second dataframe: {len(ordered_combos2)}")
print()

# Check if they have the same length first
if len(ordered_combos1) != len(ordered_combos2):
    print(f"❌ Different number of rows: {len(ordered_combos1)} vs {len(ordered_combos2)}")
else:
    print(f"✅ Same number of rows: {len(ordered_combos1)}")
    
    # Check if all combinations match in order
    matches = sum(1 for c1, c2 in zip(ordered_combos1, ordered_combos2) if c1 == c2)
    mismatches = len(ordered_combos1) - matches
    
    if matches == len(ordered_combos1):
        print("✅ All combinations match in the same order!")
    else:
        print(f"❌ Combinations differ in order")
        print(f"   Matching rows: {matches} ({100*matches/len(ordered_combos1):.1f}%)")
        print(f"   Mismatching rows: {mismatches} ({100*mismatches/len(ordered_combos1):.1f}%)")
        
        # Show first few mismatches
        print("\n🔍 First 10 mismatches:")
        mismatch_count = 0
        for i, (c1, c2) in enumerate(zip(ordered_combos1, ordered_combos2)):
            if c1 != c2:
                print(f"  Row {i}:")
                print(f"    First:  {c1}")
                print(f"    Second: {c2}")
                mismatch_count += 1
                if mismatch_count >= 10:
                    break

# ============================================================================
# SUMMARY
# ============================================================================
print("\n" + "="*70)
print("SUMMARY")
print("="*70)
print(f"First dataframe: {pairs1.shape[0]} rows, {pairs1.shape[1]} columns")
print(f"Second dataframe: {pairs2.shape[0]} rows, {pairs2.shape[1]} columns")
print()
print("Overlap statistics:")
print(f"  - First columns (tool_patch_uid vs uid): {len(tool_patch_uids1.intersection(uids2))} in common")
print(f"  - Second columns (groundtruth_patch_uid vs groundtruth_index): {len(groundtruth_uids1.intersection(groundtruth_index2))} in common")
print(f"  - Unique combinations in common: {len(combinations1.intersection(combinations2))}")
if len(ordered_combos1) == len(ordered_combos2):
    matches = sum(1 for c1, c2 in zip(ordered_combos1, ordered_combos2) if c1 == c2)
    print(f"  - Ordered rows matching: {matches} out of {len(ordered_combos1)} ({100*matches/len(ordered_combos1):.1f}%)")