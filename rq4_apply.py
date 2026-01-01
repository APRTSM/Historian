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
First check if patch exists in cleaned patches
Try to apply from rq4/second_cleaned_tool_patches
Search rq4/second_cleaned_tool_patches 
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

def copy_patches_not_fixed(tool_id):
    counter = 0
    for file in os.listdir(RQ4_SECOND_CLEANED_DATA_DIR):
        if tool_id in file:
            counter += 1

    logging.info(f"📢 Total patches already for tool {tool_id}: {counter}")
    print(f"📢 Total patches already for tool {tool_id}: {counter}")

    for file in os.listdir(RQ4_FIRST_CLEANED_DATA_DIR):
        if not tool_id in file:
            continue

        formatted_patch_dir = os.path.join(RQ4_SECOND_CLEANED_DATA_DIR, file)

        if os.path.exists(formatted_patch_dir):
            logging.info(f"✅ Patch already exists, skipping: {formatted_patch_dir}")
            continue

        first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, file)

        print(first_cleaned_location, formatted_patch_dir)
        raise
        copy_paste(first_cleaned_location, formatted_patch_dir)

        logging.info(f"✅ Copied patch to tmp formatted dir: {formatted_patch_dir}")
        
    counter = 0
    for file in os.listdir(RQ4_SECOND_CLEANED_DATA_DIR):
        if tool_id in file:
            counter += 1

    logging.info(f"📢 Total copied patches for tool {tool_id}: {counter}")
    print(f"📢 Total copied patches for tool {tool_id}: {counter}")

def iterate_patches_tool(bugs, tool_id, tool_name, ignore=True):
    # copy_patches_not_fixed does not work because when I gave it to prst It didnt include clousre 63 and 93
    # copy_patches_not_fixed(tool_id) 

    for file in os.listdir(RQ4_SECOND_CLEANED_DATA_DIR):
        if tool_id in file:
            counter += 1

    logging.info(f"📢 Total patches already for tool {tool_id}: {counter}")
    print(f"📢 Total patches already for tool {tool_id}: {counter}")

    count_success = 0
    count_total = 0

    for file in os.listdir(RQ4_SECOND_CLEANED_DATA_DIR):
        if not tool_id in file:
            continue

        count_total += 1

    for file in os.listdir(RQ4_SECOND_CLEANED_DATA_DIR):
        if not tool_id in file:
            continue

        print(f"Iteration Number: {count_success+1} / {count_total}")
        logging.info(f"Iteration Number: {count_success+1} / {count_total}")

        # Parse    
        uid = file.split(".")[0]
        formatted_patch_dir = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

        if os.path.exists(formatted_patch_dir):
            logging.info(f"✅ Patch already exists, skipping: {formatted_patch_dir}")
            print(f"✅ Patch already exists, skipping: {formatted_patch_dir}")
            count_success += 1
            continue

        _, _, project, bug_id, _, _ = uid.split("-")

        # Set bug
        bug = bugs.loc[f"defects4j-{project}-{bug_id}"].copy()
        bug['uid'] = bug.name

        second_cleaned_location = os.path.join(RQ4_SECOND_CLEANED_DATA_DIR, f"{uid}.patch")
        first_cleaned_location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")

        while True:
            logging.info(f"Trying to apply patch: {second_cleaned_location}")

            patch_dict = {
                "uid": uid,
                "bug_uid": bug.name,
                "generator": tool_name,
                "location": second_cleaned_location,
                "correctness": "Correct",
                "generator_id": tool_id,
                "origin": "Historian"
            }
            patch = pd.Series(patch_dict, name=uid)

            fixed_patch_dir = fix_patch(patch, bugs)

            if fixed_patch_dir:
                logging.info(f"✅ Fixed Patch Dir: {fixed_patch_dir}")

                patch["fixed_location"] = fixed_patch_dir
                copy_paste(fixed_patch_dir, formatted_patch_dir)

                print(f"✅ Successfully fixed patch: {formatted_patch_dir}")
                print("="*20 + "\n")

                count_success += 1
                break

            developer_patch = get_developer_patch(bug)
            checkout_dir = checkout_bug(bug)

            # Read patch content
            with open(second_cleaned_location, 'r') as f:
                patch_content = f.read()

            changed_file_address_path = os.path.join(checkout_dir, patch_content.split("\n")[0].replace("--- a/", "").split(" ")[0].strip())

            logging.error(f"❌ Failed to fix patch: {second_cleaned_location}. \n Retrying... \n")
            print(f"-> ORIGINAL path: {first_cleaned_location} \n")
            print(f"+ Bug uid: {bug['uid']} \n")
            print(f"Tool: {tool_name} \n")
            print(f"Changed File Address Path: {changed_file_address_path} \n")
            print(f"❌ Failed to fix patch: {second_cleaned_location}. \n Retrying... \n")
            print(f"Developer Patch Location: {developer_patch['location']} \n")
            print(f"Checkout Directory: {checkout_dir} \n")
            

            if ignore:
                if os.path.exists(checkout_dir):
                    shutil.rmtree(checkout_dir)

                break

            input("Press enter key to try again.")

            if os.path.exists(checkout_dir):
                shutil.rmtree(checkout_dir)

            print("-"*20 + "\n")

    logging.info(f"📢 Successfully fixed {count_success} from {count_total} patches for tool: {tool_name}")
    print(f"📢 Successfully fixed {count_success} from {count_total} patches for tool: {tool_name}")

def iterate_patches_tools(bugs):
    # iterate_patches_tool(bugs, "circle", "Circle")
    # iterate_patches_tool(bugs, "alpharepair", "AlphaRepair")
    # iterate_patches_tool(bugs, "cure", "CURE")
    # iterate_patches_tool(bugs, "dlfix", "DLFix")
    # iterate_patches_tool(bugs, "fitrepair", "FitRepair")
    # iterate_patches_tool(bugs, "knod", "KNode")
    # iterate_patches_tool(bugs, "rapgen", "RapGen")
    iterate_patches_tool(bugs, "recoder", "Recoder", ignore=False)
    # iterate_patches_tool(bugs, "repilot", "RePilot")
    # iterate_patches_tool(bugs, "tare", "Tare")
    # iterate_patches_tool(bugs, "tenure", "Tenure")
    # iterate_patches_tool(bugs, "transplantfix", "TransplantFix", ignore=False)

if __name__=="__main__": 
    logging.info("Running rq4.py ...")
    bugs, developer_patches, tool_patches = init(configure=False)

    logging.info("Reading patches from RQ4 data directory ...")
    files = iterate_patches_tools(bugs)
