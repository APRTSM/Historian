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

""" RQ5
RQ4 is Time series
This is the main file to preprocess and read the data for RQ4 LLMs.
New patches are gathered from Historian dataset.
Preprocessing functions are rewritten.

rq4_gather.py 
Gathers files from different projects in rq4/tool_patches and stores them in rq4/first_cleaned_data and assigns them ids.
We fix them manually and move them to rq4/second_cleaned_data
RQ4_FIRST_CLEANED_DATA_DIR is gathered data dir

rq4_apply.py
Searches through patches in RQ4_SECOND_CLEANED_DATA_DIR (rq4/second_cleaned_tool_patches )
Try to apply from rq4/second_cleaned_tool_patches
if applied save to TMP_FORMATTED_PATCH_DIR

Since I didnt know what did I do to rq4/second_cleaned_tool_patches I took a copy of it deleted inside and took the patches from three done folders of prsto.
    copied again the files that does not exist in rq4/second_cleaned_tool_patches. now I first copy the files of the tool I want to rq4/second_cleaned_tool_patches add not fixed (are not in done) command should check if the patch does not already exist in rq4/second_cleaned_tool_patches.
    cd /home/sahand/Desktop/Historian/rq4/first_cleaned_tool_patches && for f in *; do [ ! -e "/home/sahand/Desktop/Historian/rq4/second_cleaned_tool_patches/$f" ] && mv "$f" /home/sahand/Desktop/Historian/rq4/second_cleaned_tool_patches/; done
    copy_patches_not_fixed does not work because when I gave it to prst It didnt include clousre 63 and 93 patches, which means the index has changed. add this patches manually and index them as last one.
    
    so check the number of tool patches in the sheet and the total shown when we run rq4_apply.py. it should match. 
    light green in sheet means prsto got them ow its time to apply and check the numbers and make it dark green.
    should I cover closure 63 as well? so in replication package and in our dataset columns in the sheet match. Next tool I wil doo now I'm doing transplantfix.
    including closure will require running first_cleaned_tool_patches again to gather closure patches as well.

rq4_assign_correctness.py assignes correctness after applying patches in rq4_apply.py reads from the list in TMP

    
rq4_dataset_analyzer.py and rq4_cc.py look like the same. They are trying to get rq1 (sourcerercc and etc...) labels. BOTH DELETED

rq4_tmp_broken_patches.py gets the id from rq4/failed_patches.txt and cp them to rq4/failed_patches/
    it is related to fixing 3000 broken patches from our initial dataset not new datset.


rq5 focuses on intersection of tool labels.

rq5_historian_cache.py gets historian unknonw labels labeled by cache on 813 patches. (replace_other_apca_labels in _get_f1_type_binary in Experiment3Evaluator) (STARED)

rq5_dataset_analysis.py finds that we have 813 patches while the llm4pc has 825 tool generated patches from 1182 small-cache.
out of 1182 3.. are developer patches when excluded we are left with 825 tool patches.
Out of 825 we have 813 patches in our cleaned dataset. Missing 12 patches belong to closure 93 and 63.

rq5_dataset_fix.py after rq5_dataset_analysis.py we try to fix non applicable patches out of 813 patches. For ODS we need to get mehtods.

rq5_get_ODS.py prepares the ODS labels to use in rq5_historian_cache.py.
rq5_historian_ods.py gets histrian unknown labels labeled by ODS on 813 patches. Copy of rq5_historian_cache.py

rq5_historian_quatrain_bugreports.py uses "rq5", "Bug_Report_All.json" adds report title + description to the patches and saves it in "rq5", "rq5_quatrain_predictions.csv" to send to server to extract quatrain labels.
    evaluate_csv.py in QUatrain uses "rq5_quatrain_predictions.csv" on server uses bug_report field and content field to get quatrain labels.
rq5_historian_quatrain_bugreports.py same as rq5_historian_cache.py but uses quatrain labels instead of cache labels.
rq5_historian_quatrain_tocsv.py changes labels from results of quatrain executed on server to csv file.

rq5/README.md has the notes about files.

rq4_figure_1.py Oca 5 Draw Figure 1 for RQ4

----
State 20 Kas: _ added historian patches to prsto. Also cleaned 3000 non applicable patches from the old dataset not added yet.

---
The problem with Experiment3Evaluator is that EXP3 results have ids like llm4pc-defects4j-Math-50-SketchFix-patch1
    But I deleted the file to get llm4pc patches which was finding the patches by matching the content of the patch file.
Also, where are the results of Gemini. Find them in previous branches. On a mission to find these paatches cuz I need groundtruth correctness labels.
Added llm4pc to dataset, few to utils config and dataset,py, the function is not trying to find patches by their content but rather with their uid.
    It is actually caches small datase (csmall) which consists of the following datsets, wangicse and dl4pc.
    The get_llm4pc_dataset function in dataset.py gets patches from csmall or defectsrepairing except developer pathces.
    - Does this function get all patches in the paper?
    - get these patches ready and run rq5 without error. then get cache labels. Put them instead of Unknowns.
---
Errors
aprenfl-defects4j-Math-95-ARJA-Patch_179_153.patch Math 95 patches all are missing an if at line 145 at the file named FDistributionImpl.java
    Sometime you need to do cd .. and go back to checkout dir again to apply the patch.
"""

""" RQ1
Motivation for incorrect patches. 
I look for the time that results/rq1 first appeared or rq1.py appeared.

git log --reverse --oneline ff4086937..HEAD -- tmp/plots/cluster_size_frequency_combined.png
211a7f693 fix ...
tmp-rq1 branch (Where this branch starts aligns with motivation for Correct patches)
211a7f693 fix
05cd0ab66 fix
4a4e8ed22 fix
Let's do it on cache small dataset. except the developer remains 825 patches. (12 belong to CLosure 63 and 93)
Number of initial tool patches: 813 ...
Number of initial tool patches: 813
Number of initial tool patches: 782 (What we select) (single method patches)
Number of initial tool patches: 692 (After deduplication same bug, same generator, same content)
rq1_cache_small.py does it for correct and incorrect patches. 782.
operates in tmp/results/rq1 reads tmp/results/rq1/rq1-expert.pkl writes remaining
result is tmp/plots/cluster_size_frequency_combined.png
But I have to add labels for correct and overfitting. No pairs found with label  Error
then run rq1_plots.py reads from /home/sahand/Desktop/Historian/tmp/data/rq1

closure_correction.py add closure to defects4j dataset.
Ara 23 I added support for depricated bugs from defects4j15, check the configs. So now it is 825 patches also added two known missing patches to cache small.
    check dataset.py get_llm4pc_dataset, benchmark.py get_bug_list_defects4j and checkout_bug_defects4j and checkout_fix_defects4j
    tmp/results/rq1 includes extesnsive archvive of rq1 results.
"""

""" Missing Experiments
Evaluating the quality of APR tools in experimental settings presents a significant challenge. 
A single bug often admits multiple semantically equivalent correct solutions, and APR tools typically generate numerous plausible patches that must be assessed. 
Historian addresses this challenge through an evidence-based approach. 
By matching candidate patches against a historically validated reference set, it can identify redundant patches and provide traceable verdicts backed by concrete precedent. 
When a patch matches a known correct or overfitting solution, researchers can verify the verdict 
through simple inspection of the matched reference rather than extensive manual analysis---saving substantial time in large-scale evaluations.

 When a patch matches a known correct or overfitting solution, researchers can verify the verdict through simple inspection of the matched reference 
 rather than extensive manual analysis---
 saving substantial time in large-scale evaluations.
"""

"""
RQ3: Zero-Shot Performance of LLMs on Code Clone Detection
rq3_zero_shot_plot_boxplot.py plots violin and box plot, calculates zero shot for those failed to regex detect. again 128 combination or sth

"""


"""
    # Prompt Abbreviation Mapping:
    # Prompt UIDAbbreviation
    # llm4cc-clone_type-patch CC SCC
    # llm4cc-integrated-patch CC I
    # llm4cc-simple_prompt-patch-semantical SS S
    # llm4cc-reasoning-patch-semantical SS R
    # llm4cc-similarity_line-patch-semantical SS LS
    # llm4cc-simple_prompt-patch-identical SI S
    # llm4cc-reasoning-patch-identical SI R
    # llm4cc-similarity_line-patch-identical SI LS

    # Model Abbreviation Mapping:
    # Model UID Abbreviation
    # magicoder:7b-s-cl MC7B
    # codellama:7b-instruct CL7B
    # deepseek-coder:6.7b DSC6.7B
    # codegemma:7b-instruct CG7B
    # qwen2.5:7b QW7B
    # qwen2.5-coder:7b QWC7B
    # yi-coder:9b YC9B
    # hermes3:8b H3-8B
    # gemini-2.0-flash Gemini
"""

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
    logging.info("Deduplicating developer patches ...")
    if os.path.exists(path):
        deduplicated_patches = pd.read_pickle(path)
        return deduplicated_patches

    deduplicated_patches = patches.copy()
    deduplicated_patches['content'] = deduplicated_patches.apply(get_single_hunk_method, axis=1)
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

    # Save to HTML files
    # duplicates.to_html('removed_duplicates.html', index=False, escape=False)
    # deduplicated_patches.to_html('deduplicated_patches.html', index=False, escape=False)

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
Get New Patches (Historian)
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

    """" New Patches Preprocessing """

    # print("Raw New Patches:")
    # print(len(new_patches))

    # cleaned_new_patches = clean_and_save_patches(bugs, new_patches, TMP_CLEANED_NEW_PATHCES_PKL)

    # print("Cleaned New Patches:")
    # print(len(cleaned_new_patches))

    # cleaned_new_patches = get_methods_and_save(bugs, cleaned_new_patches, TMP_METHOD_NEW_PATHCES_PKL)

    # print("Method New Patches:")
    # print(len(cleaned_new_patches))

    # cleaned_new_patches = normalize_names_and_save(cleaned_new_patches, TMP_GENERATOR_NORMALIZED_NEW_PATHCES_PKL)

    # print("Generator Normalized New Patches:")
    # print(len(cleaned_new_patches))

    # cleaned_new_patches = get_single_methods_and_save(cleaned_new_patches, TMP_SINGLE_HUNK_NEW_PATHCES_PKL)

    # print("Single Hunk New Patches:")
    # print(len(cleaned_new_patches))

    # cleaned_new_patches = deduplicate_patches_and_save(cleaned_new_patches, TMP_DEDUPLICATED_NEW_PATHCES_PKL)

    # print("Deduplicated New Patches:")
    # print(len(cleaned_new_patches))

    """ Groundtruth Preprocessing """

    print("Raw Developer Patches:")
    print(len(developer_patches))
    print("Raw Tool Patches:")
    print(len(tool_patches))

    # Patch Cleaning (Fix Patches)
    cleaned_developer_patches = clean_and_save_patches(bugs, developer_patches, TMP_CLEANED_DEVELOPER_PATHCES_PKL)
    cleaned_tool_patches = clean_and_save_patches(bugs, tool_patches, TMP_CLEANED_TOOL_PATHCES_PKL)

    print("Cleaned Developer Patches:")
    print(len(cleaned_developer_patches))
    print("Cleaned Tool Patches:")
    print(len(cleaned_tool_patches))

    # Fetch Methods (Get Changed Methods) Should be same but is not!
    cleaned_developer_patches = get_methods_and_save(bugs, cleaned_developer_patches, TMP_METHOD_DEVELOPER_PATHCES_PKL)
    cleaned_tool_patches = get_methods_and_save(bugs, cleaned_tool_patches, TMP_METHOD_TOOL_PATHCES_PKL)

    print("Method Developer Patches:")
    print(len(cleaned_developer_patches))
    print("Method Tool Patches:")
    print(len(cleaned_tool_patches))

    # Normalize Names, Last step for Developer Patches, Formatting generator_id
    cleaned_developer_patches = normalize_names_and_save(cleaned_developer_patches, TMP_GENERATOR_NORMALIZED_DEVELOPER_PATHCES_PKL)
    cleaned_tool_patches = normalize_names_and_save(cleaned_tool_patches, TMP_GENERATOR_NORMALIZED_TOOL_PATHCES_PKL)

    print("Generator Normalized Developer Patches:")
    print(len(cleaned_developer_patches))
    print("Generator Normalized Tool Patches:")
    print(len(cleaned_tool_patches))

    # Get Single Method Patches
    cleaned_developer_patches = get_single_methods_and_save(cleaned_developer_patches, TMP_SINGLE_HUNK_DEVELOPER_PATHCES_PKL)
    cleaned_tool_patches = get_single_methods_and_save(cleaned_tool_patches, TMP_SINGLE_HUNK_TOOL_PATHCES_PKL)

    print("Single Hunk Developer Patches:")
    print(len(cleaned_developer_patches))
    print("Single Hunk Tool Patches:")
    print(len(cleaned_tool_patches))

    # Deduplicating Same bug, same tool, same content (Now looks for Methods), Last step for Tool Patches
    cleaned_tool_patches = deduplicate_patches_and_save(cleaned_tool_patches, TMP_DEDUPLICATED_TOOL_PATHCES_PKL)

    print("Deduplicated Tool Patches:")
    print(len(cleaned_tool_patches))    

    # report_dataset(cleaned_developer_patches, cleaned_tool_patches, bugs)

    """ Main Task Execution """

    # experiment_4(cleaned_new_patches, cleaned_developer_patches, cleaned_tool_patches, models, prompts, temperatures, patch_processors)

    
