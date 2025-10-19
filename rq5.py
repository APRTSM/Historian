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

# Get User Data
def get_input_patches(bugs):
    logging.info("Fetching input patches ...")

    input_patches = []

    for file_name in os.listdir(INPUT_DIR):
        # Set all to None from previous iteration
        tool = benchmark = bug = bug_project = bug_number = location =  None

        if not file_name.endswith(".patch"):
            continue

        patch_name = file_name[:-6]
        tool, benchmark = patch_name.split("_")[:2]
        location = os.path.relpath(os.path.join(INPUT_DIR, file_name), start=PROJECT_DIR)

        if benchmark == "defects4j":
            bug_project, bug_number = patch_name.split("_")[2:]
            bug_info = {"benchmark": "Defects4J", "project": bug_project, "number": bug_number}

        else:
            continue

        bug = get_record(bugs, bug_info)
        
        if not bug:
            continue
        
        # Continue beore this
        assert tool and benchmark and bug and bug_project and bug_number and location

        input_patch = {
            "uid": f"generated-{tool}-{benchmark}-{bug_project}-{bug_number}",
            "bug_uid": bug["uid"],
            "generator": tool,
            "location": os.path.relpath(location, PROJECT_DIR),
            "correctness": "Unknown",
            "origin": None
        }

        input_patches.append(input_patch)
    
    no_input_patches = len(input_patches)

    logging.info(f"Succesfully fetched input. \n #InputPatches: {no_input_patches}")

    input_patches = pd.DataFrame(input_patches).set_index("uid")

    return input_patches

def clean_patches(bugs, developer_patches, tool_patches):
    logging.info("Testing tool patches (keep applicable only) ...")

    if os.path.exists(TMP_CLEANED_DEVELOPER_PATHCES_PKL):
        cleaned_developer_patches = pd.read_pickle(TMP_CLEANED_DEVELOPER_PATHCES_PKL)
    
    else:
        cleaned_developer_patches = developer_patches.copy()
        tqdm.pandas(desc=f"Fixing developer patches.")
        cleaned_developer_patches["location"] = cleaned_developer_patches.progress_apply(fix_patch, args=(bugs, ), axis=1)
        tqdm.pandas(desc="Unknown Process.")
        cleaned_developer_patches.dropna(subset=['location'], inplace=True)
        cleaned_developer_patches.to_pickle(TMP_CLEANED_DEVELOPER_PATHCES_PKL)

    if os.path.exists(TMP_CLEANED_TOOL_PATHCES_PKL):
        cleaned_tool_patches = pd.read_pickle(TMP_CLEANED_TOOL_PATHCES_PKL)
    
    else:
        cleaned_tool_patches = tool_patches.copy()
        tqdm.pandas(desc=f"Fixing tool patches.")
        cleaned_tool_patches["location"] = cleaned_tool_patches.progress_apply(fix_patch, args=(bugs, ), axis=1)
        tqdm.pandas(desc="Unknown Process.")
        cleaned_tool_patches.dropna(subset=['location'], inplace=True)
        cleaned_tool_patches.to_pickle(TMP_CLEANED_TOOL_PATHCES_PKL)

    no_cleaned_developer_patches = len(cleaned_developer_patches)
    no_cleaned_tool_patches = len(cleaned_tool_patches)

    logging.info(f"Successfully tested tool patches: \n{no_cleaned_developer_patches} \n{no_cleaned_tool_patches} \n Correct Tool Patches: {len(cleaned_tool_patches[cleaned_tool_patches['correctness'] == 'Correct'])}, Incorrect Tool Patches: {len(cleaned_tool_patches[cleaned_tool_patches['correctness'] == 'Overfitting'])}, Unknown Tool Patches: {len(cleaned_tool_patches[cleaned_tool_patches['correctness'] == 'Unknown'])}") 
    dev_origin_counts = cleaned_developer_patches['origin'].value_counts().to_dict()
    logging.info(f"Added origin_count feature. Developer origins: {dev_origin_counts}.")

    return cleaned_developer_patches, cleaned_tool_patches

# Returns methods
def get_methods(developer_patches: pd.DataFrame, tool_patches: pd.DataFrame, bugs: pd.DataFrame):
    if os.path.exists(TMP_METHOD_DEVELOPER_PATHCES_PKL):
        developer_patches = pd.read_pickle(TMP_METHOD_DEVELOPER_PATHCES_PKL)
        tool_patches = pd.read_pickle(TMP_METHOD_TOOL_PATHCES_PKL)

        return developer_patches, tool_patches
    
    else:
        tqdm.pandas(desc=f"Getting methods for developer patches.")
        tool_patches[['source_methods', 'target_methods']] = tool_patches.progress_apply(lambda row: get_method(row, bugs), axis=1, result_type='expand')
        tqdm.pandas(desc="Unknown Process.")

        tqdm.pandas(desc=f"Getting methods for tool patches.")
        developer_patches[['source_methods', 'target_methods']] = developer_patches.progress_apply(lambda row: get_method(row, bugs), axis=1, result_type='expand')
        tqdm.pandas(desc="Unknown Process.")

        logging.info(f"Successfully fetched methods. No Methgod Developer Patches: {len(developer_patches)}, No Method Tool Patches: {len(tool_patches)}")
        logging.info("Dropping patches with no methods ...")

        cleaned_developer_patches.dropna(subset=['target_methods'], inplace=True)
        cleaned_tool_patches.dropna(subset=['target_methods'], inplace=True)

        logging.info(f"Successfully dropped patches with no methods. No Methgod Developer Patches: {len(developer_patches)}, No Method Tool Patches: {len(tool_patches)}")

        cleaned_developer_patches.to_pickle(TMP_METHOD_DEVELOPER_PATHCES_PKL)
        cleaned_tool_patches.to_pickle(TMP_METHOD_TOOL_PATHCES_PKL)

        return developer_patches, tool_patches

def get_patch_processors():
    logging.info("Listing Patch Processors.")

    function_mapping = {
        "defaultpatch": get_raw_patch,
        "method": get_single_hunk_method,
        "headerlesspatch": get_headerless_patch,
    }

    if os.path.exists(TMP_PROCESSORS_JSON):
        with open(TMP_PROCESSORS_JSON, 'r') as file:
            patch_processors = json.load(file)  

    else:
        patch_processors = [
            {
                "uid": "defaultpatch",
            },
            {
                "uid": "method",
            },
            {
                "uid": "headerlesspatch",
            }
        ]

        with open(TMP_PROCESSORS_JSON, 'w') as file:
            json.dump(patch_processors, file)

    for patch_processor in patch_processors:
        patch_processor["function"] = function_mapping[patch_processor["uid"]]

    logging.info(f"\n PatchProcessors: \n {patch_processors}.")

    return patch_processors

def get_tool_settings():
    logging.info("Fetching tool settings.")

    if os.path.exists(TMP_OLLAMA_DIR):
        with open(TMP_OLLAMA_PROMPTS_JSON, 'r') as file:
            ollama_prompts = json.load(file)   

        with open(TMP_OLLAMA_MODELS_JSON, 'r') as file:
            ollama_models = json.load(file)  

        with open(TMP_OLLAMA_TEMPERATURES_JSON, 'r') as file:
            ollama_temperatures = json.load(file)  

    else:
        ollama_prompts, ollama_models, ollama_temperatures = ollama_get_settings()

        os.mkdir(TMP_OLLAMA_DIR)

        shutil.copy(OLLAMA_PROMPTS_JSON, TMP_OLLAMA_PROMPTS_JSON)
        shutil.copy(OLLAMA_MODELS_JSON, TMP_OLLAMA_MODELS_JSON)
        shutil.copy(OLLAMA_TEMPERATURES_JSON, TMP_OLLAMA_TEMPERATURES_JSON)

    logging.info(f"Successfully fetched tool settings. \n OllamaPrompts: \n {ollama_prompts}, \n OllamaModels: \n {ollama_models}, \n OllamaTemperatures: \n {ollama_temperatures}")

    return ollama_prompts, ollama_models, ollama_temperatures

# Deduplicate
def normalaize_names(developer_patches, tool_patches):
    if os.path.exists(TMP_GENERATOR_NORMALIZED_DEVELOPER_PATHCES_PKL) and os.path.exists(TMP_GENERATOR_NORMALIZED_TOOL_PATHCES_PKL):
        developer_patches = pd.read_pickle(TMP_GENERATOR_NORMALIZED_DEVELOPER_PATHCES_PKL)
        tool_patches = pd.read_pickle(TMP_GENERATOR_NORMALIZED_TOOL_PATHCES_PKL)

        return developer_patches, tool_patches
    
    # Normalize generator names
    tool_patches['generator_id'] = tool_patches['generator'].str.lower().apply(lambda x: '-'.join(x.split('-')[:-1]) if '-' in x else x)
    developer_patches['generator_id'] = developer_patches['generator'].str.lower().apply(lambda x: '-'.join(x.split('-')[:-1]) if '-' in x else x)

    # Save normalized patches
    developer_patches.to_pickle(TMP_GENERATOR_NORMALIZED_DEVELOPER_PATHCES_PKL)
    tool_patches.to_pickle(TMP_GENERATOR_NORMALIZED_TOOL_PATHCES_PKL)

    return developer_patches, tool_patches

def deduplicate_patches(cleaned_tool_patches):
    logging.info("Deduplicating developer patches ...")
    
    if os.path.exists(TMP_DEDUPLICATED_TOOL_PATHCES_PKL):
        cleaned_tool_patches = pd.read_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)

    else:
        cleaned_tool_patches = cleaned_tool_patches.copy()
        cleaned_tool_patches['content'] = cleaned_tool_patches['target_methods'].apply(lambda x: read_file(x[0]) if isinstance(x, list) and len(x) > 0 else None)
        # cleaned_tool_patches['content'] = cleaned_tool_patches['location'].apply(read_patch)
        tqdm.pandas(desc="Deduplicating patches")
        tqdm.pandas(desc="Deduplicating patches")
        cleaned_tool_patches = cleaned_tool_patches.drop_duplicates(subset=['bug_uid', 'generator_id', 'content']) 
        cleaned_tool_patches = cleaned_tool_patches.drop(columns=['content'])
        cleaned_tool_patches.to_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)

    logging.info(f"Filtered tool patches to only include Defects4J bugs. No of tool patches: {cleaned_tool_patches[cleaned_tool_patches['bug_uid'].str.contains('defects4j', case=False, na=False)]}")

    return cleaned_tool_patches

def second_deduplicate_patches(cleaned_developer_patches, cleaned_tool_patches):
    logging.info("Second deduplicating developer patches ...")
    
    if os.path.exists(TMP_SECOND_DEDUPLICATED_TOOL_PATHCES_PKL):
        cleaned_tool_patches = pd.read_pickle(TMP_SECOND_DEDUPLICATED_TOOL_PATHCES_PKL)

    else:
        cleaned_tool_patches['content'] = cleaned_tool_patches['location'].apply(read_patch)
        tqdm.pandas(desc="Deduplicating patches")
        cleaned_tool_patches = cleaned_tool_patches.drop_duplicates(subset=['bug_uid', 'content']) 
        cleaned_tool_patches = cleaned_tool_patches.drop(columns=['content'])

        # Deduplicate tool patches against developer patches
        cleaned_developer_patches['content'] = cleaned_developer_patches['location'].apply(read_patch)
        cleaned_tool_patches['content'] = cleaned_tool_patches['location'].apply(read_patch)
        tqdm.pandas(desc="Deduplicating tool patches against developer patches")
        cleaned_tool_patches = cleaned_tool_patches[~cleaned_tool_patches.apply(lambda row: ((cleaned_developer_patches['bug_uid'] == row['bug_uid']) & (cleaned_developer_patches['content'] == row['content'])).any(), axis=1)]
        cleaned_tool_patches = cleaned_tool_patches.drop(columns=['content'])
        cleaned_developer_patches = cleaned_developer_patches.drop(columns=['content'])

        cleaned_tool_patches.to_pickle(TMP_SECOND_DEDUPLICATED_TOOL_PATHCES_PKL)

    return cleaned_developer_patches, cleaned_tool_patches

def is_in_list(patch, patch_list):
    """Check if the patch matches any patch in the patch_list based on code content.
    
    Args:
        patch: Series representing a patch
        patch_list: DataFrame containing multiple patches
    
    Returns:
        bool: True if the patch matches any patch in the list, False otherwise
    """
    # Get patch content
    patch_content = read_patch(patch['location'])
    
    # Only compare patches for the same bug
    same_bug_patches = patch_list[patch_list['bug_uid'] == patch['bug_uid']]
    
    if same_bug_patches.empty:
        return False
        
    # Check each patch in the filtered list
    for _, list_patch in same_bug_patches.iterrows():
        list_patch_content = read_patch(list_patch['location'])

        if are_codes_identical(patch_content, list_patch_content):
            return True
            
    return False

# Parameters
def parse_args():
    parser = argparse.ArgumentParser(description="Run build.py with specific prompt, model, and processor.")
    parser.add_argument('--prompt', type=str, help='UID of the prompt to use')
    parser.add_argument('--model', type=str, help='UID of the model to use')
    parser.add_argument('--processor', type=str, help='UID of the processor to use')

    return parser.parse_args()

def apply_params(args, prompts, models, patch_processors):
    if args.prompt:
        prompts = [prompt for prompt in prompts if prompt["uid"] == args.prompt]

    if args.model:
        models = [model for model in models if model["uid"] == args.model]

    if args.processor:
        patch_processors = [processor for processor in patch_processors if processor["uid"] == args.processor]

    if not prompts or not models or not patch_processors:
        raise ValueError("Invalid prompt, model, or processor UID provided.")
    
    return prompts, models, patch_processors


# RQ4
def generate_patch_from_ours(patch, bug):
    if hasattr(bug, "to_dict"):
        bug_uid = bug.name
        bug = bug.to_dict()
        bug['uid'] = bug_uid

    checkout_dir = checkout_bug_defects4j(bug)
    dir = get_developer_patch(bug)


    
    print(111111111)

    print(checkout_dir)
    print(dir)

    content = read_patch(patch['location'])
    """
    Converts the diff format to a unified patch using the 'ours' version.
    
    Args:
        content: String containing the diff in the specified format
    
    Returns:
        String containing the unified diff patch
    """
    lines = content.strip().split('\n')
    
    # Extract file path and line numbers
    file_path = None
    start_line = None
    end_line = None
    buggy_lines = []
    ours_lines = []
    
    section = None
    
    for line in lines:
        # Check if line contains a file path (has a slash)
        if '/' in line and not line.startswith('-') and not line.startswith('+') and 'buggy' not in line.lower() and 'developer' not in line.lower() and 'ours' not in line.lower():
            file_path = line.strip()
        elif line and line[0].isdigit():
            # Parse line numbers like "584 - 584" or "300 - 303"
            parts = line.replace(' ', '').split('-')
            start_line = int(parts[0])
            if len(parts) > 1:
                end_line = int(parts[1])
        elif '### buggy:' in line.lower() or '###    buggy:' in line.lower():
            section = 'buggy'
        elif '### developer:' in line.lower():
            section = 'developer'
        elif '### ours:' in line.lower() or '###    ours:' in line.lower():
            section = 'ours'
        elif line.startswith('-') and section == 'buggy':
            buggy_lines.append(line[1:].strip())  # Remove '-' prefix and trim
        elif line.startswith('+') and section == 'ours':
            ours_lines.append(line[1:].strip())  # Remove '+' prefix and trim
    
    # Calculate line counts
    buggy_count = len(buggy_lines)
    ours_count = len(ours_lines)
    
    # Generate unified diff
    patch = f"--- {file_path}\n"
    patch += f"+++ {file_path}\n"
    patch += f"@@ -{start_line},{buggy_count} +{start_line},{ours_count} @@\n"
    
    # Add removed lines
    for line in buggy_lines:
        patch += f"-{line}\n"
    
    # Add added lines
    for line in ours_lines:
        patch += f"+{line}\n"
    
    return patch

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

                        continue

                    if not os.path.exists(location):
                        logging.info(f"Processing patch: {filepath}")
                        logging.info(f"Copying to first cleaned: {location}")

                        with open(filepath, 'r') as file:
                            patch_content = file.read()

                        with open(location, 'w') as file:
                            file.write(patch_content)

                        index += 1

                        fixed_patch_dir = fix_patch(patch, bugs)

                        logging.info(f"Fixed Patch Dir: {fixed_patch_dir}")

                        patch["fixed_location"] = fixed_patch_dir

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
    bugs, developer_patches, tool_patches = init(configure=False)

    # Initial Data
    bugs, developer_patches, tool_patches = init(configure=False)

    # Patch Cleaning (keep applicable only)
    cleaned_developer_patches, cleaned_tool_patches = clean_patches(bugs, developer_patches, tool_patches)

    # Fetch Methods
    cleaned_developer_patches, cleaned_tool_patches = get_methods(cleaned_developer_patches, cleaned_tool_patches, bugs)

    # Patch Processings
    patch_processors = get_patch_processors()

    # Tool Settings
    prompts, models, temperatures = get_tool_settings()

    # Apply Passed Params
    args = parse_args()
    prompts, models, patch_processors = apply_params(args, prompts, models, patch_processors)

    # Normalaize Names, Last step for Developer Patches, Formatting generator_id
    cleaned_developer_patches, cleaned_tool_patches = normalaize_names(cleaned_developer_patches, cleaned_tool_patches)

    report_dataset(cleaned_developer_patches, cleaned_tool_patches, bugs)

    # Get Single Hunk Patches (Same as Get Single Methods Until 15 June)
    cleaned_tool_patches = get_single_hunks(cleaned_tool_patches, cleaned_developer_patches)

    report_dataset(cleaned_developer_patches, cleaned_tool_patches, bugs)
    # print(cleaned_tool_patches)

    # Deduplicating Same bug, same tool, same content (Now looks for Methods), Last step for Tool Patches
    cleaned_tool_patches = deduplicate_patches(cleaned_tool_patches)
    # print(cleaned_tool_patches)

    report_dataset(cleaned_developer_patches, cleaned_tool_patches, bugs)

    """ Standard Upto Here Next Main """

    