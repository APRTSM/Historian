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
Gathers files from different projects in rq4/tool_patches and stores them in rq4/first_cleaned_data
We fix them manually and move them to rq4/second_cleaned_data
RQ4_FIRST_CLEANED_DATA_DIR is gathered data dir
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
        
        # Set locations
        first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")
        
        # Run this once to copy paste the first cleaned patches
        copy_paste(filepath, first_cleaned_location)
        index += 1
    
    # Process deferred files (Closure-63 and Closure-93) at the end
    for filename, filepath in deferred_files:
        # Set bug
        bug = bugs.loc[f"defects4j-{filename.split('.')[0]}"].copy()
        bug['uid'] = bug.name
        
        # Set uid
        uid = f"historian-{bug.name}-{tool}-{index}"
        
        # Set locations
        first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")
        
        # Run this once to copy paste the first cleaned patches
        copy_paste(filepath, first_cleaned_location)
        index += 1

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
            
            # Set locations
            first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")
            
            # Run this once to copy paste the first cleaned patches
            copy_paste(filepath, first_cleaned_location)
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
        
        # Set locations
        first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")
        
        # Run this once to copy paste the first cleaned patches
        copy_paste(filepath, first_cleaned_location)
        index += 1

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
            
            # Set locations
            first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")
            
            # Run this once to copy paste the first cleaned patches
            copy_paste(filepath, first_cleaned_location)
            index += 1
    
    # Process deferred files (Closure-63) at the end
    for filename, filepath, bug_id in deferred_files:
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

def iterate_patches_knod(bugs):
    tool = "knod"
    tool_path = os.path.join(RQ4_DATA_DIR, tool)

    index = 0

    # Iterate through subdirectories 
    for subdir in os.listdir(tool_path):
        subdir_path = os.path.join(tool_path, subdir)

        logging.info(f"Processing knod subdirectory: {subdir}")

        for filename in os.listdir(subdir_path):
            filepath = os.path.join(subdir_path, filename)

            # Extract bug identifier from filename
            bug_id = "-".join(filename.split('_')[:2])

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
        
        # Set locations
        first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")
        
        # Run this once to copy paste the first cleaned patches
        copy_paste(filepath, first_cleaned_location)
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
        
        # Set locations
        first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")
        
        # Run this once to copy paste the first cleaned patches
        copy_paste(filepath, first_cleaned_location)
        index += 1

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

            # Set locations
            first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

            # Run this once to copy paste the first cleaned patches
            copy_paste(filepath, first_cleaned_location)
            index += 1

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
                    first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

                    # Run this once to copy paste the first cleaned patches
                    copy_paste(filepath, first_cleaned_location)
                    index += 1

def iterate_patches_arjae(bugs):
    tool = "arjae"
    tool_path = os.path.join(RQ4_DATA_DIR, tool, "patches")
    index = 0

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

        # Set locations
        first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

        # Run this once to copy paste the first cleaned patches
        copy_paste(filepath, first_cleaned_location)

        index += 1
    

def iterate_patches_t5apr(bugs):
    tool = "t5apr"
    base_search_path = os.path.join(RQ4_DATA_DIR, tool)
    index = 0

    developer_patches = pd.read_pickle(os.path.join(RQ4_META_DATA_DIR, "cleaned-developer-patches.pkl"))

    deferred_files = []
    for root, _, files in os.walk(base_search_path):
        for filename in files:
            filepath = os.path.join(root, filename)

            if not filename.endswith(".json"):
                print(f"Skipping non-JSON file: {filepath}")
                continue

            components = filepath.split(os.sep)

            bug_id = "-".join(components[-2].strip().split(" "))

            if bug_id in ["Closure-63", "Closure-93"]:
                deferred_files.append((filename, filepath, components))
                continue

            bug = bugs.loc[f"defects4j-{bug_id}"].copy()
            bug['uid'] = bug.name

            try:
                developer_patch = developer_patches.loc[f"{bug['uid']}-developer"]

            except:
                print(f"Developer patch not found for bug: {bug['uid']}")
                continue
            developer_patch_content = read_file(developer_patch['location'])

            json_content = json.load(open(filepath, 'r'))

            # Assuming developer_patch_content is a string
            lines = developer_patch_content.splitlines()
            new_lines = []

            for line in lines:
                # Check if first char is '+' and second is ' '
                if line.startswith("+ "):
                    # Keep the prefix "+ " and append the new patch content
                    new_lines.append("+     " + json_content["patch"])
                else:
                    # Keep original line
                    new_lines.append(line)

            # Reassemble the string
            modified_patch_content = "\n".join(new_lines)

            tmp_dir = os.path.join(RQ4_META_DATA_DIR, "tmp")

            # Write back to the same file (or a new file if preferred)
            with open(tmp_dir, 'w') as f:
                f.write(modified_patch_content)

            id = components[-1].split(".")[0]
            uid = f"historian-{bug.name}-{tool}-{id}-{index}"

            first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

            # copy_paste(tmp_dir, first_cleaned_location)

            # Remove temporary file
            os.remove(tmp_dir)

            index += 1



    

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
    # iterate_patches_tare(bugs)
    # iterate_patches_tenure(bugs)
    # iterate_patches_transplantfix(bugs)
    # iterate_patches_arjae(bugs)
    iterate_patches_t5apr(bugs)

    pass

if __name__=="__main__": 
    logging.info("Running rq4.py ...")
    bugs, developer_patches, tool_patches = init(configure=False)

    logging.info("Reading patches from RQ4 data directory ...")
    files = iterate_patches(bugs)
