
import os
import logging
import pandas as pd
from utils.config import *
from utils.benchmark import *
from utils.utils import *
from utils.tool import *
from utils.dataset import *
from build import init, clean_patches, get_methods, get_patch_processors, get_tool_settings, normalaize_names, deduplicate_patches, report_dataset, get_pairs


class Experiment5Results:
    def __init__(self, selected_tools, input_processor=None, input_model=None, input_prompt=None):
        # Initial Data
        bugs, developer_patches, tool_patches = init(configure=False)

        # Does not Read from RQ5 Directory but generate itself
        bugs_with_uid = bugs.reset_index()  # This makes 'uid' a regular column
        bugs_dict = bugs_with_uid.to_dict('records')
        tool_patches = pd.DataFrame(get_llm4pc_dataset(bugs_dict)).set_index("uid")

        # Patch Processings
        patch_processors = get_patch_processors()

        # Tool Settings
        prompts, models, temperatures = get_tool_settings()

        self.bugs = bugs
        self.patch_processor = get_object_by_uid(patch_processors, input_processor)
        self.model = get_object_by_uid(models, input_model)
        self.prompt = get_object_by_uid(prompts, input_prompt)
        self.temperature = temperatures[0]
        self.input_developer_patches = developer_patches
        self.input_tool_patches = tool_patches
        self.all_tool_patches = tool_patches
        self.selected_tools = selected_tools

        self._merge_results()

        self.results = self._get_results()


    def _merge_results(self):
        logging.info("Merging Results ...")

        for tool in self.selected_tools:
            final_result_file = os.path.join(TMP_RESULTS_DIR, f"EXP3-{tool}-{self.patch_processor['uid']}-{self.model['uid']}-{self.temperature['uid']}-{self.prompt['uid']}.pkl")

            if os.path.exists(final_result_file):
                logging.info(f"Skipping merging for {final_result_file} as it already exists.")

                continue 

            no_selected_tool_patches = len(self.input_tool_patches[self.input_tool_patches["generator"] == tool])

            if no_selected_tool_patches == 0:
                raise Exception(f"No tool patches found for {tool}")

            for i in range(no_selected_tool_patches):
                result_file = os.path.join(TMP_RESULTS_DIR, f"EXP3-{tool}-{self.patch_processor['uid']}-{self.model['uid']}-{self.temperature['uid']}-{self.prompt['uid']}-{i}.pkl")
                df = pd.read_pickle(result_file)
                
                if i == 0:
                    combined_df = df
                
                else:
                    combined_df = pd.concat([combined_df, df])

            combined_df.to_pickle(final_result_file)

    def _get_results(self):
        results = []

        for tool in self.selected_tools:
            logging.info(f"Getting Results for {self.patch_processor['uid']}, {self.model['uid']}, {self.temperature['uid']}, {self.prompt['uid']}")

            file_name = f"EXP3-{tool}-{self.patch_processor['uid']}-{self.model['uid']}-{self.temperature['uid']}-{self.prompt['uid']}.pkl"
            result_file = os.path.join(TMP_RESULTS_DIR, file_name)

            result = {
                "tool": tool,
                "processor": self.patch_processor,
                "model": self.model,
                "temperature": self.temperature,
                "prompt": self.prompt,
                "file_name": file_name,                           
                "result_file": result_file,
            }

            results.append(result)

        logging.info(f"Results: {results}")

        return results
    
def get_llm4pc_uid(filename):
    pathc_name, project_name, number, generator = filename.strip().replace("-plausible", "").replace("-plusible", "").replace("Nopol2015", "Nopol").replace("Nopol2017", "Nopol").replace(".patch", "").split("-")
    patch_uid = f"llm4pc-defects4j-{project_name}-{number}-{generator}-{pathc_name}"

    return patch_uid

def get_llm4pc_available_patches():
    data = pd.read_csv(os.path.join(RQ5_DIR, "LLM4PatchCorrectness", "all_data_v1.csv"))

    data["uid"] = data["filename"].apply(get_llm4pc_uid)

    data = data[data["tool"] != "defects4j-developer"].copy().reset_index(drop=False).drop(columns=["index"])
    
    return data

if __name__ == "__main__":
    # get llm4pc available patches 825
    llm4pc_data = get_llm4pc_available_patches()

    logging.info("Experiment 5 Historian Cache Results Module")

    logging.info("Running Experiment #5 ...")
    tools = [
            'Arja', 'Jaid', 'TBar', 'FixMiner', 'jKali', 'Nopol', 'HDRepair', 'ACS',
        'jGenProg', 'SketchFix', 'SimFix', 'AVATAR', 'GenProg', 'kPAR', 'Cardumen',
        'SequenceR', 'Kali', 'DynaMoth', 'SOFix', 'CapGen', 'jMutRepair', 'RSRepair'
    ]

    input_processor="defaultpatch"
    input_model="qwen2.5:7b"
    input_prompt="llm4cc-clone_type-patch"
    
    results = Experiment5Results(selected_tools=tools, input_processor=input_processor, input_model=input_model, input_prompt=input_prompt)

    # see our patches 813 results.all_tool_patches

    # see what exists in llm4pc_data["uid"] that does not exist in results.all_tool_patches.index
    missing_patches = llm4pc_data[~llm4pc_data["uid"].isin(results.all_tool_patches.index)]
    print("Missing Patches:")
    print(missing_patches)
    
    # drop duplicates keep first 757 because of the Nopol2015 and Nopol2017 issue
    llm4pc_data = llm4pc_data.drop_duplicates(subset=["uid"], keep="first").reset_index(drop=True)
    print("LLM4PC Available Patches:")
    print(llm4pc_data)
