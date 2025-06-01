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
from itertools import combinations


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
    logging.info("Testing tool patches ...") 

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

    logging.info(f"Successfully tested tool patches: \n{no_cleaned_developer_patches} \n{no_cleaned_tool_patches}") 

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
        cleaned_tool_patches['content'] = cleaned_tool_patches['location'].apply(read_patch)
        tqdm.pandas(desc="Deduplicating patches")
        cleaned_tool_patches = cleaned_tool_patches.drop_duplicates(subset=['bug_uid', 'generator_id', 'content']) 
        cleaned_tool_patches = cleaned_tool_patches.drop(columns=['content'])
        cleaned_tool_patches.to_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)

    correct_tool_patches = cleaned_tool_patches[cleaned_tool_patches['correctness'] == 'Correct']
    overfitting_tool_patches = cleaned_tool_patches[cleaned_tool_patches['correctness'] == 'Overfitting']
    logging.info(f"Successfully deduplicated tool patches. No of Correct tool patches: {len(correct_tool_patches)}")
    logging.info(f"Filtered tool patches to only include Defects4J bugs. No of tool patches: {cleaned_tool_patches[cleaned_tool_patches['bug_uid'].str.contains('defects4j', case=False, na=False)]}")
    logging.info(f"Successfully deduplicated tool patches. No of Overfitting tool patches: {len(overfitting_tool_patches)}")

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
    

# Compare input patches with existing patches
def experiment_5(developer_patches, tool_patches, models, temperatures, patch_processors):
    def get_embedding(patch_content, model, temperature):
        """Get embedding for a patch using ollama API"""
        response = ollama.embeddings(
            model=model["uid"],
            prompt=patch_content,
            options=ollama.Options(temperature=temperature["uid"])
        )
        return np.array(response["embedding"])
    
    def calculate_cosine_distance(groundtruth_patch, tool_patch, model, temperature, processor):
        """Calculate cosine distance between embeddings of two patches"""
        tool_patch_content = processor["function"](tool_patch)
        groundtruth_patch_content = processor["function"](groundtruth_patch)
        
        tool_embedding = get_embedding(tool_patch_content, model, temperature)
        groundtruth_embedding = get_embedding(groundtruth_patch_content, model, temperature)
        
        # Calculate cosine similarity
        # Guard Fix
        if tool_embedding.size == 0 or groundtruth_embedding.size == 0: return pd.Series({"cosine_distance": np.nan, "tool_patch_uid": tool_patch.name, "groundtruth_patch_uid": groundtruth_patch.name, "processor": processor["uid"], "model": model["uid"], "temperature": temperature["uid"], "tool_embedding": [], "groundtruth_embedding": [], "time": int(time.time())})

        similarity = cosine_similarity([tool_embedding], [groundtruth_embedding])[0][0]
        # Convert to distance (1 - similarity)
        distance = 1.0 - similarity
        
        label = {
            "tool_patch_uid": tool_patch.name,
            "groundtruth_patch_uid": groundtruth_patch.name,
            "processor": processor["uid"],
            "model": model["uid"],
            "temperature": temperature["uid"],
            "cosine_distance": distance,
            "tool_embedding": tool_embedding.tolist(),
            "groundtruth_embedding": groundtruth_embedding.tolist(),
            "time": int(time.time())
        }
        
        return pd.Series(label)
    
    def compare_embeddings(tool_patch, groundtruth, model, temperature, processor):
        """Compare embeddings between a tool patch and all corresponding groundtruth patches"""
        groundtruth_selected_bug = groundtruth.loc[groundtruth["bug_uid"] == tool_patch["bug_uid"]]
        
        logging.info(f"tool_patch: {tool_patch.name}, no_selected_bug_groundtruth_patches: {len(groundtruth_selected_bug)}")
        
        results = groundtruth_selected_bug.apply(
            lambda row: calculate_cosine_distance(row, tool_patch, model, temperature, processor), 
            axis=1
        )
        
        return results
    
    # Select TBar patches
    selected_tool = "tbar"
    selected_tool_patches = tool_patches[tool_patches["generator_id"].str.lower().str.contains(selected_tool)]
    
    # Exclude TBar from groundtruth patches
    tool_patches_without_tbar = tool_patches[~tool_patches["generator_id"].str.lower().str.contains(selected_tool)]
    
    # Keep single hunks
    selected_tool_patches = selected_tool_patches[selected_tool_patches.apply(is_single_hunk, axis=1)]
    tool_patches_without_tbar = tool_patches_without_tbar[tool_patches_without_tbar.apply(is_single_hunk, axis=1)]

    logging.info(f"------------------------------------")
    logging.info(f"Single Hunk TBar Patches: {len(selected_tool_patches)}")
    logging.info(f"Single Hunk Other Tool Patches: {len(tool_patches_without_tbar)}")
    logging.info(f"------------------------------------")

    # Remove Developer Exact Matches Using are_codes_identical Function
    selected_tool_patches = selected_tool_patches[selected_tool_patches.apply(lambda row: not is_in_list(row, developer_patches), axis=1)]
    tool_patches_without_tbar = tool_patches_without_tbar[tool_patches_without_tbar.apply(lambda row: not is_in_list(row, developer_patches), axis=1)]
    
    logging.info(f"------------------------------------")
    logging.info(f"Single Hunk TBar Patches: {len(selected_tool_patches)}")
    logging.info(f"Single Hunk Other Tool Patches: {len(tool_patches_without_tbar)}")
    logging.info(f"------------------------------------")
    
    # Create groundtruth from developer patches and non-TBar tool patches
    groundtruth_patches = tool_patches_without_tbar
    
    no_groundtruth_patches = len(groundtruth_patches)
    no_selected_tool_patches = len(selected_tool_patches)
    no_models = len(models)
    no_temperatures = len(temperatures)
    no_processors = len(patch_processors)
    
    logging.info(f"Running experiment 3 ... selected_tool: {selected_tool}, no_models: {no_models}, "
                f"no_groundtruth_patches: {no_groundtruth_patches}, no_selected_tool_patches: {no_selected_tool_patches}, "
                f"temperatures: {no_temperatures}, processors: {no_processors}")
    
    # Create indices for comparisons
    comparison_indices = selected_tool_patches.apply(
        lambda tool_patch: pd.Series(
            groundtruth_patches[groundtruth_patches["bug_uid"] == tool_patch["bug_uid"]].index,
            name=tool_patch.name
        ),
        axis=1
    ).stack().reset_index(level=1, drop=True).reset_index(name='groundtruth_index')
    
    comparison_indices.to_pickle(os.path.join(TMP_EXPERT_LABEL_DIR, f"EXP5-embedding-comparison-{selected_tool}.pkl"))
    
    for processor in patch_processors:
        for model in models:
            for temperature in temperatures:
                processor_uid = processor["uid"]
                model_uid = model["uid"]
                temperature_value = temperature["uid"]
                
                result_file = os.path.join(TMP_RESULTS_DIR, f"EXP5-{selected_tool}-{processor_uid}-{model_uid}-{temperature_value}.pkl")
                
                if os.path.exists(result_file):
                    logging.info(f"Results already exist. SelectedTool: {selected_tool} PatchProcessor: {processor_uid} "
                                f"model: {model_uid} temperature: {temperature_value} \n Skipping to the next one.")
                    continue
                
                all_results = []
                
                for i, (_, tool_patch) in enumerate(tqdm(selected_tool_patches.iterrows(), 
                                                        total=len(selected_tool_patches), 
                                                        desc=f"EXP5 Processing embeddings... selected_tool: {selected_tool}, "
                                                            f"PatchProcessor: {processor_uid} model: {model_uid} "
                                                            f"temperature: {temperature_value}")):
                    
                    batch_result_file = os.path.join(TMP_RESULTS_DIR, 
                                                    f"EXP5-{selected_tool}-{processor_uid}-{model_uid}-{temperature_value}-{i}.pkl")
                    
                    if os.path.exists(batch_result_file):
                        logging.info(f"Batch results already exist. SelectedTool: {selected_tool} "
                                    f"PatchProcessor: {processor_uid} model: {model_uid} "
                                    f"temperature: {temperature_value} index: {i} \n Loading existing results.")
                        batch_results = pd.read_pickle(batch_result_file)
                        all_results.append(batch_results)
                        continue
                    
                    logging.info(f"Processing embeddings... selected_tool: {selected_tool}, "
                                f"PatchProcessor: {processor_uid} model: {model_uid} "
                                f"temperature: {temperature_value}, index: {i}")
                    
                    batch_results = compare_embeddings(tool_patch, groundtruth_patches, model, temperature, processor)
                    batch_results.to_pickle(batch_result_file)
                    all_results.append(batch_results)
                
                # # Combine all results and save
                # if all_results:
                #     combined_results = pd.concat(all_results, axis=0)
                #     combined_results.to_pickle(result_file)
                
                # Release model resources
                ollama.generate(model=model["uid"], keep_alive=0)

def experiment_2(developer_patches, tool_patches, models, prompts, temperatures, patch_processors):
    def get_response(groundtruth_patch, tool_patch, prompt, temperature, model, processor):
        tool_patch_content = processor["function"](tool_patch) 

        # Select the developer patch with same bug_uid
        groundtruth_patch_content = processor["function"](groundtruth_patch) 

        prompt_content = prompt["content"]

        content = f"""
            {prompt_content}

            Patch 1: {groundtruth_patch_content}

            Patch 2: {tool_patch_content}
        """
        response = ollama.chat(model=model["uid"], keep_alive=-1, options=ollama.Options(temperature=temperature["uid"]), messages=[
            {
                "role": "system",
                "content": content,
            },
        ])

        # Continue before this
        label = {
            "tool_patch_uid": tool_patch.name,
            "groundtruth_patch_uid": groundtruth_patch.name,
            "processor": processor["uid"],
            "model": model["uid"],
            "temperature": temperature["uid"],
            "prompt": prompt["uid"],
            "response": response["message"]["content"],
            "time": int(time.time())
        }

        return pd.Series(label)
    
    def compare_groundtruth(tool_patch, groundtruth, prompt, temperature, model, processor):
        groundtruth_selected_bug = groundtruth.loc[groundtruth["bug_uid"] == tool_patch["bug_uid"]]

        logging.info(f"tool_patch: {tool_patch.name}, no_selected_bug_groundtruth_patches: {len(groundtruth_selected_bug)}")

        results = groundtruth_selected_bug.apply(lambda row: get_response(row, tool_patch, prompt, temperature, model, processor), axis=1)

        return results

    # Exclude Selected Tool
    selected_tool = "tbar"
    selected_tool_patches = tool_patches[tool_patches["generator_id"].str.lower().str.contains(selected_tool)]
    tool_patches = tool_patches[~tool_patches["generator_id"].str.lower().str.contains(selected_tool)]

    # # Exclude overfitting patches
    # tool_patches = tool_patches[tool_patches["correctness"] == "Correct"]

    logging.info(f"------------------------------------")
    logging.info(f"Tool Generated Patches: {len(tool_patches)}")
    logging.info(f"Developer Patches: {len(developer_patches)}")
    logging.info(f"Selected Patches: {len(selected_tool_patches)}")
    logging.info(f"------------------------------------")

    # Keep single hunks
    tool_patches = tool_patches[tool_patches.apply(is_single_hunk, axis=1)]
    developer_patches = developer_patches[developer_patches.apply(is_single_hunk, axis=1)]
    selected_tool_patches = selected_tool_patches[selected_tool_patches.apply(is_single_hunk, axis=1)]

    logging.info(f"------------------------------------")
    logging.info(f"Single Hunk Tool Generated Patches: {len(tool_patches)}")
    logging.info(f"Single Hunk Developer Patches: {len(developer_patches)}")
    logging.info(f"Single Hunk Selected Patches: {len(selected_tool_patches)}")
    logging.info(f"------------------------------------")

    # Create extended groundtruth
    groundtruth_patches = pd.concat([tool_patches, developer_patches], axis=0)

    no_groundtruth_patches = len(groundtruth_patches)
    no_selected_tool_patches = len(selected_tool_patches)
    no_models = len(models)
    no_prompts = len(prompts)
    no_temperatures = len(temperatures)
    logging.info(f"Running experiment 2 ... selected_tool: {selected_tool}, no_models: {no_models}, no_prompts: {no_prompts}, no_correct_patches: {no_groundtruth_patches}, no_selected_tool_patches: {no_selected_tool_patches}, temperature: {no_temperatures}")

    comparison_indices = selected_tool_patches.apply(
        lambda tool_patch: pd.Series(
            groundtruth_patches[groundtruth_patches["bug_uid"] == tool_patch["bug_uid"]].index,
            name=tool_patch.name
        ),
        axis=1
    ).stack().reset_index(level=1, drop=True).reset_index(name='groundtruth_index')

    comparison_indices.to_pickle(os.path.join(TMP_EXPERT_LABEL_DIR, f"EXP2-unlabeled-{selected_tool}.pkl"))

    for processor in patch_processors:
        for model in models:
            for temperature in temperatures:
                for prompt in prompts:
                    prompt_uid = prompt["uid"]
                    temperature_value = temperature["uid"]
                    model_uid = model["uid"]
                    processor_uid = processor["uid"]
                    result_file = os.path.join(TMP_RESULTS_DIR, f"EXP2-{selected_tool}-{processor_uid}-{model_uid}-{temperature_value}-{prompt_uid}.pkl")

                    if os.path.exists(result_file):
                        logging.info(f"Results already exist. SelectedTool: {selected_tool} PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid} \n Skipping to the next one.")
                        
                        continue

                    for i, (_, tool_patch) in enumerate(tqdm(selected_tool_patches.iterrows(), total=len(selected_tool_patches), desc=f"Processing the patch ... selected_tool: {selected_tool}, PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid}")):
                        result_file = os.path.join(TMP_RESULTS_DIR, f"EXP2-{selected_tool}-{processor_uid}-{model_uid}-{temperature_value}-{prompt_uid}-{i}.pkl")

                        if os.path.exists(result_file):
                            logging.info(f"Results already exist. SelectedTool: {selected_tool} PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid} index: {i} \n Skipping to the next one.")
                            
                            continue

                        logging.info(f"Processing the patch ... selected_tool: {selected_tool}, PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid}, index: {i}")

                        results = compare_groundtruth(tool_patch, groundtruth_patches, prompt, temperature, model, processor)
                        results.to_pickle(result_file)

            ollama.generate(model=model["uid"], keep_alive=0)

def experiment_1(developer_patches, tool_patches, models, prompts, temperatures, patch_processors):
    def get_response(patch, prompt, temperature, model, processor):
        tool_content = processor["function"](patch) 

        # Select the developer patch with same bug_uid
        developer_patch = developer_patches.loc[developer_patches["bug_uid"] == patch["bug_uid"]].iloc[0]
        developer_content = processor["function"](developer_patch) 

        prompt_content = prompt["content"]

        content = f"""
            {prompt_content}

            Patch 1: {developer_content}

            Patch 2: {tool_content}
        """
        response = ollama.chat(model=model["uid"], keep_alive=-1, options=ollama.Options(temperature=temperature["uid"]), messages=[
            {
                "role": "system",
                "content": content,
            },
        ])

        # Continue before this
        label = {
            "patch_uid": patch.name,
            "processor": processor["uid"],
            "model": model["uid"],
            "temperature": temperature["uid"],
            "prompt": prompt["uid"],
            "response": response["message"]["content"],
            "time": int(time.time())
        }

        return pd.Series(label)

    correct_patches = tool_patches[tool_patches["correctness"] == "Correct"]
    correct_patches = correct_patches[correct_patches.apply(lambda patch: are_single_hunks(patch, developer_patches), axis=1)]
    no_correct_patches = len(correct_patches)
    no_models = len(models)
    no_prompts = len(prompts)
    no_temperatures = len(temperatures)
    logging.info(f"Running experiment 1 ... no_models: {no_models}, no_prompts: {no_prompts}, no_correct_patches: {no_correct_patches}, temperature: {no_temperatures}")

    for processor in patch_processors:
        for model in models:
            for temperature in temperatures:
                for prompt in prompts:
                    prompt_uid = prompt["uid"]
                    temperature_value = temperature["uid"]
                    model_uid = model["uid"]
                    processor_uid = processor["uid"]
                    result_file = os.path.join(TMP_RESULTS_DIR, f"EXP1-{processor_uid}-{model_uid}-{temperature_value}-{prompt_uid}.pkl")

                    if os.path.exists(result_file):
                        logging.info(f"Results already exist. PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid} \n Skipping to the next one.")
                        
                        continue

                    logging.info(f"Processing the patches ... PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid}")
                    tqdm.pandas(desc=f"Processing the patches ... PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid}")
                    results = correct_patches.progress_apply(get_response, args=(prompt, temperature, model, processor), axis=1)
                    tqdm.pandas(desc="Unknown Process.")
                    results.to_pickle(result_file)

            ollama.generate(model=model["uid"], keep_alive=0)

def report_dataset(cleaned_developer_patches, cleaned_tool_patches, bugs):
    # Reports single hunk, and identical seperately
    logging.info("--- Reporting dataset ---")
    # Make copies to avoid modifying input dataframes
    developer_patches = cleaned_developer_patches.copy()
    tool_patches = cleaned_tool_patches.copy()
    
    logging.info("Reporting dataset ... Tool-devided, Developer-Provided, and Correctness, being single hunk")
    logging.info(f"No of bugs: {len(bugs)}, No of developer patches: {len(developer_patches)}, No of tool patches: {len(tool_patches)}, No of correct tool patches: {len(tool_patches[tool_patches['correctness'] == 'Correct'])}")

    # ===== Summary Table Report =====
    combined_patches = pd.concat([tool_patches, developer_patches])
    combined_patches.reset_index(drop=True, inplace=True)
    merged_data = combined_patches.merge(
        bugs,
        left_on="bug_uid",
        right_index=True,
        how="left"
    )

    summary_table = (
        merged_data.groupby(['generator', 'benchmark', 'correctness'])
            .size()
            .unstack(fill_value=0)
            .reset_index()
    )

    summary_table['C/O'] = (
        summary_table.get('Correct', 0).astype(str) + '/' + summary_table.get('Overfitting', 0).astype(str)
    )

    final_table = summary_table.pivot(index='generator', columns='benchmark', values='C/O')
    final_table = final_table.fillna('0/0')

    logging.info("\nSummary Table (Correct/Overfitting by Generator x Benchmark):")
    logging.info(final_table)

    # ===== Developer-Identical Patches Report =====
    logging.info("\n--- Developer-Identical Patches Report ---")
    
    # Read patch content for comparison
    developer_patches['content'] = developer_patches['location'].apply(read_patch)
    tool_patches['content'] = tool_patches['location'].apply(read_patch)
    
    # Find tool patches identical to developer patches
    dev_identical_patches = []
    non_dev_identical_patches = []
    
    for _, tool_patch in tool_patches.iterrows():
        match = developer_patches[
            (developer_patches['bug_uid'] == tool_patch['bug_uid']) &
            (developer_patches['content'] == tool_patch['content'])
        ]
        if not match.empty:
            dev_identical_patches.append(tool_patch)
        else:
            non_dev_identical_patches.append(tool_patch)
    
    dev_identical_df = pd.DataFrame(dev_identical_patches) if dev_identical_patches else pd.DataFrame()
    non_dev_identical_df = pd.DataFrame(non_dev_identical_patches) if non_dev_identical_patches else pd.DataFrame()
    
    logging.info(f"Total tool patches: {len(tool_patches)}")
    logging.info(f"Developer-identical patches: {len(dev_identical_df)}")
    logging.info(f"Non-developer-identical patches: {len(non_dev_identical_df)}")
    
    if len(dev_identical_df) > 0:
        # Breakdown by generator
        logging.info("\nDeveloper-identical patches by generator:")
        dev_identical_by_gen = dev_identical_df.groupby('generator_id').size()
        for gen, count in dev_identical_by_gen.items():
            logging.info(f"  {gen}: {count}")
        
        # Check correctness of developer-identical patches
        if 'correctness' in dev_identical_df.columns:
            correctness_count = dev_identical_df['correctness'].value_counts()
            logging.info("\nCorrectness of developer-identical patches:")
            for correctness, count in correctness_count.items():
                logging.info(f"  {correctness}: {count}")
            
            # Flag if any overfitting patches are identical to developer patches
            overfitting_identical = dev_identical_df[dev_identical_df['correctness'] == 'Overfitting']
            if len(overfitting_identical) > 0:
                logging.warning(f"WARNING: {len(overfitting_identical)} overfitting patches are identical to developer patches!")
                for _, patch in overfitting_identical.iterrows():
                    logging.warning(f"  Bug: {patch['bug_uid']}, Generator: {patch['generator_id']}")

    # ===== Single Hunk Reporting =====
    logging.info("\n--- Single Hunk Patch Reporting ---")

    # Add single hunk info columns using both checks
    developer_patches['is_single_hunk'] = developer_patches.apply(is_single_hunk, axis=1)
    developer_patches['are_single_hunks'] = developer_patches.apply(
        lambda patch: are_single_hunks(patch, developer_patches), axis=1
    )

    tool_patches['is_single_hunk'] = tool_patches.apply(is_single_hunk, axis=1)
    tool_patches['are_single_hunks'] = tool_patches.apply(
        lambda patch: are_single_hunks(patch, developer_patches), axis=1
    )

    # Developer single hunk patches (with both methods)
    dev_single_hunk_is = developer_patches[developer_patches['is_single_hunk']]
    dev_single_hunk_are = developer_patches[developer_patches['are_single_hunks']]
    logging.info(f"Developer patches: {len(developer_patches)} total")
    logging.info(f"  - {len(dev_single_hunk_is)} are single-hunk (is_single_hunk)")
    logging.info(f"  - {len(dev_single_hunk_are)} are single-hunk (are_single_hunks)")

    # Tool-generated single hunk patches (all)
    tool_single_hunk_is = tool_patches[tool_patches['is_single_hunk']]
    tool_single_hunk_is_correct = tool_single_hunk_is[tool_single_hunk_is['correctness'] == 'Correct']
    tool_single_hunk_are = tool_patches[tool_patches['are_single_hunks']]
    tool_single_hunk_are_correct = tool_single_hunk_are[tool_single_hunk_are['correctness'] == 'Correct']
    logging.info(f"Tool patches: {len(tool_patches)} total")
    logging.info(f"  - {len(tool_single_hunk_is)} are single-hunk (is_single_hunk)")
    logging.info(f"  - {len(tool_single_hunk_is_correct)} are single-hunk (is_single_hunk) and correct")
    logging.info(f"  - {len(tool_single_hunk_are)} are single-hunk (are_single_hunks)")
    logging.info(f"  - {len(tool_single_hunk_are_correct)} are single-hunk (are_single_hunks) and correct")

    # Single hunk analysis for non-developer-identical patches
    if len(non_dev_identical_df) > 0:
        non_dev_identical_df['is_single_hunk'] = non_dev_identical_df.apply(is_single_hunk, axis=1)
        non_dev_identical_df['are_single_hunks'] = non_dev_identical_df.apply(
            lambda patch: are_single_hunks(patch, developer_patches), axis=1
        )
        
        non_dev_single_hunk_is = non_dev_identical_df[non_dev_identical_df['is_single_hunk']]
        non_dev_single_hunk_are = non_dev_identical_df[non_dev_identical_df['are_single_hunks']]
        
        logging.info(f"\nNon-developer-identical tool patches: {len(non_dev_identical_df)} total")
        logging.info(f"  - {len(non_dev_single_hunk_is)} are single-hunk (is_single_hunk)")
        logging.info(f"  - {len(non_dev_single_hunk_are)} are single-hunk (are_single_hunks)")

    # Correct tool patches - single hunk
    correct_patches = tool_patches[tool_patches["correctness"] == "Correct"]
    correct_single_hunk_is = correct_patches[correct_patches['is_single_hunk']]
    correct_single_hunk_are = correct_patches[correct_patches['are_single_hunks']]
    
    # Calculate non-developer-identical correct patches that are single hunk
    correct_non_dev_identical_single_hunk_is = 0
    correct_non_dev_identical_single_hunk_are = 0
    
    for _, patch in correct_patches.iterrows():
        # Check if patch is developer-identical
        match = developer_patches[
            (developer_patches['bug_uid'] == patch['bug_uid']) &
            (developer_patches['content'] == patch['content'])
        ]
        # If not developer-identical, check if it's single hunk
        if match.empty:
            if patch['is_single_hunk']:
                correct_non_dev_identical_single_hunk_is += 1
            if patch['are_single_hunks']:
                correct_non_dev_identical_single_hunk_are += 1
    
    logging.info(f"\nCorrect tool patches: {len(correct_patches)} total")
    logging.info(f"  - {len(correct_single_hunk_is)} are single-hunk (is_single_hunk)")
    logging.info(f"  - {len(correct_single_hunk_are)} are single-hunk (are_single_hunks)")
    logging.info(f"  - {correct_non_dev_identical_single_hunk_is} non-developer-identical are single-hunk (is_single_hunk)")
    logging.info(f"  - {correct_non_dev_identical_single_hunk_are} non-developer-identical are single-hunk (are_single_hunks)")

    # Overfitting tool patches - single hunk
    overfitting_patches = tool_patches[tool_patches["correctness"] == "Overfitting"]
    overfitting_single_hunk_is = overfitting_patches[overfitting_patches['is_single_hunk']]
    overfitting_single_hunk_are = overfitting_patches[overfitting_patches['are_single_hunks']]

    # Calculate non-developer-identical overfitting patches that are single hunk
    overfitting_non_dev_identical_single_hunk_is = 0
    overfitting_non_dev_identical_single_hunk_are = 0

    for _, patch in overfitting_patches.iterrows():
        # Check if patch is developer-identical
        match = developer_patches[
            (developer_patches['bug_uid'] == patch['bug_uid']) &
            (developer_patches['content'] == patch['content'])
        ]
        # If not developer-identical, check if it's single hunk
        if match.empty:
            if patch['is_single_hunk']:
                overfitting_non_dev_identical_single_hunk_is += 1
            if patch['are_single_hunks']:
                overfitting_non_dev_identical_single_hunk_are += 1

    logging.info(f"Overfitting tool patches: {len(overfitting_patches)} total")
    logging.info(f"  - {len(overfitting_single_hunk_is)} are single-hunk (is_single_hunk)")
    logging.info(f"  - {len(overfitting_single_hunk_are)} are single-hunk (are_single_hunks)")
    logging.info(f"  - {overfitting_non_dev_identical_single_hunk_is} non-developer-identical are single-hunk (is_single_hunk)")
    logging.info(f"  - {overfitting_non_dev_identical_single_hunk_are} non-developer-identical are single-hunk (are_single_hunks)")

    # ===== Combined Summary Table =====
    logging.info("\n--- Combined Summary Table ---")
    
    combined_summary = []
    
    # Add developer patches
    dev_row = {
        'Generator': 'Developer',
        'Total': len(developer_patches),
        'Dev-Identical': 0,  # Developers are always dev-identical
        'Non-Dev-Identical': 0,  # N/A for developers
        'is_single_hunk': len(developer_patches[developer_patches['is_single_hunk']]),
        'are_single_hunks': len(developer_patches[developer_patches['are_single_hunks']]),
        'Correctness': 'N/A'
    }
    combined_summary.append(dev_row)
    
    # Add tool patches by generator and correctness
    for generator in tool_patches['generator_id'].unique():
        gen_tool_patches = tool_patches[tool_patches['generator_id'] == generator]
        
        # Find dev-identical and non-dev-identical counts
        gen_dev_identical = 0
        gen_non_dev_identical = 0
        for _, patch in gen_tool_patches.iterrows():
            match = developer_patches[
                (developer_patches['bug_uid'] == patch['bug_uid']) &
                (developer_patches['content'] == patch['content'])
            ]
            if not match.empty:
                gen_dev_identical += 1
            else:
                gen_non_dev_identical += 1
        
        for correctness in ['Correct', 'Overfitting']:
            subset = gen_tool_patches[gen_tool_patches['correctness'] == correctness]
            if len(subset) > 0:
                # Count dev-identical in this subset
                subset_dev_identical = 0
                subset_non_dev_identical = 0
                for _, patch in subset.iterrows():
                    match = developer_patches[
                        (developer_patches['bug_uid'] == patch['bug_uid']) &
                        (developer_patches['content'] == patch['content'])
                    ]
                    if not match.empty:
                        subset_dev_identical += 1
                    else:
                        subset_non_dev_identical += 1
                
                row = {
                    'Generator': generator,
                    'Total': len(subset),
                    'Dev-Identical': subset_dev_identical,
                    'Non-Dev-Identical': subset_non_dev_identical,
                    'is_single_hunk': len(subset[subset['is_single_hunk']]),
                    'are_single_hunks': len(subset[subset['are_single_hunks']]),
                    'Correctness': correctness
                }
                combined_summary.append(row)
    
    if combined_summary:
        summary_df = pd.DataFrame(combined_summary)
        logging.info("\n")
        logging.info(summary_df.to_string(index=False))

    # Drop temporary columns (not from originals since we made copies)
    developer_patches.drop(['content', 'is_single_hunk', 'are_single_hunks'], axis=1, inplace=True, errors='ignore')
    tool_patches.drop(['content', 'is_single_hunk', 'are_single_hunks'], axis=1, inplace=True, errors='ignore')
    if len(non_dev_identical_df) > 0:
        non_dev_identical_df.drop(['content', 'is_single_hunk', 'are_single_hunks'], axis=1, inplace=True, errors='ignore')

    logging.info("Reporting dataset completed.")

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

def experiment_7(developer_patches, tool_patches, processor):
    def get_response(patch):
        tool_content = processor["function"](patch) 

        # Select the developer patch with same bug_uid
        developer_patch = developer_patches.loc[developer_patches["bug_uid"] == patch["bug_uid"]].iloc[0]
        developer_content = processor["function"](developer_patch) 

        are_clones = sourcerercc_are_clones(developer_content, tool_content)

        # Continue before this
        label = {
            "patch_uid": patch.name,
            "processor": processor["uid"],
            "clones": are_clones
        }

        return pd.Series(label)

    correct_patches = tool_patches[tool_patches["correctness"] == "Correct"]
    correct_patches = correct_patches[correct_patches.apply(lambda patch: are_single_hunks(patch, developer_patches), axis=1)]
    no_correct_patches = len(correct_patches)
    no_models = len(models)
    no_prompts = len(prompts)
    no_temperatures = len(temperatures)
    logging.info(f"Running experiment 1 ... no_models: {no_models}, no_prompts: {no_prompts}, no_correct_patches: {no_correct_patches}, temperature: {no_temperatures}")
    result_file = os.path.join(TMP_RESULTS_DIR, "EXP7-sourcerercc.pkl")

    if not os.path.exists(result_file):        
        logging.info(f"Processing the patches ... EXP7-sourcerercc")
        tqdm.pandas(desc=f"Processing the patches ... EXP7-sourcerercc")
        results = correct_patches.progress_apply(get_response, axis=1)
        tqdm.pandas(desc="Unknown Process.")
        results.to_pickle(result_file)

    logging.info(f"Results already exist. EXP7-sourcerercc")
    
def experiment_8(developer_patches, tool_patches, processor): #SourcererCC
    def get_response(groundtruth_patch, tool_patch):
        tool_patch_content = processor["function"](tool_patch) 
        groundtruth_patch_content = processor["function"](groundtruth_patch) 

        are_clones = sourcerercc_are_clones(groundtruth_patch_content, tool_patch_content)

        label = {
            "tool_patch_uid": tool_patch.name,
            "groundtruth_patch_uid": groundtruth_patch.name,
            "processor": processor["uid"],
            "clones": are_clones
        }

        return pd.Series(label)
    
    def compare_groundtruth(tool_patch, groundtruth):
        groundtruth_selected_bug = groundtruth.loc[groundtruth["bug_uid"] == tool_patch["bug_uid"]]

        logging.info(f"tool_patch: {tool_patch.name}, no_selected_bug_groundtruth_patches: {len(groundtruth_selected_bug)}")

        results = groundtruth_selected_bug.apply(
            lambda row: get_response(row, tool_patch), 
            axis=1
        )

        return results

    # Exclude Selected Tool
    selected_tool = "tbar"
    selected_tool_patches = tool_patches[tool_patches["generator_id"].str.lower().str.contains(selected_tool)]
    tool_patches = tool_patches[~tool_patches["generator_id"].str.lower().str.contains(selected_tool)]

    # Keep single hunks
    tool_patches = tool_patches[tool_patches.apply(is_single_hunk, axis=1)]
    developer_patches = developer_patches[developer_patches.apply(is_single_hunk, axis=1)]

    logging.info(f"------------------------------------")
    logging.info(f"Single Hunk Tool Generated Patches: {len(tool_patches)}")
    logging.info(f"Single Hunk Developer Patches: {len(developer_patches)}")
    logging.info(f"Selected Patches: {len(selected_tool_patches)}")
    logging.info(f"------------------------------------")

    # Create extended groundtruth
    groundtruth_patches = pd.concat([tool_patches, developer_patches], axis=0)

    no_groundtruth_patches = len(groundtruth_patches)
    no_selected_tool_patches = len(selected_tool_patches)
    
    logging.info(f"Running experiment 8 ... selected_tool: {selected_tool}, no_correct_patches: {no_groundtruth_patches}, no_selected_tool_patches: {no_selected_tool_patches}")

    comparison_indices = selected_tool_patches.apply(
        lambda tool_patch: pd.Series(
            groundtruth_patches[groundtruth_patches["bug_uid"] == tool_patch["bug_uid"]].index,
            name=tool_patch.name
        ),
        axis=1
    ).stack().reset_index(level=1, drop=True).reset_index(name='groundtruth_index')

    comparison_indices.to_pickle(os.path.join(TMP_EXPERT_LABEL_DIR, f"EXP8-unlabeled-{selected_tool}.pkl"))

    result_file = os.path.join(TMP_RESULTS_DIR, f"EXP8-{selected_tool}-{processor['uid']}.pkl")

    if os.path.exists(result_file):
        logging.info(f"Results already exist. SelectedTool: {selected_tool} PatchProcessor: {processor['uid']} \n Skipping.")
        return

    for i, (_, tool_patch) in enumerate(tqdm(selected_tool_patches.iterrows(), 
                                           total=len(selected_tool_patches), 
                                           desc=f"Processing the patch ... selected_tool: {selected_tool}, PatchProcessor: {processor['uid']}")):
        
        intermediate_result_file = os.path.join(TMP_RESULTS_DIR, f"EXP8-{selected_tool}-{processor['uid']}-{i}.pkl")

        if os.path.exists(intermediate_result_file):
            logging.info(f"Results already exist. SelectedTool: {selected_tool} PatchProcessor: {processor['uid']} index: {i} \n Skipping to the next one.")
            continue

        logging.info(f"Processing the patch ... selected_tool: {selected_tool}, PatchProcessor: {processor['uid']}, index: {i}")

        results = compare_groundtruth(tool_patch, groundtruth_patches)
        results.to_pickle(intermediate_result_file)

def get_pairwise_tool_bug_based(patches): # Aligns with exp1 sahand-exp1-2 CloneHelper
    logging.info("Generating pairwise bug-based DataFrame ...")
    correct_patches = patches[patches["correctness"] == "Correct"].copy()
    unique_bug_uids = correct_patches['bug_uid'].nunique()
    logging.info("Unique bug UIDs in the cleaned tool patches with correctness 'Correct':")
    logging.info(unique_bug_uids)
    pairwise_rows = []
    for bug_uid, group in correct_patches.groupby("bug_uid"):
        uids = group.index.tolist()  
        for uid1, uid2 in combinations(uids, 2):
            pairwise_rows.append({
                "uid": uid1,
                "groundtruth_index": uid2,
                "expert_label": "Not-Clone"
            })

    pairwise_df = pd.DataFrame(pairwise_rows)

    logging.info("Pairwise DataFrame:")
    pairwise_df.to_pickle(os.path.join(TMP_PLOTS_DIR, "pairwise-bug-correct-deduplicated.pkl"))

    logging.info(pairwise_df)

    return pairwise_df

if __name__=="__main__": 
    logging.info("Running build.py ...")

    # Initial Data
    bugs, developer_patches, tool_patches = init(configure=False)

    # Patch Cleaning
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

    # Normalaize Names
    cleaned_developer_patches, cleaned_tool_patches = normalaize_names(cleaned_developer_patches, cleaned_tool_patches)

    report_dataset(cleaned_developer_patches, cleaned_tool_patches, bugs)

    # Deduplicating Same bug, same tool, same content
    cleaned_tool_patches = deduplicate_patches(cleaned_tool_patches)
    pairs = get_pairwise_tool_bug_based(cleaned_tool_patches)

    report_dataset(cleaned_developer_patches, cleaned_tool_patches, bugs)

    # Deduplicating Second
    # cleaned_developer_patches, cleaned_tool_patches = second_deduplicate_patches(cleaned_developer_patches, cleaned_tool_patches)

    report_dataset(cleaned_developer_patches, cleaned_tool_patches, bugs)

    # experiment_1(cleaned_developer_patches, cleaned_tool_patches, models, prompts, temperatures, patch_processors)

    experiment_2(cleaned_developer_patches, cleaned_tool_patches, models, prompts, temperatures, patch_processors)

    # experiment_5(cleaned_developer_patches, cleaned_tool_patches, models, temperatures, patch_processors)

    # experiment_7(cleaned_developer_patches, cleaned_tool_patches, [procesessor for procesessor in patch_processors if procesessor["uid"] == "method"][0])

    # experiment_8(cleaned_developer_patches, cleaned_tool_patches, [procesessor for procesessor in patch_processors if procesessor["uid"] == "method"][0])




    
