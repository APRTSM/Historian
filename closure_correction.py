import re
from collections import defaultdict, deque
import pandas as pd
import os
import time
import logging
from tqdm import tqdm
import ollama
from utils.config import *
from utils.benchmark import *
from utils.utils import *
from utils.tool import *
from utils.dataset import *
from rq4_llms import init, get_patch_processors, get_tool_settings, apply_params, parse_args, clean_and_save_patches, get_methods_and_save, normalize_names_and_save, get_single_methods_and_save, deduplicate_patches_and_save


if __name__ == "__main__":
    # Get the tool patches and developer patches (Numbers match with previous versions if patch matches are considered)
    bugs, developer_patches, tool_patches = init(configure=True)

    closure_bugs = bugs[bugs['project'] == 'Closure'].index.tolist()
    print("defects4j-Closure-93" in closure_bugs)