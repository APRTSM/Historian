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
orig_uid = pairs2['uid'].copy()
pairs2['uid'] = pairs2['groundtruth_index']
pairs2['groundtruth_index'] = orig_uid

print(pairs1)
print(pairs2)

print("First dataframe shape:", pairs1.shape)
print("Second dataframe shape:", pairs2.shape)
print()

# Check if they have the same UIDs
uids1 = set(pairs1.index) if hasattr(pairs1, 'index') else set(pairs1['uid']) if 'uid' in pairs1.columns else set()
uids2 = set(pairs2['uid']) if 'uid' in pairs2.columns else set(pairs2.index)

print("Number of unique UIDs in first dataframe:", len(uids1))
print("Number of unique UIDs in second dataframe:", len(uids2))
print()

# Check if they're the same
if uids1 == uids2:
    print("✅ The dataframes have identical sets of UIDs!")
else:
    print("❌ The dataframes have different sets of UIDs")
    
    # Find differences
    only_in_first = uids1 - uids2
    only_in_second = uids2 - uids1
    
    if only_in_first:
        print(f"\n🔍 UIDs only in first dataframe ({len(only_in_first)}):")
        for uid in sorted(list(only_in_first)[:10]):  # Show first 10
            print(f"  - {uid}")
        if len(only_in_first) > 10:
            print(f"  ... and {len(only_in_first) - 10} more")
    
    if only_in_second:
        print(f"\n🔍 UIDs only in second dataframe ({len(only_in_second)}):")
        for uid in sorted(list(only_in_second)[:10]):  # Show first 10
            print(f"  - {uid}")
        if len(only_in_second) > 10:
            print(f"  ... and {len(only_in_second) - 10} more")
    
    # Show intersection
    common_uids = uids1.intersection(uids2)
    print(f"\n📊 Common UIDs: {len(common_uids)}")


















# After your existing code, add:

print("\n" + "="*80)
print("CHECKING COMBINATIONS")
print("="*80)

# Get the combinations from both dataframes
# First dataframe: index (uid) + tool_patch_uid column
combo1_uid = pairs1.index.tolist()
combo1_tool = pairs1['tool_patch_uid'].tolist()

# Second dataframe: uid + groundtruth_index columns
combo2_uid = pairs2['uid'].tolist()
combo2_groundtruth = pairs2['groundtruth_index'].tolist()

# Check if lengths match
print(f"\nLength of first dataframe: {len(combo1_uid)}")
print(f"Length of second dataframe: {len(combo2_uid)}")

# Check if combinations match in order
matches = 0
mismatches = 0
mismatch_details = []

for i in range(min(len(combo1_uid), len(combo2_uid))):
    if combo1_uid[i] == combo2_uid[i] and combo1_tool[i] == combo2_groundtruth[i]:
        matches += 1
    else:
        mismatches += 1
        if len(mismatch_details) < 10:  # Store first 10 mismatches
            mismatch_details.append({
                'row': i,
                'df1_uid': combo1_uid[i],
                'df1_tool': combo1_tool[i],
                'df2_uid': combo2_uid[i],
                'df2_groundtruth': combo2_groundtruth[i]
            })

print(f"\n📊 RESULTS:")
print(f"✅ Matching rows: {matches}")
print(f"❌ Mismatching rows: {mismatches}")

if mismatches == 0:
    print("\n🎉 Perfect match! All combinations are identical and in the same order!")
else:
    print(f"\n⚠️  Found {mismatches} mismatches")
    print("\n🔍 First few mismatches:")
    for detail in mismatch_details:
        print(f"\n  Row {detail['row']}:")
        print(f"    DF1: uid='{detail['df1_uid']}' + tool_patch_uid='{detail['df1_tool']}'")
        print(f"    DF2: uid='{detail['df2_uid']}' + groundtruth_index='{detail['df2_groundtruth']}'")

# Also create a quick comparison dataframe
print("\n📋 Creating comparison dataframe...")
comparison_df = pd.DataFrame({
    'df1_uid': combo1_uid,
    'df1_tool_patch_uid': combo1_tool,
    'df2_uid': combo2_uid,
    'df2_groundtruth_index': combo2_groundtruth,
    'uid_match': [u1 == u2 for u1, u2 in zip(combo1_uid, combo2_uid)],
    'tool_match': [t1 == t2 for t1, t2 in zip(combo1_tool, combo2_groundtruth)]
})

comparison_df['both_match'] = comparison_df['uid_match'] & comparison_df['tool_match']

print(f"\nComparison summary:")
print(f"  Both match: {comparison_df['both_match'].sum()} / {len(comparison_df)}")
print(f"  UID matches: {comparison_df['uid_match'].sum()} / {len(comparison_df)}")
print(f"  Tool/Groundtruth matches: {comparison_df['tool_match'].sum()} / {len(comparison_df)}")

# Show first few rows of comparison
print("\n📝 First 10 rows of comparison:")
print(comparison_df.head(10).to_string())

# If you want to see the mismatches specifically
if mismatches > 0:
    print("\n❌ Rows where combinations don't match:")
    print(comparison_df[~comparison_df['both_match']].head(10).to_string())