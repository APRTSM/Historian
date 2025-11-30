
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
from build import init, clean_patches, get_methods, get_patch_processors, get_tool_settings, normalaize_names, deduplicate_patches, report_dataset, get_pairs
from transformers import pipeline
import itertools
from sklearn.metrics import cohen_kappa_score, accuracy_score, f1_score
from collections import Counter, defaultdict
import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns
from rq4_llms import get_methods_and_save, normalize_names_and_save, get_single_methods_and_save, get_files_and_save

from rq5_historian_cache import get_cache_labels


if __name__ == "__main__":
    # TMP_LLM4PC_FILES_PKL
    print(get_cache_labels())
