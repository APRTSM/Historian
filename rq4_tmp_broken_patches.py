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

# Returns methods
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

def get_single_methods_and_save(patches: pd.DataFrame, path: str) -> pd.DataFrame:
    if os.path.exists(path):
        single_method_patches = pd.read_pickle(path)
        return single_method_patches
    
    single_method_patches = patches[patches.apply(is_single_hunk, axis=1)]
    single_method_patches.to_pickle(path)
    return single_method_patches

# Deduplicate
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
    if os.path.exists(path):
        deduplicated_patches = pd.read_pickle(path)
        return deduplicated_patches

    deduplicated_patches = patches.copy()
    deduplicated_patches['content'] = deduplicated_patches['location'].apply(lambda x: read_patch(x[0]) if isinstance(x, list) and len(x) > 0 else None)
    deduplicated_patches = deduplicated_patches.drop_duplicates(subset=['bug_uid', 'generator_id', 'content'])
    deduplicated_patches = deduplicated_patches.drop(columns=['content'])
    deduplicated_patches.to_pickle(path)
    return deduplicated_patches

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

"""
Get New Patches
Preprocess New Patches
First Step Compare Each New Patch with Existing Groundtruth Patches (Do it in Pare Checking Way Not File)
"""
def experiment_4(new_patches, developer_patches, tool_patches, models, prompts, temperatures, patch_processors):
    def get_response(groundtruth_patch, new_patch, prompt, temperature, model, processor):
        new_patch_content = processor["function"](new_patch) 
        groundtruth_patch_content = processor["function"](groundtruth_patch) 

        prompt_content = prompt["content"]

        content = f"""
            {prompt_content}

            Patch 1: {groundtruth_patch_content}

            Patch 2: {new_patch_content}
        """
        response = ollama.chat(model=model["uid"], keep_alive=-1, options=ollama.Options(temperature=temperature["uid"]), messages=[
            {
                "role": "system",
                "content": content,
            },
        ])

        label = {
            "new_patch_uid": new_patch.name,
            "groundtruth_patch_uid": groundtruth_patch.name,
            "processor": processor["uid"],
            "model": model["uid"],
            "temperature": temperature["uid"],
            "prompt": prompt["uid"],
            "response": response["message"]["content"],
            "time": int(time.time())
        }

        return pd.Series(label)
    
    def compare_groundtruth(new_patch, groundtruth, prompt, temperature, model, processor, existing_pairs):
        groundtruth_selected_bug = groundtruth.loc[groundtruth["bug_uid"] == new_patch["bug_uid"]]

        logging.info(f"new_patch: {new_patch.name}, no_selected_bug_groundtruth_patches: {len(groundtruth_selected_bug)}")

        results = []
        for _, gt_patch in groundtruth_selected_bug.iterrows():
            # Check if this pair already exists in the results
            pair_key = (new_patch.name, gt_patch.name)
            if pair_key not in existing_pairs:
                result = get_response(gt_patch, new_patch, prompt, temperature, model, processor)
                results.append(result)
            else:
                logging.info(f"Pair already exists: new_patch={new_patch.name}, groundtruth={gt_patch.name}. Skipping.")
        
        if results:
            return pd.DataFrame(results)
        else:
            return pd.DataFrame()

    # Create groundtruth from developer_patches and tool_patches (not discarding any)
    groundtruth_patches = pd.concat([tool_patches, developer_patches], axis=0)

    no_groundtruth_patches = len(groundtruth_patches)
    no_new_patches = len(new_patches)
    no_models = len(models)
    no_prompts = len(prompts)
    no_temperatures = len(temperatures)
    logging.info(f"Running experiment 4 ... no_models: {no_models}, no_prompts: {no_prompts}, no_groundtruth_patches: {no_groundtruth_patches}, no_new_patches: {no_new_patches}, no_temperatures: {no_temperatures}")

    for processor in patch_processors:
        for model in models:
            for temperature in temperatures:
                for prompt in prompts:
                    prompt_uid = prompt["uid"]
                    temperature_value = temperature["uid"]
                    model_uid = model["uid"]
                    processor_uid = processor["uid"]
                    result_file = os.path.join(TMP_RESULTS_DIR, f"EXP4-{processor_uid}-{model_uid}-{temperature_value}-{prompt_uid}.pkl")

                    # Load existing results if file exists
                    existing_pairs = set()
                    if os.path.exists(result_file):
                        logging.info(f"Loading existing results from {result_file}")
                        existing_results = pd.read_pickle(result_file)
                        # Create set of existing pairs (new_patch_uid, groundtruth_patch_uid)
                        existing_pairs = set(zip(existing_results["new_patch_uid"], existing_results["groundtruth_patch_uid"]))
                        logging.info(f"Found {len(existing_pairs)} existing pairs")

                    all_results = []
                    if os.path.exists(result_file):
                        all_results.append(existing_results)

                    for i, (_, new_patch) in enumerate(tqdm(new_patches.iterrows(), total=len(new_patches), desc=f"Processing the patch ... PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid}")):
                        logging.info(f"Processing the patch ... PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid}, index: {i}")

                        results = compare_groundtruth(new_patch, groundtruth_patches, prompt, temperature, model, processor, existing_pairs)
                        
                        if not results.empty:
                            all_results.append(results)
                            # Update existing_pairs with newly processed pairs
                            new_pairs = set(zip(results["new_patch_uid"], results["groundtruth_patch_uid"]))
                            existing_pairs.update(new_pairs)
                            
                            # Save incrementally after each new_patch
                            combined_results = pd.concat(all_results, ignore_index=True)
                            combined_results.to_pickle(result_file)

            ollama.generate(model=model["uid"], keep_alive=0)





if __name__=="__main__": 
    logging.info("Running rq4_llms.py ...")

    """ Setup and Initial Data Fetching """

    # Initial Data
    bugs, developer_patches, tool_patches = init(configure=False)
    new_patches = pd.DataFrame(get_historian_dataset(bugs)).set_index("uid")

    # Patch Processings
    patch_processors = get_patch_processors()

    # Tool Settings
    prompts, models, temperatures = get_tool_settings()

    # Apply Passed Params
    args = parse_args()
    prompts, models, patch_processors = apply_params(args, prompts, models, patch_processors)

    """ Groundtruth Preprocessing """

    print("Raw Developer Patches:")
    print(len(developer_patches))
    print("Raw Tool Patches:")
    print(len(tool_patches))

    # Read txt file
    with open("rq4/failed_patches.txt", "r") as f:
        failed_patch_ids = [line.strip() for line in f.readlines()]

    failed_patches = tool_patches.loc[tool_patches.index.isin(failed_patch_ids)]
    print("Failed Tool Patches:")
    print(len(failed_patches))
    
    
    # Iterate through failed pathces
    for index, failed_patch in failed_patches.iterrows():
        location = failed_patch['location']
        uid = failed_patch.name
        with open(location, 'r') as f:
            content = f.read()
        with open(os.path.join("rq4", "failed_patches", f"{uid}.patch"), 'w') as f:
            f.write(content)    