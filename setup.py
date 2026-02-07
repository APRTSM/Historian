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


FOLDER_NAME = "setup"

SETUP_BUGS_DIR = {
    "__base__": "bugs",
    "RAW": os.path.join(FOLDER_NAME, "bugs.pkl")
}

SETUP_DEVELOPER_PATCHES_DIR = {
    "__base__": "developer",
    "RAW": os.path.join(FOLDER_NAME, "developer_patches.pkl"),
    "CLEANED": os.path.join(FOLDER_NAME, "cleaned_developer_patches.pkl"),
    "METHOD": os.path.join(FOLDER_NAME, "method_developer_patches.pkl"),
    "FILES": os.path.join(FOLDER_NAME, "files_developer_patches.pkl"),
    "NORMALIZED": os.path.join(FOLDER_NAME, "normalized_developer_patches.pkl"),
    "SINGLE_METHOD": os.path.join(FOLDER_NAME, "single_method_developer_patches.pkl"),
    "DEDUPLICATED": os.path.join(FOLDER_NAME, "deduplicated_developer_patches.pkl"),
}

SETUP_TOOL_PATCHES_DIR = {
    "__base__": "tool",
    "RAW": os.path.join(FOLDER_NAME, "tool_patches.pkl"),
    "CLEANED": os.path.join(FOLDER_NAME, "cleaned_tool_patches.pkl"),
    "METHOD": os.path.join(FOLDER_NAME, "method_tool_patches.pkl"),
    "FILES": os.path.join(FOLDER_NAME, "files_tool_patches.pkl"),
    "NORMALIZED": os.path.join(FOLDER_NAME, "normalized_tool_patches.pkl"),
    "SINGLE_METHOD": os.path.join(FOLDER_NAME, "single_method_tool_patches.pkl"),
    "DEDUPLICATED": os.path.join(FOLDER_NAME, "deduplicated_tool_patches.pkl"),
}

SETUP_HISTORIAN_PATCHES_DIR = {
    "__base__": "historian",
    "RAW": os.path.join(FOLDER_NAME, "historian_patches.pkl"),
    "CLEANED": os.path.join(FOLDER_NAME, "cleaned_historian_patches.pkl"),
    "METHOD": os.path.join(FOLDER_NAME, "method_historian_patches.pkl"),
    "FILES": os.path.join(FOLDER_NAME, "files_historian_patches.pkl"),
    "NORMALIZED": os.path.join(FOLDER_NAME, "normalized_historian_patches.pkl"),
    "SINGLE_METHOD": os.path.join(FOLDER_NAME, "single_method_historian_patches.pkl"),
    "DEDUPLICATED": os.path.join(FOLDER_NAME, "deduplicated_historian_patches.pkl"),
}


EXTRACT_FILES = False
CONFIGURE_BENCHMARKS = False
GET_SUDO = False

# Fetch data and preprocess
def fetch_bugs():
    logging.info("Fetching bugs ...")

    if CONFIGURE_BENCHMARKS:
        configure_benchmarks()

    if os.path.exists(SETUP_BUGS_DIR["RAW"]):
        logging.info("Using the bugs data in TMP ...")
        bugs = pd.read_pickle(SETUP_BUGS_DIR["RAW"])
    else:
        logging.info("Fetching bugs data ...")
        bugs_list = get_bugs()
        bugs = pd.DataFrame(bugs_list).set_index("uid")
        bugs.to_pickle(SETUP_BUGS_DIR["RAW"])

    logging.info(f"Successfully fetched bugs. No of bugs: {len(bugs)}")

    return bugs

def fetch_patches(bugs_df, dirs_dict):

    # make bugs a dictionary from dataframe
    bugs_with_uid = bugs_df.reset_index()  # This makes 'uid' a regular column
    bugs = bugs_with_uid.to_dict('records')

    if os.path.exists(dirs_dict["RAW"]):
        logging.info(f"Using the patches data in TMP for {dirs_dict['__base__']} ...")
        patches = pd.read_pickle(dirs_dict["RAW"])
    else:
        logging.info(f"Fetching patches data for {dirs_dict['__base__']} ...")
        if dirs_dict["__base__"] == "developer":
            patches = pd.DataFrame(get_developer_patches(bugs)).set_index("uid")
        elif dirs_dict["__base__"] == "tool":
            patches = pd.DataFrame(get_patches(bugs)).set_index("uid")
        elif dirs_dict["__base__"] == "historian":
            patches = pd.DataFrame(get_historian_dataset(bugs_df)).set_index("uid")
        else:
            raise ValueError(f"Invalid base type: {dirs_dict['__base__']}")
        
        patches.to_pickle(dirs_dict["RAW"])

    logging.info(f"Successfully fetched patches for {dirs_dict['__base__']}. No of patches: {len(patches)}")

    return patches

def clean_and_save_patches(bugs, patches, path):
    if os.path.exists(path):
        cleaned_patches = pd.read_pickle(path)
        return cleaned_patches

    logging.info(f"Cleaning and saving patches to {path} ...") 

    cleaned_patches = patches.copy()
    tqdm.pandas(desc=f"Fixing patches.")
    cleaned_patches["location"] = cleaned_patches.progress_apply(fix_patch, args=(bugs, ), axis=1)
    tqdm.pandas(desc="Unknown Process.")
    cleaned_patches.dropna(subset=['location'], inplace=True)
    cleaned_patches.to_pickle(path)

    no_cleaned_patches = len(cleaned_patches)

    logging.info(f"Successfully cleaned and saved patches: \n{no_cleaned_patches}") 

    return cleaned_patches

def get_methods_and_save(bugs, patches, path):
    if os.path.exists(path):
        cleaned_patches = pd.read_pickle(path)
        return cleaned_patches
    logging.info(f"Getting methods and saving patches to {path} ...")
    patches[['source_methods', 'target_methods']] = patches.progress_apply(lambda row: get_method(row, bugs), axis=1, result_type='expand')
    tqdm.pandas(desc="Unknown Process.")

    logging.info(f"Successfully fetched methods. No Methgod Patches: {len(patches)}")
    logging.info("Dropping patches with no methods ...")

    patches.dropna(subset=['target_methods'], inplace=True)
    patches.to_pickle(path)

    return patches

def get_files_and_save(bugs, patches, path):
    """
    Extract changed files from patches and save them, with caching support.
    
    Args:
        bugs: DataFrame containing bug information
        patches: DataFrame containing patch information
        path: Path to save/load the cached results
    
    Returns:
        DataFrame: Updated patches DataFrame with source_files and target_files columns
    """
    if os.path.exists(path):
        cleaned_patches = pd.read_pickle(path)
        return cleaned_patches
    
    logging.info(f"Getting files and saving patches to {path} ...")
    
    # Add progress bar for applying get_file function
    tqdm.pandas(desc="Extracting Files")
    patches[['source_files', 'target_files']] = patches.progress_apply(
        lambda row: get_file(row, bugs), axis=1, result_type='expand'
    )
    
    logging.info(f"Successfully fetched files. Total patches processed: {len(patches)}")
    logging.info("Dropping patches with no files ...")
    
    # Drop patches where file extraction failed
    patches.dropna(subset=['target_files'], inplace=True)
    
    logging.info(f"Remaining patches after filtering: {len(patches)}")
    
    # Save the results
    patches.to_pickle(path)
    return patches

def get_single_methods_and_save(patches: pd.DataFrame, path: str) -> pd.DataFrame:
    if os.path.exists(path):
        single_method_patches = pd.read_pickle(path)
        return single_method_patches
    
    single_method_patches = patches[patches.apply(is_single_hunk, axis=1)]
    single_method_patches.to_pickle(path)
    return single_method_patches

def normalize_names_and_save(patches, path):
    if os.path.exists(path):
        normalized_patches = pd.read_pickle(path)
        return normalized_patches
    
    # Normalize generator names
    patches['generator_id'] = patches['generator'].str.lower().apply(lambda x: '-'.join(x.split('-')[:-1]) if '-' in x else x)

    # Save normalized patches
    patches.to_pickle(path)

    return patches

def deduplicate_patches_and_save(patches, path):
    logging.info("Deduplicating developer patches ...")
    if os.path.exists(path):
        deduplicated_patches = pd.read_pickle(path)
        return deduplicated_patches

    deduplicated_patches = patches.copy()
    deduplicated_patches['content'] = deduplicated_patches.apply(get_single_hunk_method, axis=1)

    # Remove all white sapces from content for deduplication
    deduplicated_patches['content'] = deduplicated_patches['content'].apply(lambda x: re.sub(r'\s+', '', x) if isinstance(x, str) else x)

    # deduplicated_patches['content'] = deduplicated_patches['target_methods'].apply(lambda x: read_file(x[0]) if len(x) == 1 else None)
    
    # cleaned_tool_patches['content'] = cleaned_tool_patches['location'].apply(read_patch)
    # If there is None raise error
    # if deduplicated_patches['content'].isnull().any():
    #     raise ValueError("There are patches with no single method content.")

    # write removed patches dataframe to html 
    # Find the duplicates before deduplication (for comparison)
    # duplicates = deduplicated_patches[deduplicated_patches.duplicated(subset=['bug_uid', 'generator_id', 'content'], keep=False)]

    # Perform deduplication
    deduplicated_patches = deduplicated_patches.drop_duplicates(subset=['bug_uid', 'generator_id', 'content'])

    # Log number of duplicates removed
    num_duplicates = len(patches) - len(deduplicated_patches)
    logging.info(f"Removed {num_duplicates} duplicate patches")
    logging.info(f"Original patches: {len(patches)}, Deduplicated patches: {len(deduplicated_patches)}")


    # Save to HTML files
    # duplicates.to_html('removed_duplicates.html', index=False, escape=False)
    # deduplicated_patches.to_html('deduplicated_patches.html', index=False, escape=False)

    deduplicated_patches = deduplicated_patches.drop(columns=['content'])
    deduplicated_patches.to_pickle(path)
    return deduplicated_patches

def preprocess_patches(bugs, patches, dirs_dict):
    if GET_SUDO:
        # Just force sudo
        bug = bugs.loc['defects4j-Closure-63'].copy()
        bug['uid'] = bug.name
        checkout_dir = checkout_bug(bug)
        if os.path.exists(checkout_dir):
            shutil.rmtree(checkout_dir)


    cleaned_patches = clean_and_save_patches(bugs, patches, dirs_dict["CLEANED"])
    method_patches = get_methods_and_save(bugs, cleaned_patches, dirs_dict["METHOD"])
    
    if EXTRACT_FILES:
        file_patches = get_files_and_save(bugs, method_patches, dirs_dict["FILES"])
        normalized_patches = normalize_names_and_save(file_patches, dirs_dict["NORMALIZED"])

    else:
        normalized_patches = normalize_names_and_save(method_patches, dirs_dict["NORMALIZED"])

    single_method_patches = get_single_methods_and_save(normalized_patches, dirs_dict["SINGLE_METHOD"])
    deduplicated_patches = deduplicate_patches_and_save(single_method_patches, dirs_dict["DEDUPLICATED"])

    logging.info(f"Finished preprocessing patches for {dirs_dict['__base__']}. Final count: {len(deduplicated_patches)}")
    logging.info(f"Cleaned patches: {len(cleaned_patches)}, Method patches: {len(method_patches)}, Normalized patches: {len(normalized_patches)}, Single method patches: {len(single_method_patches)}, Deduplicated patches: {len(deduplicated_patches)}")

    return deduplicated_patches

def get_data():
    bugs = fetch_bugs()
    print(f"Fetched {len(bugs)} bugs.")
    
    developer_patches = fetch_patches(bugs, SETUP_DEVELOPER_PATCHES_DIR)
    print(f"Fetched {len(developer_patches)} developer patches.")
    developer_patches = preprocess_patches(bugs, developer_patches, SETUP_DEVELOPER_PATCHES_DIR)
    print(f"Preprocessed developer patches. Final count: {len(developer_patches)}")

    tool_patches = fetch_patches(bugs, SETUP_TOOL_PATCHES_DIR)
    print(f"Fetched {len(tool_patches)} tool patches.")
    tool_patches = preprocess_patches(bugs, tool_patches, SETUP_TOOL_PATCHES_DIR)
    print(f"Preprocessed tool patches. Final count: {len(tool_patches)}")
    
    historian_patches = fetch_patches(bugs, SETUP_HISTORIAN_PATCHES_DIR)
    print(f"Fetched {len(historian_patches)} historian patches.")
    historian_patches = preprocess_patches(bugs, historian_patches, SETUP_HISTORIAN_PATCHES_DIR)
    print(f"Preprocessed historian patches. Final count: {len(historian_patches)}")

    return bugs, developer_patches, tool_patches, historian_patches

# Get all parameters and apply passed params
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

def get_params():
    # Patch Processings
    patch_processors = get_patch_processors()

    # Tool Settings
    prompts, models, temperatures = get_tool_settings()

    # Apply Passed Params
    args = parse_args()
    prompts, models, patch_processors = apply_params(args, prompts, models, patch_processors)

    return prompts, models, temperatures, patch_processors

    
if __name__=="__main__": 
    logging.info("Running build.py ...")

    # Initial Data
    bugs, developer_patches, tool_patches, historian_patches = get_data()
    prompts, models, temperatures, patch_processors = get_params()

    print(
        f"""
        Successfully fetched and preprocessed data. Summary:
        ===========================
        Bugs: {len(bugs)},
        Developer Patches: {len(developer_patches)},
        Tool Patches: {len(tool_patches)},
        Historian Patches: {len(historian_patches)},
        ===========================
        Prompts:  {len(prompts)},
        ===========================
        Models: {len(models)},
        ===========================
        Temperatures: {len(temperatures)},
        ===========================
        Patch Processors: {len(patch_processors)}
        """
    )

    
