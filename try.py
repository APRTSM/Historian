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

# Additional checks for the combination of uid and groundtruth_index/groundtruth_patch_uid
print("\n" + "="*50)
print("CHECKING UID-GROUNDTRUTH COMBINATIONS")
print("="*50)

# For first dataframe - check if it has groundtruth_patch_uid column
if 'groundtruth_patch_uid' in pairs1.columns:
    combinations1 = set(zip(pairs1.index, pairs1['groundtruth_patch_uid']))
    print("First dataframe uses 'groundtruth_patch_uid' column")
else:
    print("First dataframe column structure:")
    print(pairs1.columns.tolist())

# For second dataframe - check if it has groundtruth_index column  
if 'groundtruth_index' in pairs2.columns:
    combinations2 = set(zip(pairs2['uid'], pairs2['groundtruth_index']))
    print("Second dataframe uses 'groundtruth_index' column")
    print(f"Number of unique combinations in second dataframe: {len(combinations2)}")
else:
    print("Second dataframe column structure:")
    print(pairs2.columns.tolist())

# If both have the relevant columns, compare combinations
if ('groundtruth_patch_uid' in pairs1.columns or 'groundtruth_index' in pairs1.columns) and 'groundtruth_index' in pairs2.columns:
    # Create comparable combinations
    if 'groundtruth_patch_uid' in pairs1.columns:
        combinations1 = set(zip(pairs1.index, pairs1['groundtruth_patch_uid']))
    else:
        combinations1 = set(zip(pairs1.index, pairs1['groundtruth_index']))
    
    combinations2 = set(zip(pairs2['uid'], pairs2['groundtruth_index']))
    
    print(f"\nNumber of unique (uid, groundtruth) combinations:")
    print(f"First dataframe: {len(combinations1)}")
    print(f"Second dataframe: {len(combinations2)}")
    
    if combinations1 == combinations2:
        print("✅ The dataframes have identical (uid, groundtruth) combinations!")
    else:
        print("❌ The dataframes have different (uid, groundtruth) combinations")
        
        only_in_first_combo = combinations1 - combinations2
        only_in_second_combo = combinations2 - combinations1
        
        if only_in_first_combo:
            print(f"\nCombinations only in first: {len(only_in_first_combo)}")
            for combo in sorted(list(only_in_first_combo)[:5]):
                print(f"  {combo}")
        
        if only_in_second_combo:
            print(f"\nCombinations only in second: {len(only_in_second_combo)}")
            for combo in sorted(list(only_in_second_combo)[:5]):
                print(f"  {combo}")