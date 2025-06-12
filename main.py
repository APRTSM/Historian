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



if __name__ == "__main__":
    tool_patches = pd.read_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)
    developer_patches = pd.read_pickle(TMP_GENERATOR_NORMALIZED_DEVELOPER_PATHCES_PKL)

    print(tool_patches)

    # Remove developer identical-1 patches
    cleaned_tool_patches = remove_developer_identical_patches(tool_patches, developer_patches)

    # pairs = get_pairwise_tool_bug_based(cleaned_tool_patches)

    # print(pairs)

    # correct_tool_patches = cleaned_tool_patches[cleaned_tool_patches["correctness"] == "Correct"]

    # correct_pairs = get_pairwise_tool_bug_based(correct_tool_patches)

    # print(correct_pairs)