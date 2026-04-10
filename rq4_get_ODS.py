
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



if __name__ == "__main__":
    ODS_RESULTS_CSV = os.path.join("notes", "rq5_ods_predictions.csv")

    if not os.path.exists(ODS_RESULTS_CSV):
        patches = pd.read_pickle(TMP_LLM4PC_FILES_PKL)
        ODS_DIR = "/home/sahand/coming2/coming"
        
        ods_predictions = []
        
        for index, row in tqdm(patches.iterrows(), total=len(patches), desc="Processing patches"):
            patch_id = row.name  # or index if no name
            actual_correctness = row['correctness']
            
            # Get source and target files
            source_files = row['source_files']
            target_files = row['target_files']
            
            # Create command with all file pairs (make paths absolute)
            files_args = []
            for src, tgt in zip(source_files, target_files):
                src_path = os.path.join(PROJECT_DIR, src)
                tgt_path = os.path.join(PROJECT_DIR, tgt)
                files_args.extend([src_path, tgt_path])
            
            cmd = f"python3 ods_predictor.py {' '.join(files_args)}"

            result = execute_bash_command(cmd, ODS_DIR)
            
            # Extract prediction result (last line)
            prediction = result.strip().split('\n')[-1].replace('ODS Prediction: ', '')
            ods_predictions.append(prediction)
            
            logging.info(f"Patch: {patch_id} | Actual: {actual_correctness} | ODS: {prediction}")
        
        # Add ODS column
        patches['ods_prediction'] = ods_predictions
        
        # Save to CSV
        patches.to_csv(ODS_RESULTS_CSV)

    ods_labels_raw = pd.read_csv(ODS_RESULTS_CSV)


    ods_labels = ods_labels_raw[['uid', 'correctness', 'ods_prediction']].rename(columns={'uid':'tool_patch_uid', 'correctness': 'Oracle', 'ods_prediction': 'Predicted value'}).copy()
    ods_labels["Predicted value"] = ods_labels["Predicted value"].str.capitalize()

    ODS_LABELS_RQ5_DIR = os.path.join(RQ4_DIR, "ODS.csv")
    ods_labels.to_csv(ODS_LABELS_RQ5_DIR, index=False)



# Index	Patch	Oracle	Predicted value	tool_patch_uid
# 0	0	correct/patch1-Chart-1-AVATAR.patch	correct	Overfitting	llm4pc-defects4j-Chart-1-AVATAR-patch1
# 1	1	correct/patch1-Chart-1-SequenceR.patch	correct	Overfitting	llm4pc-defects4j-Chart-1-SequenceR-patch1
# 2	2	correct/patch1-Chart-1-dev.patch	correct	Overfitting	llm4pc-defects4j-Chart-1-dev-patch1
# 3	3	correct/patch1-Chart-10-dev.patch	correct	Correct	llm4pc-defects4j-Chart-10-dev-patch1
# 4	4	correct/patch1-Chart-11-AVATAR.patch	correct	Correct	llm4pc-defects4j-Chart-11-AVATAR-patch1



# bug_uid	generator	location	correctness	origin	source_methods	target_methods	generator_id	source_files	target_files
# uid										
# llm4pc-defects4j-Lang-7-Arja-patch1	defects4j-Lang-7	Arja	tmp/patches/cleaned/llm4pc-defects4j-Lang-7-Arja-patch1.patch	Overfitting	LLM4PC	[tmp/methods/llm4pc-defects4j-Lang-7-Arja-patch1/source-0.java, tmp/methods/llm4pc-defects4j-Lang-7-Arja-patch1/source-1.java]	[tmp/methods/llm4pc-defects4j-Lang-7-Arja-patch1/target-0.java, tmp/methods/llm4pc-defects4j-Lang-7-Arja-patch1/target-1.java]	arja	[tmp/files/llm4pc-defects4j-Lang-7-Arja-patch1/source-0-EventListenerSupport.java, tmp/files/llm4pc-defects4j-Lang-7-Arja-patch1/source-1-NumberUtils.java]	[tmp/files/llm4pc-defects4j-Lang-7-Arja-patch1/target-0-EventListenerSupport.java, tmp/files/llm4pc-defects4j-Lang-7-Arja-patch1/target-1-NumberUtils.java]
# llm4pc-defects4j-Lang-20-Arja-patch1	defects4j-Lang-20	Arja	tmp/patches/cleaned/llm4pc-defects4j-Lang-20-Arja-patch1.patch	Overfitting	LLM4PC	[tmp/methods/llm4pc-defects4j-Lang-20-Arja-patch1/source-0.java]	[tmp/methods/llm4pc-defects4j-Lang-20-Arja-patch1/target-0.java]	arja	[tmp/files/llm4pc-defects4j-Lang-20-Arja-patch1/source-0-StringUtils.java]	[tmp/files/llm4pc-defects4j-Lang-20-Arja-patch1/target-0-StringUtils.java]
# llm4pc-defects4j-Lang-16-Arja-patch1	defects4j-Lang-16	Arja	tmp/patches/cleaned/llm4pc-defects4j-Lang-16-Arja-patch1.patch	Overfitting	LLM4PC	[tmp/methods/llm4pc-defects4j-Lang-16-Arja-patch1/source-0.java]	[tmp/methods/llm4pc-defects4j-Lang-16-Arja-patch1/target-0.java]	arja	[tmp/files/llm4pc-defects4j-Lang-16-Arja-patch1/source-0-NumberUtils.java]	[tmp/files/llm4pc-defects4j-Lang-16-Arja-patch1/target-0-NumberUtils.java]
# llm4pc-defects4j-Closure-22-Arja-patch1	defects4j-Closure-22	Arja	tmp/patches/cleaned/llm4pc-defects4j-Closure-22-Arja-patch1.patch	Overfitting	LLM4PC	[tmp/methods/llm4pc-defects4j-Closure-22-Arja-patch1/source-0.java]	[tmp/methods/llm4pc-defects4j-Closure-22-Arja-patch1/target-0.java]	arja	[tmp/files/llm4pc-defects4j-Closure-22-Arja-patch1/source-0-CheckSideEffects.java]	[tmp/files/llm4pc-defects4j-Closure-22-Arja-patch1/target-0-CheckSideEffects.java]
# llm4pc-defects4j-Closure-130-Arja-patch1	defects4j-Closure-130	Arja	tmp/patches/cleaned/llm4pc-defects4j-Closure-130-Arja-patch1.patch	Overfitting	LLM4PC	[tmp/methods/llm4pc-defects4j-Closure-130-Arja-patch1/source-0.java]	[tmp/methods/llm4pc-defects4j-Closure-130-Arja-patch1/target-0.java]	arja	[tmp/files/llm4pc-defects4j-Closure-130-Arja-patch1/source-0-CollapseProperties.java]