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

"""
run after rq4_apply.py
Gathers files from different projects in rq4/tool_patches with correctness labels
and creates a single dataframe and saves it to rq4/patches_metadata/transplantfix_patches.pkl
"""

# Configure Benchmarks, Get Initial Data (Bugs, Developer Patches, Tool Patches)
def init(configure=True):
    logging.info("Fetching the initial data.")

    if configure:
        configure_benchmarks()

    if os.path.exists(TMP_SETTINGS_FILE):
        logging.info("Using the data in TMP ...")

        bugs = pd.read_pickle(TMP_BUGS_PKL)
        developer_patches = pd.read_pickle(TMP_DEVELOPER_PATHCES_PKL)
        tool_patches = pd.read_pickle(TMP_TOOL_PATHCES_PKL)

    else:
        logging.info("Fetching initial data ...")

        bugs_list = get_bugs()
        bugs = pd.DataFrame(bugs_list).set_index("uid")
        bugs.to_pickle(TMP_BUGS_PKL)

        developer_patches = pd.DataFrame(get_developer_patches(bugs_list)).set_index("uid")
        developer_patches.to_pickle(TMP_DEVELOPER_PATHCES_PKL)

        tool_patches = pd.DataFrame(get_patches(bugs_list)).set_index("uid")
        tool_patches.to_pickle(TMP_TOOL_PATHCES_PKL)

        if not os.path.exists(TMP_META_DATA):
            os.mkdir(TMP_META_DATA)

        shutil.copy(BENCHMARKS_JSON, TMP_BENCHMARKS_JSON)
        shutil.copy(DATASETS_JSON, TMP_DATASETS_JSON)
        shutil.copy(TOOLS_JSON, TMP_TOOLS_JSON)
        shutil.copy(SETTINGS_FILE, TMP_SETTINGS_FILE)

    logging.info(f"Successfully fetched the initial data.")
    bugs_statistics = bugs.groupby('benchmark').size().reset_index(name="#Bugs")
    developer_patches_statistics = developer_patches.groupby('origin').size().reset_index(name="#DeveloperPatches")
    tool_patches_statistics = tool_patches.groupby(['origin', 'correctness']).size().unstack(fill_value=0)
    logging.info(f"Bugs:\n {bugs_statistics}")
    logging.info(f"Developer Patches:\n {developer_patches_statistics}")
    logging.info(f"Tool Patches:\n {tool_patches_statistics}")

    return bugs, developer_patches, tool_patches

def iterate_patches_circle(bugs):
    tool = "circle"
    tool_path = os.path.join(RQ4_DATA_DIR, tool)
    index = 0
    patches_list = []

    # Store Closure-63 and Closure-93 files to process at the end
    deferred_files = []
    
    for filename in os.listdir(tool_path):
        filepath = os.path.join(tool_path, filename)
        
        # Check if ends with .txt and skip non-patch files
        if not filename.endswith(".txt") or filename == "d4j_patches.txt":
            logging.info(f"Skipping file: {filepath}")
            continue
        
        # Defer Closure-63 and Closure-93 to process at the end
        if "Closure-63" in filename or "Closure-93" in filename:
            deferred_files.append((filename, filepath))
            continue
        
        # Set bug
        bug = bugs.loc[f"defects4j-{filename.split('.')[0]}"].copy()
        bug['uid'] = bug.name
        
        # Set uid
        uid = f"historian-{bug.name}-{tool}-{index}"
        
        # ...
        # first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")
        
        location = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

        if not os.path.exists(location):  
            raise FileNotFoundError(f"Patch file not found: {location}")

        correctness = "Correct"

        patch_dict = {
            "uid": uid,
            "bug_uid": bug.name,
            "generator": tool,
            "location": location,
            "correctness": correctness,
            "generator_id": tool,
            "origin": "Historian",
            "index": index
        }

        patches_list.append(patch_dict)
        # copy_paste(filepath, first_cleaned_location)
        # ...

        index += 1
    
    # Process deferred files (Closure-63 and Closure-93) at the end
    for filename, filepath in deferred_files:
        # Set bug
        bug = bugs.loc[f"defects4j-{filename.split('.')[0]}"].copy()
        bug['uid'] = bug.name
        
        # Set uid
        uid = f"historian-{bug.name}-{tool}-{index}"
        
        # ...
        # first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")
        
        location = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

        if not os.path.exists(location):  
            raise FileNotFoundError(f"Patch file not found: {location}")

        correctness = "Correct"

        patch_dict = {
            "uid": uid,
            "bug_uid": bug.name,
            "generator": tool,
            "location": location,
            "correctness": correctness,
            "generator_id": tool,
            "origin": "Historian",
            "index": index
        }

        patches_list.append(patch_dict)
        # copy_paste(filepath, first_cleaned_location)
        # ...

        index += 1

    patches_df = pd.DataFrame(patches_list).set_index("uid")
    patches_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.pkl"))
    patches_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.html"))

def iterate_patches_alpharepair(bugs):
    tool = "alpharepair"
    tool_path = os.path.join(RQ4_DATA_DIR, tool)

    index = 0

    # Iterate through subdirectories (d4j1-2, d4j1-2-npfl, d4j2-0)
    for subdir in os.listdir(tool_path):
        subdir_path = os.path.join(tool_path, subdir)

        logging.info(f"Processing alpharepair subdirectory: {subdir}")

        for filename in os.listdir(subdir_path):
            filepath = os.path.join(subdir_path, filename)

            # Check if ends with .txt
            if not filename.endswith(".txt"):
                logging.info(f"Skipping file: {filepath}")
                continue

            # Extract bug identifier from filename (e.g., "Chart-1.txt" -> "Chart-1")
            bug_id = filename.split('.')[0]

            # Set bug
            bug = bugs.loc[f"defects4j-{bug_id}"].copy()
            bug['uid'] = bug.name

            # Set uid
            uid = f"historian-{bug.name}-{tool}-{index}"

            # Set locations
            first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

            # Run this once to copy paste the first cleaned patches
            copy_paste(filepath, first_cleaned_location)
            index += 1

def iterate_patches_cure(bugs):
    tool = "cure"
    tool_path = os.path.join(RQ4_DATA_DIR, tool)

    index = 0

    # Iterate through subdirectories 
    for subdir in os.listdir(tool_path):
        subdir_path = os.path.join(tool_path, subdir)

        logging.info(f"Processing cure subdirectory: {subdir}")

        for filename in os.listdir(subdir_path):
            filepath = os.path.join(subdir_path, filename)

            # Check if ends with .txt
            if not filename.endswith(".txt"):
                logging.info(f"Skipping file: {filepath}")
                continue

            # Extract bug identifier from filename
            bug_id = "-".join(filename.split('.')[0].split('_'))

            # Set bug
            bug = bugs.loc[f"defects4j-{bug_id}"].copy()
            bug['uid'] = bug.name

            # Set uid
            uid = f"historian-{bug.name}-{tool}-{index}"

            # Set locations
            first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

            # Run this once to copy paste the first cleaned patches
            copy_paste(filepath, first_cleaned_location)
            index += 1

def iterate_patches_dlfix(bugs):
    tool = "dlfix"
    tool_path = os.path.join(RQ4_DATA_DIR, tool)
    index = 0
    patches_list = []
    
    # Store Closure_63 files to process at the end
    deferred_files = []
    
    # Iterate through subdirectories 
    for subdir in os.listdir(tool_path):
        subdir_path = os.path.join(tool_path, subdir)
        if not os.path.isdir(subdir_path):
            continue
        logging.info(f"Processing dlfix subdirectory: {subdir}")
        
        for filename in os.listdir(subdir_path):
            filepath = os.path.join(subdir_path, filename)
            
            # Check if ends with .txt
            if not filename.endswith(".txt"):
                logging.info(f"Skipping file: {filepath}")
                continue
            
            # Defer Closure_63 to process at the end
            if "Closure_63" in filename:
                deferred_files.append((filename, filepath))
                continue
            
            # Extract bug identifier from filename
            bug_id = "-".join(filename.split('.')[0].split('_'))
            
            # Set bug
            bug = bugs.loc[f"defects4j-{bug_id}"].copy()
            bug['uid'] = bug.name
            
            # Set uid
            uid = f"historian-{bug.name}-{tool}-{index}"
            
            # ...
            # first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")
            
            location = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

            if not os.path.exists(location):  
                raise FileNotFoundError(f"Patch file not found: {location}")

            correctness = "Correct"

            patch_dict = {
                "uid": uid,
                "bug_uid": bug.name,
                "generator": tool,
                "location": location,
                "correctness": correctness,
                "generator_id": tool,
                "origin": "Historian",
                "index": index
            }

            patches_list.append(patch_dict)
            # copy_paste(filepath, first_cleaned_location)
            # ...

            index += 1
    
    # Process deferred files (Closure_63) at the end
    for filename, filepath in deferred_files:
        # Extract bug identifier from filename
        bug_id = "-".join(filename.split('.')[0].split('_'))
        
        # Set bug
        bug = bugs.loc[f"defects4j-{bug_id}"].copy()
        bug['uid'] = bug.name
        
        # Set uid
        uid = f"historian-{bug.name}-{tool}-{index}"
        
        # ...
        # first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")
        
        location = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

        if not os.path.exists(location):  
            raise FileNotFoundError(f"Patch file not found: {location}")

        correctness = "Correct"

        patch_dict = {
            "uid": uid,
            "bug_uid": bug.name,
            "generator": tool,
            "location": location,
            "correctness": correctness,
            "generator_id": tool,
            "origin": "Historian",
            "index": index
        }

        patches_list.append(patch_dict)
        # copy_paste(filepath, first_cleaned_location)
        # ...
        
        index += 1

    patches_df = pd.DataFrame(patches_list).set_index("uid")
    patches_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.pkl"))
    patches_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.html"))

def iterate_patches_fitrepair(bugs):
    tool = "fitrepair"
    tool_path = os.path.join(RQ4_DATA_DIR, tool)

    index = 0

    # Iterate through subdirectories 
    for subdir in os.listdir(tool_path):
        subdir_path = os.path.join(tool_path, subdir)

        logging.info(f"Processing fitrepair subdirectory: {subdir}")

        for filename in os.listdir(subdir_path):
            filepath = os.path.join(subdir_path, filename)

            # Check if ends with .txt
            if not filename.endswith(".txt"):
                logging.info(f"Skipping file: {filepath}")
                continue

            # Extract bug identifier from filename
            bug_id = filename.split('.')[0]

            # Set bug
            bug = bugs.loc[f"defects4j-{bug_id}"].copy()
            bug['uid'] = bug.name

            # Set uid
            uid = f"historian-{bug.name}-{tool}-{index}"

            # Set locations
            first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

            # Run this once to copy paste the first cleaned patches
            copy_paste(filepath, first_cleaned_location)
            index += 1

def iterate_patches_iter(bugs):
    tool = "iter"
    tool_path = os.path.join(RQ4_DATA_DIR, tool)
    index = 0
    patches_list = []
    
    # Store Closure-63 files to process at the end
    deferred_files = []
    
    # Iterate through subdirectories 
    for subdir in os.listdir(tool_path):
        subdir_path = os.path.join(tool_path, subdir)
        logging.info(f"Processing iter subdirectory: {subdir}")
        
        for filename in os.listdir(subdir_path):
            # Extract bug identifier from filename
            project, bug_number = re.match(r"([a-zA-Z]+)(\d+)", subdir).groups()
            bug_id = f"{project}-{bug_number}"
            filepath = os.path.join(subdir_path, filename)
            
            # Check if ends with .txt
            if not filename.endswith(".txt"):
                logging.info(f"Skipping file: {filepath}")
                continue
            
            # Defer Closure-63 to process at the end
            if bug_id == "Closure-63":
                deferred_files.append((filename, filepath, bug_id))
                continue
            
            # Set bug
            bug = bugs.loc[f"defects4j-{bug_id}"].copy()
            bug['uid'] = bug.name
            
            # Set uid
            uid = f"historian-{bug.name}-{tool}-{index}"
            
            # ...
            # first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")
            
            location = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

            # 
            if "historian-defects4j-JacksonCore-6-iter-7.patch" in location or "historian-defects4j-JacksonCore-12-iter-70.patch" in location:
                print(f"Found problematic patch: {location}")
                index += 1

                break

            if not os.path.exists(location):  
                raise FileNotFoundError(f"Patch file not found: {location}")

            correctness = "Correct"

            patch_dict = {
                "uid": uid,
                "bug_uid": bug.name,
                "generator": tool,
                "location": location,
                "correctness": correctness,
                "generator_id": tool,
                "origin": "Historian",
                "index": index
            }

            patches_list.append(patch_dict)
            # copy_paste(filepath, first_cleaned_location)
            # ...

            index += 1
    
    # Process deferred files (Closure-63) at the end
    for filename, filepath, bug_id in deferred_files:
        # Set bug
        bug = bugs.loc[f"defects4j-{bug_id}"].copy()
        bug['uid'] = bug.name
        
        # Set uid
        uid = f"historian-{bug.name}-{tool}-{index}"
        
        # ...
        # first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")
        
        location = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

        if not os.path.exists(location):  
            raise FileNotFoundError(f"Patch file not found: {location}")

        correctness = "Correct"

        patch_dict = {
            "uid": uid,
            "bug_uid": bug.name,
            "generator": tool,
            "location": location,
            "correctness": correctness,
            "generator_id": tool,
            "origin": "Historian",
            "index": index
        }

        patches_list.append(patch_dict)
        # copy_paste(filepath, first_cleaned_location)
        # ...

        index += 1
    
    patches_df = pd.DataFrame(patches_list).set_index("uid")
    patches_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.pkl"))
    patches_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.html"))


def iterate_patches_knod(bugs):
    tool = "knod"
    tool_path = os.path.join(RQ4_DATA_DIR, tool)

    index = 0

    # Iterate through subdirectories 
    for subdir in os.listdir(tool_path):
        subdir_path = os.path.join(tool_path, subdir)

        logging.info(f"Processing knod subdirectory: {subdir}")

        patches_list = []

        for filename in os.listdir(subdir_path):
            filepath = os.path.join(subdir_path, filename)

            # Extract bug identifier from filename
            bug_id = "-".join(filename.split('_')[:2])

            # Set bug
            bug = bugs.loc[f"defects4j-{bug_id}"].copy()
            bug['uid'] = bug.name

            # Set uid
            uid = f"historian-{bug.name}-{tool}-{index}"

            # ...
            # Set locations
            # first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

            correctness = filename.split('_')[2]

            if "correct" in correctness.lower():
                correctness = "Correct"

            else:
                correctness = "Overfitting"

            location = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

            if not os.path.exists(location):  
                raise FileNotFoundError(f"Patch file not found: {location}")
                
            patch_dict = {
                "uid": uid,
                "bug_uid": bug.name,
                "generator": tool,
                "location": location,
                "correctness": correctness,
                "generator_id": tool,
                "origin": "Historian",
                "index": index
            }

            patches_list.append(patch_dict)

            # Run this once to copy paste the first cleaned patches
            # copy_paste(filepath, first_cleaned_location)
            # ...
            index += 1

    
    patches_df = pd.DataFrame(patches_list).set_index("uid")
    patches_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.pkl"))
    patches_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.html"))


def iterate_patches_rapgen(bugs):
    tool = "rapgen"
    tool_path = os.path.join(RQ4_DATA_DIR, tool)

    index = 0

    # Iterate through subdirectories 
    for subdir in os.listdir(tool_path):
        subdir_path = os.path.join(tool_path, subdir)

        logging.info(f"Processing rapgen subdirectory: {subdir}")

        for filename in os.listdir(subdir_path):
            filepath = os.path.join(subdir_path, filename)

            # Extract bug identifier from filename
            bug_id = "-".join(filename.replace("[Uniquely repaired bug] ", "").split('.')[0].split('_')[:2])

            if "defects4j" in bug_id:
                logging.info(f"Skipping file: {filepath}")
                continue

            # Set bug
            bug = bugs.loc[f"defects4j-{bug_id}"].copy()
            bug['uid'] = bug.name

            # Set uid
            uid = f"historian-{bug.name}-{tool}-{index}"

            # Set locations
            first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

            # Run this once to copy paste the first cleaned patches
            copy_paste(filepath, first_cleaned_location)
            index += 1

def iterate_patches_recoder(bugs):
    tool = "recoder"
    tool_path = os.path.join(RQ4_DATA_DIR, tool)
    index = 0
    patches_list = []
    
    # Store closure-63 and closure-93 files to process at the end
    deferred_files = []
    
    for filename in os.listdir(tool_path):
        filepath = os.path.join(tool_path, filename)
        
        # Skip non-patch files
        if "patch_ground" in filename or "out" in filename or "defect4j2" in filename:
            logging.info(f"Skipping file: {filepath}")
            continue
        
        # Defer closure-63 and closure-93 to process at the end
        if "closure-63" in filename or "closure-93" in filename:
            deferred_files.append((filename, filepath))
            continue
        
        # Extract bug identifier from filename
        bug_id = filename.split('.')[0]
        bug_id = bug_id[0].upper() + bug_id[1:]
        
        # Set bug
        bug = bugs.loc[f"defects4j-{bug_id}"].copy()
        bug['uid'] = bug.name
        
        # Set uid
        uid = f"historian-{bug.name}-{tool}-{index}"
        
        # ...
        # first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

        location = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

        if not os.path.exists(location):  
            raise FileNotFoundError(f"Patch file not found: {location}")

        correctness = "Correct"

        if bug.name in ["defects4j-Closure-7", "defects4j-Closure-109", "defects4j-Chart-7", "defects4j-Lang-7", "defects4j-Lang-63"]:
            correctness = "Overfitting"

        patch_dict = {
            "uid": uid,
            "bug_uid": bug.name,
            "generator": tool,
            "location": location,
            "correctness": correctness,
            "generator_id": tool,
            "origin": "Historian",
            "index": index
        }

        patches_list.append(patch_dict)
        # copy_paste(filepath, first_cleaned_location)
        # ...

        index += 1
    
    # Process deferred files (closure-63 and closure-93) at the end
    for filename, filepath in deferred_files:
        # Extract bug identifier from filename
        bug_id = filename.split('.')[0]
        bug_id = bug_id[0].upper() + bug_id[1:]
        
        # Set bug
        bug = bugs.loc[f"defects4j-{bug_id}"].copy()
        bug['uid'] = bug.name
        
        # Set uid
        uid = f"historian-{bug.name}-{tool}-{index}"
        
        location = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

        if not os.path.exists(location):  
            raise FileNotFoundError(f"Patch file not found: {location}")

        correctness = "Correct"

        if bug.name in ["defects4j-Closure-7", "defects4j-Closure-109", "defects4j-Chart-7", "defects4j-Lang-7", "defects4j-Lang-63"]:
            correctness = "Overfitting"

        patch_dict = {
            "uid": uid,
            "bug_uid": bug.name,
            "generator": tool,
            "location": location,
            "correctness": correctness,
            "generator_id": tool,
            "origin": "Historian",
            "index": index
        }

        patches_list.append(patch_dict)
        
        index += 1
    
    patches_df = pd.DataFrame(patches_list).set_index("uid")
    patches_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.pkl"))
    patches_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.html"))

def iterate_patches_repilot(bugs):
    tool = "repilot"
    tool_path = os.path.join(RQ4_DATA_DIR, tool)

    index = 0

    # Iterate through subdirectories 
    for subdir in os.listdir(tool_path):
        subdir_path = os.path.join(tool_path, subdir)

        logging.info(f"Processing repilot subdirectory: {subdir}")

        for filename in os.listdir(subdir_path):
            filepath = os.path.join(subdir_path, filename)

            # Extract bug identifier from filename
            bug_id = filename.split('.')[0]

            # Set bug
            bug = bugs.loc[f"defects4j-{bug_id}"].copy()
            bug['uid'] = bug.name

            # Set uid
            uid = f"historian-{bug.name}-{tool}-{index}"

            # Set locations
            first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

            # Run this once to copy paste the first cleaned patches
            copy_paste(filepath, first_cleaned_location)
            index += 1

def iterate_patches_tare(bugs):
    tool = "tare"
    tool_path = os.path.join(RQ4_DATA_DIR, tool)
    index = 0
    patches_list = []

    # Iterate through subdirectories 
    for subdir in os.listdir(tool_path):
        subdir_path = os.path.join(tool_path, subdir)

        logging.info(f"Processing tare subdirectory: {subdir}")

        for filename in os.listdir(subdir_path):
            filepath = os.path.join(subdir_path, filename)

            if ".txt" not in filename:
                continue

            # Extract bug identifier from filename
            bug_id = filename.replace("patch", "").split('.')[0]

            # Set bug
            bug = bugs.loc[f"defects4j-{bug_id}"].copy()
            bug['uid'] = bug.name

            # Set uid
            uid = f"historian-{bug.name}-{tool}-{index}"

            # ...
            # Set locations
            # first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

            location = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

            if not os.path.exists(location):  
                raise FileNotFoundError(f"Patch file not found: {location}")
            
            corrects = [
                "Chart-1", "Chart-3", "Chart-4", "Chart-5", "Chart-8", "Chart-9", "Chart-11", "Chart-12", "Chart-20", "Chart-24", "Chart-26",
                "Cli-5", "Cli-8", "Cli-17", "Cli-18", "Cli-25", "Cli-28", "Cli-32",
                "Closure-1", "Closure-2", "Closure-5", "Closure-10", "Closure-11", "Closure-14", "Closure-15", "Closure-18", "Closure-33", "Closure-38", "Closure-40", "Closure-46", "Closure-62", "Closure-63", "Closure-70", "Closure-73", "Closure-86", "Closure-92", "Closure-93", "Closure-102", "Closure-104", "Closure-115", "Closure-118", "Closure-124", "Closure-126", "Closure-168",
                "Codec-2", "Codec-3", "Codec-7", "Codec-8", "Codec-17",
                "Compress-19", "Compress-27", "Compress-31", "Compress-32",
                "Csv-4", "Csv-5", "Csv-9", "Csv-11", "Csv-15",
                "Gson-6",
                "JacksonCore-1", "JacksonCore-5", "JacksonCore-8", "JacksonCore-13", "JacksonCore-16", "JacksonCore-24", "JacksonCore-27", "JacksonCore-46", "JacksonCore-49", "JacksonCore-76", "JacksonCore-83", "JacksonCore-96", "JacksonCore-99", "JacksonCore-102",
                "Jsoup-24", "Jsoup-26", "Jsoup-39", "Jsoup-40", "Jsoup-43", "Jsoup-55", "Jsoup-57", "Jsoup-61", "Jsoup-64", "Jsoup-68", "Jsoup-77", "Jsoup-85", "Jsoup-88", "Jsoup-89",
                "Jxpath-10", "Jxpath-12", "Jxpath-21",
                "Lang-6", "Lang-10", "Lang-16", "Lang-24", "Lang-26", "Lang-29", "Lang-33", "Lang-38", "Lang-43", "Lang-45", "Lang-46", "Lang-55", "Lang-57", "Lang-59",
                "Math-3", "Math-5", "Math-27", "Math-30", "Math-33", "Math-34", "Math-41", "Math-50", "Math-56", "Math-57", "Math-58", "Math-59", "Math-65", "Math-70", "Math-75", "Math-80", "Math-82", "Math-85", "Math-94", "Math-96", "Math-98", "Math-105",
                "Mockito-29", "Mockito-38",
                "Time-4", "Time-7", "Time-19",
            ]

            if bug_id in corrects:
                correctness = "Correct"
            else:
                correctness = "Overfitting"

            patch_dict = {
                "uid": uid,
                "bug_uid": bug.name,
                "generator": tool,
                "location": location,
                "correctness": correctness,
                "generator_id": tool,
                "origin": "Historian",
                "index": index
            }

            patches_list.append(patch_dict)
            index += 1

            # Run this once to copy paste the first cleaned patches
            # copy_paste(filepath, first_cleaned_location)
            # ..

    patches_df = pd.DataFrame(patches_list).set_index("uid")
    patches_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.pkl"))
    patches_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.html"))


def iterate_patches_tenure(bugs):
    tool = "tenure"
    tool_path = os.path.join(RQ4_DATA_DIR, tool)

    index = 0

    # Iterate through subdirectories 
    for subdir in os.listdir(tool_path):
        subdir_path = os.path.join(tool_path, subdir)

        logging.info(f"Processing tenure subdirectory: {subdir}")

        for filename in os.listdir(subdir_path):
            filepath = os.path.join(subdir_path, filename)

            if ".txt" not in filename or "Closure_63" in filename or "Closure_93" in filename:
                continue

            # Extract bug identifier from filename
            bug_id = filename.replace("_multi", "").split('.')[0].replace('_', '-')

            # Set bug
            bug = bugs.loc[f"defects4j-{bug_id}"].copy()
            bug['uid'] = bug.name

            # Set uid
            uid = f"historian-{bug.name}-{tool}-{index}"

            # Set locations
            first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

            # Run this once to copy paste the first cleaned patches
            copy_paste(filepath, first_cleaned_location)
            index += 1

def iterate_patches_transplantfix(bugs):
    tool = "transplantfix"
    tool_path = os.path.join(RQ4_DATA_DIR, tool)
    patches_list = []

    index = 0

    # Iterate through subdirectories 
    for subdir in os.listdir(tool_path):
        subdir_path = os.path.join(tool_path, subdir)

        if not os.path.isdir(subdir_path):
            continue

        logging.info(f"Processing transplantfix subdirectory: {subdir}")

        for foldername_fl in os.listdir(subdir_path):
            folderpath_fl = os.path.join(subdir_path, foldername_fl)

            for foldername_bug in os.listdir(folderpath_fl):
                folderpath_bug = os.path.join(folderpath_fl, foldername_bug)

                for filename in os.listdir(folderpath_bug):
                    filepath = os.path.join(folderpath_bug, filename)

                    # Extract bug identifier from filename
                    bug_id = foldername_bug.replace('_', '-')

                    # if "Closure_63" in filename or "Closure_93" in filename:
                    #     continue

                    # Set bug
                    bug = bugs.loc[f"defects4j-{bug_id}"].copy()
                    bug['uid'] = bug.name

                    # Set uid
                    uid = f"historian-{bug.name}-{tool}-{index}"

                    # Set locations
                    location = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

                    if not os.path.exists(location):  
                        raise FileNotFoundError(f"Patch file not found: {location}")  

                    correctness = "Overfitting"
                    readme_location = os.path.join(tool_path, "README.md")
                    with open(readme_location, 'r') as readme_file:
                        readme_content = readme_file.read()
                    
                    readme_chunks = readme_content.split(">>>")
                    if bug_id.replace('-', '_') in readme_chunks[1] and "FL_scenario" in foldername_fl:
                        correctness = "Correct"
                    
                    elif bug_id.replace('-', '_') in readme_chunks[2] and "Non_FL_scenario" in foldername_fl:
                        correctness = "Correct"

                    elif bug_id.replace('-', '_') in readme_chunks[3] and "FL_scenario" in foldername_fl:
                        correctness = "Correct"

                    elif bug_id.replace('-', '_') in readme_chunks[4] and "Non_FL_scenario" in foldername_fl:
                        correctness = "Correct"

                    # Run this once to copy paste the first cleaned patches
                    index += 1

                    patch_dict = {
                        "uid": uid,
                        "bug_uid": bug.name,
                        "generator": tool,
                        "location": location,
                        "correctness": correctness,
                        "generator_id": tool,
                        "origin": "Historian",
                        "index": index
                    }

                    patches_list.append(patch_dict)

    patches_df = pd.DataFrame(patches_list).set_index("uid")
    patches_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.pkl"))
    patches_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.html"))

def iterate_patches_arjae(bugs):
    tool = "arjae"
    tool_path = os.path.join(RQ4_DATA_DIR, tool, "patches")
    index = 0

    patches_list = []

    for filename in os.listdir(tool_path):
        filepath = os.path.join(tool_path, filename)

        # Skip non-patch files
        if filename.endswith(".md"):
            logging.info(f"Skipping file: {filepath}")
            continue

        # Extract bug identifier from filename
        bug_id = '-'.join(filename.split('_'))

        # Set bug
        bug = bugs.loc[f"defects4j-{bug_id}"].copy()
        bug['uid'] = bug.name

        # Set uid
        uid = f"historian-{bug.name}-{tool}-{index}"

        location = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

        if not os.path.exists(location):  
            raise FileNotFoundError(f"Patch file not found: {location}")
        
        correctness = "Overfitting"

        # 7 bugs for Chart: C3, C4, C5, C10, C11, C12, C24 \
        # 9 bugs for Lang: L7, L20, L24, L33, L34, L39, L43, L44, L61 \
        # 21 bugs for Math: M4, M5, M11, M22, M25, M30, M34, M39, M53, M56, M57, M58, M65, M70, M73, M75, M79, M86, M89, M94, M98 \
        # 2 bugs for Time: T7, T15

        bug_project, bug_number = filename.split('_')

        if bug_project == "Chart" and bug_number in ["3", "4", "5", "10", "11", "12", "24"]:
            correctness = "Correct"
        elif bug_project == "Lang" and bug_number in ["7", "20", "24", "33", "34", "39", "43", "44", "61"]:
            correctness = "Correct"
        elif bug_project == "Math" and bug_number in ["4", "5", "11", "22", "25", "30", "34", "39", "53", "56", "57", "58", "65", "70", "73", "75", "79", "86", "89", "94", "98"]:
            correctness = "Correct"
        elif bug_project == "Time" and bug_number in ["7", "15"]:
            correctness = "Correct"

        patch_dict = {
            "uid": uid,
            "bug_uid": bug.name,
            "generator": tool,
            "location": location,
            "correctness": correctness,
            "generator_id": tool,
            "origin": "Historian",
            "index": index
        }

        patches_list.append(patch_dict)

        index += 1

    patches_df = pd.DataFrame(patches_list).set_index("uid")
    patches_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.pkl"))
    patches_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.html"))

def iterate_patches_t5apr(bugs, ignore=False):
    tool = "t5apr"
    base_search_path = os.path.join(RQ4_DATA_DIR, tool)
    index = 0

    developer_patches = pd.read_pickle(os.path.join(RQ4_META_DATA_DIR, "cleaned-developer-patches.pkl"))
    patches_list = []
    for root, _, files in os.walk(base_search_path):
        for filename in files:
            filepath = os.path.join(root, filename)

            if not filename.endswith(".json"):
                print(f"Skipping non-JSON file: {filepath}")
                continue

            components = filepath.split(os.sep)

            bug_id = "-".join(components[-2].strip().split(" "))

            bug = bugs.loc[f"defects4j-{bug_id}"].copy()
            bug['uid'] = bug.name

            try:
                developer_patches.loc[f"{bug['uid']}-developer"]

            except:
                print(f"Developer patch not found for bug: {bug['uid']}")
                continue

            json_content = json.load(open(filepath, 'r'))

            # id = components[-1].split(".")[0]
            uid = f"historian-{bug.name}-{tool}-{index}"

            # ...
            # first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

            correctness = json_content["correct"]

            if correctness:
                correctness = "Correct"
            else:
                correctness = "Overfitting"

            location = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

            if not os.path.exists(location):  
                if ignore:
                    print(f"Patch file not found (ignored): {location}")
                    index += 1
                    
                    continue

                raise FileNotFoundError(f"Patch file not found: {location}")
                

            patch_dict = {
                "uid": uid,
                "bug_uid": bug.name,
                "generator": tool,
                "location": location,
                "correctness": correctness,
                "generator_id": tool,
                "origin": "Historian",
                "index": index
            }

            patches_list.append(patch_dict)


            # copy_paste(tmp_dir, first_cleaned_location)

            # # Remove temporary file
            # os.remove(tmp_dir)
            # ...

            index += 1

    patches_df = pd.DataFrame(patches_list).set_index("uid")
    patches_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.pkl"))
    patches_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.html"))

def iterate_patches_selfapr(bugs):
    tool = "selfapr"
    base_search_path = os.path.join(RQ4_DATA_DIR, tool)
    index = 0
    developer_patches = pd.read_pickle(os.path.join(RQ4_META_DATA_DIR, "cleaned-developer-patches.pkl"))
    
    # Read the single file containing all patches
    selfapr_file = os.path.join(base_search_path, "defects4j-patches.txt")  # adjust filename as needed
    with open(selfapr_file, 'r') as f:
        content = f.read()
    
    # Split by the separator
    patch_blocks = content.split('*' * 60)  # 60 asterisks as separator
    
    patches_list = []

    for block in patch_blocks:

        block = block.strip()
        if not block:
            continue
        
        lines = block.split('\n')
        
        # Parse the block
        # First line: "Identical   Codec_7_Base64_1_1" or "Plausible   Codec_7_Base64_1_1"
        first_line = lines[0].strip()

        parts = first_line.split()
        if len(parts) < 2:
            print(f"Skipping malformed block.")

            continue
        
        full_id = parts[1]  # e.g., "Codec_7_Base64_1_1"
        
        # Extract bug_id: take project and number (e.g., "Codec-7" from "Codec_7_Base64_1_1")
        id_parts = full_id.split('_')

        bug_id = f"{id_parts[0]}-{id_parts[1]}"  # e.g., "Codec-7"

        correctness = "Correct" if "Identical" in first_line else "Overfitting"

        bug = bugs.loc[f"defects4j-{bug_id}"].copy()
        bug['uid'] = bug.name
        uid = f"historian-{bug.name}-{tool}-{index}"

        location = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

        if not os.path.exists(location):  
            raise FileNotFoundError(f"Patch file not found: {location}")
            

        patch_dict = {
            "uid": uid,
            "bug_uid": bug.name,
            "generator": tool,
            "location": location,
            "correctness": correctness,
            "generator_id": tool,
            "origin": "Historian",
            "index": index
        }

        patches_list.append(patch_dict)

        index += 1
    
    print(f"Processed {index} patches for {tool}")

    patches_df = pd.DataFrame(patches_list).set_index("uid")
    patches_df.to_pickle(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.pkl"))
    patches_df.to_html(os.path.join(RQ4_META_DATA_DIR, f"{tool}_patches.html"))



def iterate_patches(bugs):
    # iterate_patches_circle(bugs)
    # iterate_patches_alpharepair(bugs)
    # iterate_patches_cure(bugs)
    # iterate_patches_dlfix(bugs)
    # iterate_patches_fitrepair(bugs)
    # iterate_patches_iter(bugs)
    # iterate_patches_knod(bugs)
    # iterate_patches_rapgen(bugs)
    # iterate_patches_recoder(bugs)
    # iterate_patches_repilot(bugs)
    iterate_patches_tare(bugs)
    # iterate_patches_tenure(bugs)
    # iterate_patches_transplantfix(bugs)
    # iterate_patches_arjae(bugs)
    # iterate_patches_t5apr(bugs, ignore=True)
    # iterate_patches_selfapr(bugs)

    pass

if __name__=="__main__": 
    logging.info("Running rq4.py ...")
    bugs, developer_patches, tool_patches = init(configure=False)

    logging.info("Reading patches from RQ4 data directory ...")
    files = iterate_patches(bugs)
