from setup import get_data, get_params
from utils.config import *
from utils.benchmark import *
from utils.utils import *
from utils.tool import *
from utils.dataset import *
import os
import logging
import re
import json
import ollama
import pandas as pd
import numpy as np

FOLDER_NAME = "rq0"
DATA_DIR = os.path.join(FOLDER_NAME, "data")
INDICIES_FOR_EXPERT_LABELS_FILE = os.path.join(FOLDER_NAME, f"indices-for-expert-labels.pkl")


# Compare patches of the selected tool Our EXP3 working with command parameters.
def experiment_selected_tool_vs_other(developer_patches, tool_patches, models, prompts, temperatures, patch_processors, selected_tool):
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
    selected_tool_patches = tool_patches[tool_patches["generator_id"] == selected_tool]
    tool_patches = tool_patches[tool_patches["generator_id"] != selected_tool]

    # Create extended groundtruth
    groundtruth_patches = pd.concat([tool_patches, developer_patches], axis=0)

    comparison_indices = selected_tool_patches.apply(
        lambda tool_patch: pd.Series(
            groundtruth_patches[groundtruth_patches["bug_uid"] == tool_patch["bug_uid"]].index,
            name=tool_patch.name
        ),
        axis=1
    ).stack().reset_index(level=1, drop=True).reset_index(name='groundtruth_index')

    comparison_indices.to_pickle(INDICIES_FOR_EXPERT_LABELS_FILE)

    print(
        f"""
        Selected tool: {selected_tool}
        Number of groundtruth patches: {len(groundtruth_patches)}
        Number of selected tool patches: {len(selected_tool_patches)}
        Number of models: {len(models)}
        Number of prompts: {len(prompts)}
        Number of temperatures: {len(temperatures)}   
        Total number of comparisons: {len(groundtruth_patches) * len(selected_tool_patches) * len(models) * len(prompts) * len(temperatures)}
        """
    )    

    for processor in patch_processors:
        # Filter prompts based on processor type
        if processor["uid"] == "defaultpatch":
            filtered_prompts = [p for p in prompts if p["input"] == "patches"]
        elif processor["uid"] == "method":
            filtered_prompts = [p for p in prompts if p["input"] == "code"]
        else:
            filtered_prompts = prompts

        for model in models:
            for temperature in temperatures:
                for prompt in filtered_prompts:
                    prompt_uid = prompt["uid"]
                    temperature_value = temperature["uid"]
                    model_uid = model["uid"]
                    processor_uid = processor["uid"]
                    result_file = os.path.join(DATA_DIR, f"RQ0-{selected_tool}-{processor_uid}-{model_uid}-{temperature_value}-{prompt_uid}.pkl")

                    if os.path.exists(result_file):
                        logging.info(f"Results already exist. SelectedTool: {selected_tool} PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid} \n Skipping to the next one.")
                        
                        continue

                    all_results = []
                    
                    for i, (_, tool_patch) in enumerate(tqdm(selected_tool_patches.iterrows(), total=len(selected_tool_patches), desc=f"Processing the patch ... selected_tool: {selected_tool}, PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid}")):
                        individual_file = os.path.join(DATA_DIR, f"RQ0-incomplete-{selected_tool}-{processor_uid}-{model_uid}-{temperature_value}-{prompt_uid}-{i}.pkl")

                        if os.path.exists(individual_file):
                            logging.info(f"Loading existing results for index: {i}")
                            results = pd.read_pickle(individual_file)
                        else:
                            logging.info(f"Processing the patch ... selected_tool: {selected_tool}, PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid}, index: {i}")
                            results = compare_groundtruth(tool_patch, groundtruth_patches, prompt, temperature, model, processor)
                            results.to_pickle(individual_file)
                        
                        all_results.append(results)
                    
                    # Merge all results into one DataFrame
                    merged_results = pd.concat(all_results, ignore_index=True)
                    merged_results.to_pickle(result_file)
                    logging.info(f"Merged results saved to {result_file}")

            ollama.generate(model=model["uid"], keep_alive=0)

    
if __name__ == "__main__":
    bugs, developer_patches, tool_patches = get_data()
    prompts, models, temperatures, patch_processors = get_params()
    tool = "tbar"

    print(f"Number of Prompts: {len(prompts)}, Models: {models}, Temperatures: {temperatures}, Patch Processors: {patch_processors}")
    experiment_selected_tool_vs_other(developer_patches, tool_patches, models, prompts, temperatures, patch_processors, tool)

