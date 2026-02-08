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

    # # Exclude Selected Tool
    # selected_tool = "tbar"

    # Remove 2017, 2015, ...
    selected_tool_patches = tool_patches[tool_patches["generator"] == selected_tool]
    tool_patches = tool_patches[tool_patches["generator"] != selected_tool]

    # # Exclude overfitting patches
    # tool_patches = tool_patches[tool_patches["correctness"] == "Correct"]

    # # Keep single hunks
    # tool_patches = tool_patches[tool_patches.apply(is_single_hunk, axis=1)]
    # developer_patches = developer_patches[developer_patches.apply(is_single_hunk, axis=1)]

    logging.info(f"------------------------------------")
    logging.info(f"Single Hunk Tool Generated Patches: {len(tool_patches)}")
    logging.info(f"Single Hunk Developer Patches: {len(developer_patches)}")
    logging.info(f"Selected Patches: {len(selected_tool_patches)}")
    logging.info(f"------------------------------------")

    # Create extended groundtruth
    groundtruth_patches = pd.concat([tool_patches, developer_patches], axis=0)

    no_groundtruth_patches = len(groundtruth_patches)
    no_selected_tool_patches = len(selected_tool_patches)
    no_models = len(models)
    no_prompts = len(prompts)
    no_temperatures = len(temperatures)
    logging.info(f"Running experiment 3 ... selected_tool: {selected_tool}, no_models: {no_models}, no_prompts: {no_prompts}, no_correct_patches: {no_groundtruth_patches}, no_selected_tool_patches: {no_selected_tool_patches}, temperature: {no_temperatures}")

    comparison_indices = selected_tool_patches.apply(
        lambda tool_patch: pd.Series(
            groundtruth_patches[groundtruth_patches["bug_uid"] == tool_patch["bug_uid"]].index,
            name=tool_patch.name
        ),
        axis=1
    ).stack().reset_index(level=1, drop=True).reset_index(name='groundtruth_index')

    comparison_indices.to_pickle(os.path.join(TMP_EXPERT_LABEL_DIR, f"EXP3-unlabeled-{selected_tool}.pkl"))

    for processor in patch_processors:
        for model in models:
            for temperature in temperatures:
                for prompt in prompts:
                    prompt_uid = prompt["uid"]
                    temperature_value = temperature["uid"]
                    model_uid = model["uid"]
                    processor_uid = processor["uid"]
                    result_file = os.path.join(TMP_RESULTS_DIR, f"EXP3-{selected_tool}-{processor_uid}-{model_uid}-{temperature_value}-{prompt_uid}.pkl")

                    if os.path.exists(result_file):
                        logging.info(f"Results already exist. SelectedTool: {selected_tool} PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid} \n Skipping to the next one.")
                        
                        continue

                    for i, (_, tool_patch) in enumerate(tqdm(selected_tool_patches.iterrows(), total=len(selected_tool_patches), desc=f"Processing the patch ... selected_tool: {selected_tool}, PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid}")):
                        result_file = os.path.join(TMP_RESULTS_DIR, f"EXP3-{selected_tool}-{processor_uid}-{model_uid}-{temperature_value}-{prompt_uid}-{i}.pkl")

                        if os.path.exists(result_file):
                            logging.info(f"Results already exist. SelectedTool: {selected_tool} PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid} index: {i} \n Skipping to the next one.")
                            
                            continue

                        logging.info(f"Processing the patch ... selected_tool: {selected_tool}, PatchProcessor: {processor_uid} model: {model_uid} temperature: {temperature_value} prompt: {prompt_uid}, index: {i}")

                        results = compare_groundtruth(tool_patch, groundtruth_patches, prompt, temperature, model, processor)
                        results.to_pickle(result_file)

            ollama.generate(model=model["uid"], keep_alive=0)


    


if __name__ == "__main__":
    bugs, developer_patches, tool_patches = get_data()
    prompts, models, temperatures, patch_processors = get_params()
    tool = "tbar"

    report_selected_tool(tool_patches, tool)
    print(f"TBar Raw patches: {len(tool_patches[tool_patches['generator'] == tool])}")
    print(f"TBar final patches: {len(tool_patches[tool_patches['generator_id'] == tool])}")
    print(f"TBar correct patches: {len(tool_patches[(tool_patches['generator_id'] == tool) & (tool_patches['correctness'] == 'Correct')])}")
    print(f"Tbar Overfitting patches: {len(tool_patches[(tool_patches['generator_id'] == tool) & (tool_patches['correctness'] == 'Overfitting')])}")

    print(
        f"""
        ============================
        Tools: {tool_patches["generator_id"].unique()},
        ============================
        # Tools: {len(tool_patches["generator_id"].unique())},
        """    
    )

    # experiment_selected_tool_vs_other(developer_patches, tool_patches, models, prompts, temperatures, patch_processors, tool)

