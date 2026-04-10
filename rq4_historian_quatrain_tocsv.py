
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
from rq5_llms import get_methods_and_save, normalize_names_and_save, get_single_methods_and_save, get_files_and_save

from rq4_historian_cache import get_cache_labels
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
    # READ LABELS
    with open(os.path.join(RQ4_DIR, "log3.log"), "r") as f:
        labels_row = f.read()

    parts = labels_row.split("----------------------------------------")[:-1]

    quatrain_template = pd.read_csv(os.path.join(RQ4_DIR, "quatrain_predictions.csv"))

    results = []

    for part in parts:
        # Get the Number out UID: 812 Predicted Label:
        match = re.search(r"UID:\s*(\d+)\s*\n", part)

        if match:
            uid = int(match.group(1))
            bug_uid = quatrain_template.iloc[uid]["location"].split("/")[-1].replace(".patch", "")

            if "INCORRECT" in part:
                predicted_label = "Overfitting"
            
            elif "CORRECT" in part:
                predicted_label = "Correct"

            else:  
                raise ValueError("No predicted label found in part.")

        else:
            raise ValueError("No match found in part.")

        # Create a json with uid as "tool_patch_uid" and predicted label as "Predicted value"
        result = {
            "tool_patch_uid": bug_uid,
            "Predicted value": predicted_label
        }
        results.append(result)
    
    quatrain_template = pd.DataFrame(results)
    quatrain_template.to_csv(os.path.join(RQ4_DIR, "Quatrain.csv"), index=False)


    # print(quatrain_template)