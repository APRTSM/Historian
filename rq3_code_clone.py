import pandas as pd
import logging
import pandas as pd
from utils.config import *
from utils.benchmark import *
from utils.utils import *
from utils.tool import *
from utils.dataset import *
from build import init, clean_patches, get_methods, get_patch_processors, get_tool_settings, normalaize_names, deduplicate_patches


class RQ:
    def __init__(self, selected_tool):
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

class RQ3(RQ):
    def __init__(self, selected_tool, input_processors, input_models, input_prompts):
        super().__init__(selected_tool)


if __name__ == "__main__":
    input_models = [
        "magicoder:7b-s-cl",
        "codellama:7b-instruct",
        "codellama:13b-instruct",
        "deepseek-coder:6.7b",
        "codegemma:7b-instruct",
        "qwen2.5:7b",
        "qwen2.5-coder:7b",
        "yi-coder:9b",
        "hermes3:8b"
    ]

    input_prompts = [
        "llm4cc-clone_type",
        "llm4cc-integrated",
        "llm4cc-simple_prompt-semantical",
        "llm4cc-reasoning-patch-semantical",
        "llm4cc-similarity_line-patch-semantical",
        "llm4cc-simple_prompt-identical",
        "llm4cc-reasoning-patch-identical",
        "llm4cc-similarity_line-patch-identical"
    ]

    pairs = pd.read_pickle(os.path.join(TMP_RESULTS_DIR, "EXP2-tbar-method-hermes3:8b-0.1-llm4cc-similarity_line-patch-identical.pkl"))
    print(pairs)
    pairs = pd.read_pickle(os.path.join(TMP_RESULTS_DIR, "expert", "EXP2-labeled-tbar.pkl"))
    print(pairs)
    rq3 = RQ3(selected_tool="tbar", input_processors=None, input_models=input_models, input_prompts=input_prompts)


