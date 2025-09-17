import pandas as pd
import logging
import pandas as pd
from utils.config import *
from utils.benchmark import *
from utils.utils import *
from utils.tool import *
from utils.dataset import *
from build import init, clean_patches, get_methods, get_patch_processors, get_tool_settings, normalaize_names, deduplicate_patches, report_dataset, get_pairs


class Result:
    def __init__(self, selected_tool, input_processors=None, input_models=None, input_prompts=None):
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

        # Normalaize Names
        cleaned_developer_patches, cleaned_tool_patches = normalaize_names(cleaned_developer_patches, cleaned_tool_patches)

        # Deduplicating
        cleaned_tool_patches = deduplicate_patches(cleaned_tool_patches)

        self.bugs = bugs
        self.patch_processors = patch_processors
        self.models = models
        self.prompts = prompts
        self.temperatures = temperatures
        self.input_developer_patches = cleaned_developer_patches
        self.input_tool_patches = cleaned_tool_patches
        self.all_tool_patches = tool_patches
        self.selected_tool = selected_tool
        self.selected_tool_patches = self.input_tool_patches[self.input_tool_patches["generator_id"].str.lower().str.contains(self.selected_tool)]
        self.no_selected_tool_patches = len(self.selected_tool_patches)
        self.all_groundtruth_patches_uid_deduplicated = pd.concat((self.input_developer_patches, self.all_tool_patches), axis=0)
        self.all_groundtruth_patches_uid_deduplicated = self.all_groundtruth_patches_uid_deduplicated[~self.all_groundtruth_patches_uid_deduplicated.index.duplicated(keep='first')] # New Addition Eyl 17

        logging.info(f"Selected Tool: {selected_tool}, # Developer Patches: {len(developer_patches)}, # Tool Patches: {len(tool_patches)}, Patch Processors: {self.patch_processors}, Models: {self.models}, Prompts: {self.prompts}, Temperatures: {self.temperatures}")



if __name__ == "__main__":
    pairs = pd.read_pickle("/home/sahand/Desktop/hist-update/Historian/tmp/results/EXP2-tbar-method-hermes3:8b-0.1-llm4cc-similarity_line-patch-identical.pkl")
    results = Result()


