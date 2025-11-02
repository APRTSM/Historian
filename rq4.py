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

# Configure Benchmarks, Get Initial Data (Bugs, Developer Patches, Tool Patches)
def init(configure=True):
    logging.info("Fetching the initiaql data.")

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

def iterate_patches(rq4_data_dir, bugs):
    results = []
    for tool in os.listdir(rq4_data_dir):
        tool_path = os.path.join(rq4_data_dir, tool)

        if os.path.isdir(tool_path):
            index = 0

            for filename in os.listdir(tool_path):
                filepath = os.path.join(tool_path, filename)

                if os.path.isdir(filepath):
                    logging.info(f"Skipping non-patch file: {filepath}")

                    continue

                elif os.path.isdir(filepath):
                    continue

                elif "alpharepair" in tool:
                    continue

                elif "arja-e" in tool:
                    continue

                elif "chatrepair" in tool:
                    continue

                elif "circle" in tool:
                    bug = bugs.loc[f"defects4j-{filename.split('.')[0]}"].copy()
                    bug['uid'] = bug.name
                    uid = f"historian-{bug.name}-{tool}-{index}"
                    location = os.path.join(RQ4_FIRST_CLEANED_DATA_DIR, f"{uid}.patch")
                    formatted_patch_dir = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{uid}.patch")


                    # patch_content = generate_patch_from_ours(patch, bug)

                    if os.path.exists(formatted_patch_dir):
                        logging.info(f"Patch already exists in tmp formatted dir, skipping: {formatted_patch_dir}")

                        index += 1

                        continue

                    if not os.path.exists(location):
                        logging.info(f"Processing patch: {filepath}")
                        logging.info(f"Copying to first cleaned: {location}")

                        with open(filepath, 'r') as file:
                            patch_content = file.read()

                        with open(location, 'w') as file:
                            file.write(patch_content)

                        logging.info(f"Not Yet Fixed Patch Dir: {fixed_patch_dir}")

                    checkout_dir = None
                    while True:
                        logging.info(f"Trying to apply patch: {location}")

                        patch_dict = {
                            "uid": uid,
                            "bug_uid": bug.name,
                            "generator": "Circle",
                            "location": location,
                            "correctness": "Correct",
                            "generator_id": "circle",
                            "origin": "Historian"
                        }
                        patch = pd.Series(patch_dict, name=uid)

                        if checkout_dir:
                            shutil.rmtree(checkout_dir)

                        fixed_patch_dir = fix_patch(patch, bugs)

                        if fixed_patch_dir:
                            logging.info(f"Fixed Patch Dir: {fixed_patch_dir}")

                            patch["fixed_location"] = fixed_patch_dir

                            with open(fixed_patch_dir, 'r') as file:
                                fixed_patch_content = file.read()

                            with open(formatted_patch_dir, 'w') as file:
                                file.write(fixed_patch_content)

                            break

                        developer_patch = get_developer_patch(bug)
                        checkout_dir = checkout_bug(bug)

                        logging.error(f"Failed to fix patch: {location}. Retrying...")
                        print(f"Failed to fix patch: {location}. Retrying...")
                        print(f"Developer Patch Location: {developer_patch['location']}")
                        print(f"Checkout Directory: {checkout_dir}")
                        input("Press any key to try again.")

                    raise




    return results



if __name__=="__main__": 
    logging.info("Running rq4.py ...")
    bugs, developer_patches, tool_patches = init(configure=False)

    logging.info("Reading patches from RQ4 data directory ...")
    files = iterate_patches(RQ4_DATA_DIR, bugs)

    raise
