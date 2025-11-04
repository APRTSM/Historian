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

def iterate_patches_tool(bugs, tool_id, tool_name):
    for file in os.listdir(RQ4_SECOND_CLEANED_DATA_DIR):
        if not tool_id in file:
            continue

        # Parse    
        uid = file.split(".")[0]
        formatted_patch_dir = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")

        if os.path.exists(formatted_patch_dir):
            logging.info(f"Patch already exists in tmp formatted dir, skipping: {formatted_patch_dir}")
            index += 1
            continue

        _, _, project, bug_id, _, index = uid.split("-")

        # Set bug
        bug = bugs.loc[f"defects4j-{project}-{bug_id}"].copy()
        bug['uid'] = bug.name

        second_cleaned_location = os.path.join(RQ4_SECOND_CLEANED_DATA_DIR, f"{uid}.patch")

        checkout_dir = None
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

            if checkout_dir:
                shutil.rmtree(checkout_dir)

            fixed_patch_dir = fix_patch(patch, bugs)

            if fixed_patch_dir:
                logging.info(f"Fixed Patch Dir: {fixed_patch_dir}")

                patch["fixed_location"] = fixed_patch_dir
                copy_paste(fixed_patch_dir, formatted_patch_dir)
                index += 1

                print(f"Successfully fixed patch: {formatted_patch_dir}")
                break

            developer_patch = get_developer_patch(bug)
            checkout_dir = checkout_bug(bug)

            logging.error(f"Failed to fix patch: {second_cleaned_location}. Retrying...")
            print(f"Failed to fix patch: {second_cleaned_location}. Retrying...")
            print(f"Developer Patch Location: {developer_patch['location']}")
            print(f"Checkout Directory: {checkout_dir}")
            input("Press enter key to try again.")

def iterate_patches_tools(bugs):
    iterate_patches_tool(bugs, "circle", "Circle")
    iterate_patches_tool(bugs, "alpharepair", "AlphaRepair")


if __name__=="__main__": 
    logging.info("Running rq4.py ...")
    bugs, developer_patches, tool_patches = init(configure=False)

    logging.info("Reading patches from RQ4 data directory ...")
    files = iterate_patches_tools(bugs)

    raise