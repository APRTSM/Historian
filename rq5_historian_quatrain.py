
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
from tqdm import tqdm

def get_defects4j_issues(patch: pd.Series, bug_reports: dict):
    new_patch = patch.copy()
    bug_uid = new_patch["bug_uid"]
    bug_uid = bug_uid.replace("defects4j-", "").lower()

    report_list = bug_reports.get(bug_uid, [])

    if not report_list:
        new_patch["bug_report"] = "none"

    else:
        new_patch["bug_report"] = "\n".join(report_list)

    return new_patch




if __name__ == "__main__":
    BUG_REPORT_PATH = os.path.join("rq5", "Bug_Report_All.json")

    with open(BUG_REPORT_PATH, "r") as f:
        bug_reports = json.load(f)

    patches = pd.read_pickle(TMP_LLM4PC_FILES_PKL)

    patches["content"] = patches["location"].apply(read_file)
    patches = patches.apply(get_defects4j_issues, args=(bug_reports, ), axis=1)

    ODS_RESULTS_CSV = os.path.join("rq5", "rq5_quatrain_predictions.csv")
    patches.to_csv(ODS_RESULTS_CSV, index=False)