
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

def majority_vote_labels(df, label_column="predicted_label", id_column="tool_patch_uid"):
    voted_labels = {}

    for patch_uid, group in df.groupby(id_column):
        # Filter out 'Unknown' labels
        non_unknown_labels = group[group[label_column] != "Unknown"][label_column]
        label_counts = Counter(non_unknown_labels)

        if not label_counts:
            voted_labels[patch_uid] = "Unknown"
            continue

        most_common = label_counts.most_common()
        
        # Check for tie among top non-'Unknown' labels
        if len(most_common) > 1 and most_common[0][1] == most_common[1][1]:
            voted_labels[patch_uid] = "Unknown"
        else:
            voted_labels[patch_uid] = most_common[0][0]

    return pd.Series(voted_labels)


class Results:
    def __init__(self):
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

        logging.info(f"Bugs: {bugs.head()}, # Developer Patches: {len(developer_patches)}, # Tool Patches: {len(tool_patches)}, Patch Processors: {self.patch_processors}, Models: {self.models}, Prompts: {self.prompts}, Temperatures: {self.temperatures}")

        report_dataset(self.input_developer_patches, self.input_tool_patches, bugs)

        self.results = self._get_results()

        self.pipe = pipeline(model="facebook/bart-large-mnli", device=0)


    def _get_results(self):
        results = []

        for processor in self.patch_processors:
            for model in self.models:
                for temperature in self.temperatures:
                    for prompt in self.prompts:
                        prompt_uid = prompt["uid"]
                        temperature_value = temperature["uid"]
                        model_uid = model["uid"]
                        processor_uid = processor["uid"]
                        file_name = f"EXP1-{processor_uid}-{model_uid}-{temperature_value}-{prompt_uid}.pkl"
                        result_file = os.path.join(TMP_RESULTS_DIR, file_name)

                        result = {
                            "processor": processor,
                            "model": model,
                            "temperature": temperature,
                            "prompt": prompt,
                            "file_name": file_name,                           
                            "result_file": result_file,
                        }

                        results.append(result)

        return results
    
    def _classify_text(self, text: str) -> str:
        result = self.pipe(text, candidate_labels=self.labels)
        predicted_label = result["labels"][0]

        return predicted_label

    def classify(self, labels: list, selected_results=None):
        if not selected_results:
            selected_results = self.results

        for result in selected_results:
            self.labels = labels
            classified_result_dir = os.path.join(TMP_CLASSIFICATION_RESULTS_DIR, f"{'-'.join(labels)}-{result['file_name']}")
            result[f"classified_result_file_{'-'.join(labels)}"] = classified_result_dir

            if os.path.exists(classified_result_dir):
                logging.info(f"Skipping classification for {result['file_name']} as it already exists.")

                continue

            else:
                logging.info(f"Classifying Texts {classified_result_dir}")

                df = pd.read_pickle(result["result_file"])

                tqdm.pandas(desc=f"Classifying Texts {classified_result_dir}")

                df["predicted_label"] = df["response"].progress_apply(self._classify_text)

                df.to_pickle(classified_result_dir)

        logging.info("Classification Done.")



class Evaluator:
    def __init__(self, results: Results):
        self.results = results

        # Simple Prompts Yes/No (simple-results-exp1.csv) (semantical similar, identical and code clone y/n * 3) Actual Recall
        simple_prompts = [
            "llm4cc-simple_prompt",
            "llm4cc-reasoning",
            "llm4cc-similarity_line",

            "llm4cc-simple_prompt-patch",
            "llm4cc-reasoning-patch",
            "llm4cc-similarity_line-patch",

            "llm4cc-simple_prompt-semantical",
            "llm4cc-reasoning-patch-semantical",
            "llm4cc-similarity_line-patch-semantical",

            "llm4cc-simple_prompt-identical",
            "llm4cc-reasoning-patch-identical",
            "llm4cc-similarity_line-patch-identical"
        ]
        simple_results = [result for result in self.results.results if result["prompt"]["uid"] in simple_prompts]
        self._get_simple_results_table(simple_results, ["yes", "no"])

        # Simple Prompts Type (type-results-exp1.csv)  (per-class recall)
        type_prompts = [
            "llm4cc-clone_type",
            "llm4cc-integrated",
            "llm4cc-clone_type-patch",
            "llm4cc-integrated-patch",
        ]
        type_results = [result for result in self.results.results if result["prompt"]["uid"] in type_prompts]
        self._get_type_results_table(type_results, ["type-1", "type-2", "type-3", "type-4"], pd.read_pickle(TMP_EXPERT_CORRECT_LABEL_PKL))

        # Calculate Cohen's kappa values (cohens-kappa-exp1.csv)
        self._calculate_cohens_kappa(type_results, ["type-1", "type-2", "type-3", "type-4"], pd.read_pickle(TMP_EXPERT_CORRECT_LABEL_PKL))
    
    def _get_recall_simple(self, results, labels):
        recalls = []

        for result in results:
            classified_result_dir = result[f"classified_result_file_{'-'.join(labels)}"]

            df = pd.read_pickle(classified_result_dir)

            recall = len(df[df["predicted_label"] == "yes"]) / len(df)

            recalls.append(recall)

        return recalls

    def _get_simple_results_table(self, results, labels):
        recalls = self._get_recall_simple(results, labels)

        table = pd.DataFrame({
            "Processor": [result["processor"]["uid"] for result in results],
            "Prompt": [result["prompt"]["uid"] for result in results],
            "Model": [result["model"]["uid"] for result in results],
            "Recall": recalls
        })

        table.to_csv(TMP_SIMPLE_RESULTS_CSV)

    def _get_recall_type(self, results, labels, ground_truth):
        recalls = {label: [] for label in labels}
        macros = []
        micros = []
        kappas = []

        for result in results:
            classified_result_dir = result[f"classified_result_file_{'-'.join(labels)}"]

            df = pd.read_pickle(classified_result_dir)
            df = df[df.index.isin(ground_truth.index)]
            df["expert_label"] = df.index.map(ground_truth["expert_label"])

            per_label_recalls = []
            true_positive_total = 0
            actual_positive_total = 0

            for label in labels:
                true_positives = ((df["predicted_label"] == label) & (df["expert_label"] == label)).sum()
                actual_positives = (df["expert_label"] == label).sum()

                recall = true_positives / actual_positives if actual_positives > 0 else 0
                recalls[label].append(recall)
                per_label_recalls.append(recall)

                true_positive_total += true_positives
                actual_positive_total += actual_positives

            # Macro recall
            macros.append(sum(per_label_recalls) / len(labels))

            # Micro recall
            micro_recall = true_positive_total / actual_positive_total if actual_positive_total > 0 else 0
            micros.append(micro_recall)

            # Cohen's kappa (across all 4 labels)
            kappa = cohen_kappa_score(df["expert_label"], df["predicted_label"], labels=labels)
            kappas.append(kappa)

        return recalls, macros, micros, kappas


    def _get_type_results_table(self, results, labels, ground_truth):
        recalls, macros, micros, kappas = self._get_recall_type(results, labels, ground_truth)

        table_data = {
            "Processor": [result["processor"]["uid"] for result in results],
            "Prompt": [result["prompt"]["uid"] for result in results],
            "Model": [result["model"]["uid"] for result in results],
            "Micro": micros,
            "Macro": macros,
            "Kappa": kappas
        }

        for label in labels:
            table_data[f"Recall_{label}"] = recalls[label]

        table = pd.DataFrame(table_data)

        table.to_csv(os.path.join(TMP_TYPE_RESULTS_CSV))

    def _calculate_cohens_kappa(self, results, labels, ground_truth):
        kappa_values = []

        for (result1, result2) in itertools.combinations(results, 2):
            classified_result_dir1 = result1[f"classified_result_file_{'-'.join(labels)}"]
            classified_result_dir2 = result2[f"classified_result_file_{'-'.join(labels)}"]

            df1 = pd.read_pickle(classified_result_dir1)
            df2 = pd.read_pickle(classified_result_dir2)

            common_indices = df1.index.intersection(df2.index).intersection(ground_truth.index)
            df1 = df1.loc[common_indices]
            df2 = df2.loc[common_indices]

            kappa = cohen_kappa_score(df1["predicted_label"], df2["predicted_label"])

            kappa_values.append({
                "Model1": result1["model"]["uid"],
                "Model2": result2["model"]["uid"],
                "Prompt1": result1["prompt"]["uid"],
                "Prompt2": result2["prompt"]["uid"],
                "Processor1": result1["processor"]["uid"],
                "Processor2": result2["processor"]["uid"],
                "Kappa": kappa
            })

        kappa_df = pd.DataFrame(kappa_values)
        kappa_df.to_csv(TMP_COHENS_KAPPA_CSV)


class Plotter:
        
    def __init__(self):
        # Simple 
        ## Method Representation
        results = self._get_simple_results("llm4cc-simple_prompt", "method")
        logging.info("Prompt: llm4cc-simple_prompt, Processor_method")
        logging.info(results)
        results = self._get_simple_results("llm4cc-reasoning", "method")
        logging.info("Prompt: llm4cc-reasoning, Processor_method")
        logging.info(results)
        results = self._get_simple_results("llm4cc-similarity_line", "method")
        logging.info("Prompt: llm4cc-similarity_line, Processor_method")
        logging.info(results)

        logging.info("-----------------------------------")

        results = self._get_simple_results("llm4cc-simple_prompt-semantical", "method")
        logging.info("Prompt: llm4cc-simple_prompt-semantical, Processor_method")
        logging.info(results)
        results = self._get_simple_results("llm4cc-reasoning-patch-semantical", "method")
        logging.info("Prompt: llm4cc-reasoning-patch-semantical, Processor_method")
        logging.info(results)
        results = self._get_simple_results("llm4cc-similarity_line-patch-semantical", "method")
        logging.info("Prompt: llm4cc-similarity_line-patch-semantical, Processor_method")
        logging.info(results)
        
        logging.info("-----------------------------------")

        results = self._get_simple_results("llm4cc-simple_prompt-identical", "method")
        logging.info("Prompt: llm4cc-simple_prompt-identical, Processor_method")
        logging.info(results)
        results = self._get_simple_results("llm4cc-reasoning-patch-identical", "method")
        logging.info("Prompt: llm4cc-reasoning-patch-identical, Processor_method")
        logging.info(results)
        results = self._get_simple_results("llm4cc-similarity_line-patch-identical", "method")
        logging.info("Prompt: llm4cc-similarity_line-patch-identical, Processor_method")
        logging.info(results)

        logging.info("-----------------------------------")

        ## Patch Representation
        results = self._get_simple_results("llm4cc-simple_prompt-patch", "defaultpatch")
        logging.info("Prompt: llm4cc-simple_prompt-patch, Processor_defaultpatch")
        logging.info(results)
        results = self._get_simple_results("llm4cc-reasoning-patch", "defaultpatch")
        logging.info("Prompt: llm4cc-reasoning-patch, Processor_defaultpatch")
        logging.info(results)
        results = self._get_simple_results("llm4cc-similarity_line-patch", "defaultpatch")
        logging.info("Prompt: llm4cc-similarity_line-patch, Processor_defaultpatch")
        logging.info(results)

        logging.info("-----------------------------------")

        results = self._get_simple_results("llm4cc-simple_prompt-semantical", "defaultpatch")
        logging.info("Prompt: llm4cc-simple_prompt-semantical, Processor_defaultpatch")
        logging.info(results)
        results = self._get_simple_results("llm4cc-reasoning-patch-semantical", "defaultpatch")
        logging.info("Prompt: llm4cc-reasoning-patch-semantical, Processor_defaultpatch")
        logging.info(results)
        results = self._get_simple_results("llm4cc-similarity_line-patch-semantical", "defaultpatch")
        logging.info("Prompt: llm4cc-similarity_line-patch-semantical, Processor_defaultpatch")
        logging.info(results)

        logging.info("-----------------------------------")

        results = self._get_simple_results("llm4cc-simple_prompt-identical", "defaultpatch")
        logging.info("Prompt: llm4cc-simple_prompt-identical, Processor_defaultpatch")
        logging.info(results)
        results = self._get_simple_results("llm4cc-reasoning-patch-identical", "defaultpatch")
        logging.info("Prompt: llm4cc-reasoning-patch-identical, Processor_defaultpatch")
        logging.info(results)
        results = self._get_simple_results("llm4cc-similarity_line-patch-identical", "defaultpatch")
        logging.info("Prompt: llm4cc-similarity_line-patch-identical, Processor_defaultpatch")
        logging.info(results)

        logging.info("-----------------------------------")

        # Type
        results = self._get_type_results("llm4cc-clone_type", "method")
        logging.info(results)
        results = self._get_type_results("llm4cc-integrated", "method")
        logging.info(results)

        logging.info("-----------------------------------")

        results = self._get_type_results("llm4cc-clone_type-patch", "defaultpatch")
        logging.info(results)
        results = self._get_type_results("llm4cc-integrated-patch", "defaultpatch")
        logging.info(results)


    def _get_simple_results(self, prompt, representation):
        simple_df = pd.read_csv(TMP_SIMPLE_RESULTS_CSV)
        simple_df = simple_df[simple_df["Prompt"] == prompt]
        simple_df = simple_df[simple_df["Processor"] == representation]

        return simple_df

    def _get_type_results(self, prompt, representation):
        type_df = pd.read_csv(TMP_TYPE_RESULTS_CSV)
        type_df = type_df[type_df["Prompt"] == prompt]
        type_df = type_df[type_df["Processor"] == representation]

        return type_df


class Experiment2Results:
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

        if input_processors:
            self.patch_processors = [processor for processor in self.patch_processors if processor["uid"] in input_processors]

        if input_models:
            self.models = [model for model in self.models if model["uid"] in input_models]

        if input_prompts:
            self.prompts = [prompt for prompt in self.prompts if prompt["uid"] in input_prompts]

        logging.info(f"Experiment 2, Selected Tool: {selected_tool}, Bugs: {bugs.head()}, # Developer Patches: {len(developer_patches)}, # Tool Patches: {len(tool_patches)}, Patch Processors: {self.patch_processors}, Models: {self.models}, Prompts: {self.prompts}, Temperatures: {self.temperatures}")

        report_dataset(self.input_developer_patches, self.input_tool_patches, bugs)

        self._merge_results(110)

        self.results = self._get_results()

        self.pipe = pipeline(model="facebook/bart-large-mnli", device=0)

    def _merge_results(self, no_selected):
        logging.info("Merging Results ...")

        for processor in self.patch_processors:
            for model in self.models:
                for temperature in self.temperatures:
                    for prompt in self.prompts:
                        final_result_file = os.path.join(TMP_RESULTS_DIR, f"EXP2-{self.selected_tool}-{processor['uid']}-{model['uid']}-{temperature['uid']}-{prompt['uid']}.pkl")

                        if os.path.exists(final_result_file):
                            logging.info(f"Skipping merging for {final_result_file} as it already exists.")
                            
                            continue

                        try:
                            for i in range(self.no_selected_tool_patches):
                                result_file = os.path.join(TMP_RESULTS_DIR, f"EXP2-{self.selected_tool}-{processor['uid']}-{model['uid']}-{temperature['uid']}-{prompt['uid']}-{i}.pkl")
                                df = pd.read_pickle(result_file)
                            
                                if i == 0:
                                    combined_df = df
                            
                                else:
                                    combined_df = pd.concat([combined_df, df])
                        except:
                            for i in range(no_selected):
                                result_file = os.path.join(TMP_RESULTS_DIR, f"EXP2-{self.selected_tool}-{processor['uid']}-{model['uid']}-{temperature['uid']}-{prompt['uid']}-{i}.pkl")
                                df = pd.read_pickle(result_file)

                                if i == 0:
                                    combined_df = df

                                else:
                                    combined_df = pd.concat([combined_df, df])

                        combined_df.to_pickle(final_result_file)
                        
    def _get_results(self):
        results = []

        for processor in self.patch_processors:
            for model in self.models:
                for temperature in self.temperatures:
                    for prompt in self.prompts:
                        logging.info(f"Getting Results for {processor['uid']}, {model['uid']}, {temperature['uid']}, {prompt['uid']}")

                        file_name = f"EXP2-{self.selected_tool}-{processor['uid']}-{model['uid']}-{temperature['uid']}-{prompt['uid']}.pkl"
                        result_file = os.path.join(TMP_RESULTS_DIR, file_name)

                        result = {
                            "tool": self.selected_tool,
                            "processor": processor,
                            "model": model,
                            "temperature": temperature,
                            "prompt": prompt,
                            "file_name": file_name,                           
                            "result_file": result_file,
                        }

                        results.append(result)

        logging.info(f"Results: {results}")

        return results
        
    def _classify_text(self, text: str) -> str:
        result = self.pipe(text, candidate_labels=self.labels)
        predicted_label = result["labels"][0]

        return predicted_label

    def classify(self, labels: list, selected_results=None):
        if not selected_results:
            selected_results = self.results

        for result in selected_results:
            self.labels = labels
            classified_result_dir = os.path.join(TMP_CLASSIFICATION_RESULTS_DIR, f"{'-'.join(labels)}-{result['file_name']}")
            result[f"classified_result_file_{'-'.join(labels)}"] = classified_result_dir

            if os.path.exists(classified_result_dir):
                logging.info(f"Skipping classification for {result['file_name']} as it already exists.")

                continue

            else:
                logging.info(f"Classifying Texts {classified_result_dir}")

                df = pd.read_pickle(result["result_file"])

                tqdm.pandas(desc=f"Classifying Texts {classified_result_dir}")

                df["predicted_label"] = df["response"].progress_apply(self._classify_text)

                df.to_pickle(classified_result_dir)

        logging.info("Classification Done.")

class Experiment2Evaluator:
    def __init__(self, results: Results):
        self.results = results

        # Simple Prompts Yes/No (translate yes no to overfitting and correct) Doing Majority Vote, Inverted
        simple_prompts = [
            "llm4cc-simple_prompt-semantical",
            "llm4cc-reasoning-patch-semantical",
            "llm4cc-similarity_line-patch-semantical",

            "llm4cc-simple_prompt-identical",
            "llm4cc-reasoning-patch-identical",
            "llm4cc-similarity_line-patch-identical",
        ]
        simple_results = [result for result in self.results.results if result["prompt"]["uid"] in simple_prompts]
        self._get_simple_results_table(simple_results, ["yes", "no"], results.all_groundtruth_patches_uid_deduplicated)

        # Simple Prompts Type Binary (translate type to overfitting and correct) Doing Majority Vote, Inverted
        type_binary_prompts = [
            "llm4cc-clone_type",
            "llm4cc-integrated",
            "llm4cc-clone_type-patch",
            "llm4cc-integrated-patch",
        ]
        type_binary_results = [result for result in self.results.results if result["prompt"]["uid"] in type_binary_prompts]
        self._get_type_binary_results_table(type_binary_results, ["type-1", "type-2", "type-3", "type-4", "not-clone"], results.all_groundtruth_patches_uid_deduplicated)

        # Simple Prompts Type Expert Label (translate type to overfitting and correct) Not Doing Majority Vote
        self._get_type_expert_label_results_table(pd.read_pickle(TMP_EXPERT_CORRECT_LABEL_PKL_EXP2), results.all_groundtruth_patches_uid_deduplicated)

        # Simple Prompts Type (clone prompts with type, Not translating to Correct/Overfitting, 4 F1: against expert label) Not Doing Majority Vote
        type_prompts = [
            "llm4cc-clone_type",
            "llm4cc-integrated",
            "llm4cc-clone_type-patch",
            "llm4cc-integrated-patch",
        ]
        type_results = [result for result in self.results.results if result["prompt"]["uid"] in type_prompts]
        self._get_type_results_table(type_results, ["type-1", "type-2", "type-3", "type-4", "not-clone"], pd.read_pickle(TMP_EXPERT_CORRECT_LABEL_PKL_EXP2))

        # Simple Prompts Yes/No (translate yes no (clone version) to overfitting and correct)) Not Doing Majority Vote
        if "method" in [processor["uid"] for processor in results.patch_processors]:
            simple_clone_prompts = [
                "llm4cc-simple_prompt",
                "llm4cc-reasoning",
                "llm4cc-similarity_line",
            ]
        
        elif "defaultpatch" in [processor["uid"] for processor in results.patch_processors]:
            simple_clone_prompts = [
                "llm4cc-simple_prompt-patch",
                "llm4cc-reasoning-patch",
                "llm4cc-similarity_line-patch",
            ]

        else:
            simple_clone_prompts = []
            
        simple_clone_results = [result for result in self.results.results if result["prompt"]["uid"] in simple_clone_prompts]
        self._get_simple_clone_results_table(simple_clone_results, ["yes", "no"], results.all_groundtruth_patches_uid_deduplicated)


    def _translate_simple_label_to_binary(self, groundtruth_correctness, label): #
        if groundtruth_correctness == "Correct" and label == "no":
            return "Overfitting"

        elif groundtruth_correctness == "Correct" and label == "yes":
            return "Correct"

        elif groundtruth_correctness == "Overfitting" and label == "yes":
            return "Overfitting"

        return "Unknown"

    # Deal with the logic in a result file and assign values to it
    def _get_f1_simple(self, results, labels, ground_truth): #
        f1_values = []
        support = []
        total = []
        tp_values = []
        fp_values = []
        tn_values = []
        fn_values = []

        for result in results:
            print(pd.read_pickle(result["result_file"]))
            print(result["result_file"])
            classified_result_dir = result[f"classified_result_file_{'-'.join(labels)}"]

            df = pd.read_pickle(classified_result_dir)

            ground_truth = ground_truth[~ground_truth.index.duplicated(keep='first')]
            df["groundtruth_correctness"] = ground_truth.loc[df["groundtruth_patch_uid"]]["correctness"].values

            df["raw_predicted_correctness"] = df.apply(lambda x: self._translate_simple_label_to_binary(x["groundtruth_correctness"], x["predicted_label"]), axis=1)
            majority_labels = majority_vote_labels(df, label_column="raw_predicted_correctness", id_column="tool_patch_uid")
            df_voted = df[["tool_patch_uid"]].drop_duplicates().copy()

            df_voted["selected_correctness"] = ground_truth.loc[df_voted["tool_patch_uid"]]["correctness"].values
            df_voted["predicted_correctness"] = df_voted["tool_patch_uid"].map(majority_labels)

            df = df_voted.copy()

            # Get Support and Drop Unknowns
            unknown_count = df["predicted_correctness"].value_counts().get("Unknown", 0)
            total_count = len(df)
            support.append(total_count - unknown_count)
            total.append(total_count)
            df = df[df["predicted_correctness"] != "Unknown"]

            df["predicted_correctness_binary"] = df["predicted_correctness"].apply(lambda x: 0 if x == "Correct" else 1)
            df["selected_correctness_binary"] = df["selected_correctness"].apply(lambda x: 0 if x == "Correct" else 1)

            # Calculate TP, FP, TN, FN
            tp = ((df["predicted_correctness_binary"] == 1) & (df["selected_correctness_binary"] == 1)).sum()
            fp = ((df["predicted_correctness_binary"] == 1) & (df["selected_correctness_binary"] == 0)).sum()
            tn = ((df["predicted_correctness_binary"] == 0) & (df["selected_correctness_binary"] == 0)).sum()
            fn = ((df["predicted_correctness_binary"] == 0) & (df["selected_correctness_binary"] == 1)).sum()
            
            tp_values.append(tp)
            fp_values.append(fp)
            tn_values.append(tn)
            fn_values.append(fn)

            f1 = f1_score(df["selected_correctness_binary"], df["predicted_correctness_binary"], zero_division=0)
            f1_values.append(f1)
            
        return f1_values, support, total, tp_values, fp_values, tn_values, fn_values

    def _get_simple_results_table(self, results, labels, ground_truth): #
        f1_values, support, total, tp_values, fp_values, tn_values, fn_values = self._get_f1_simple(results, labels, ground_truth)

        table_data = {
            "Processor": [result["processor"]["uid"] for result in results],
            "Prompt": [result["prompt"]["uid"] for result in results],
            "Model": [result["model"]["uid"] for result in results],
            "F1": f1_values,
            "Support": support,
            "Total": total,
            "TP": tp_values,
            "FP": fp_values,
            "TN": tn_values,
            "FN": fn_values
        }

        table = pd.DataFrame(table_data)

        table.to_csv(TMP_SIMPLE_RESULTS_CSV_EXP2)
        

    def _translate_type_label_to_binary(self, groundtruth_correctness, label): #
        if groundtruth_correctness == "Correct" and label == "not-clone":
            return "Overfitting" 

        elif groundtruth_correctness == "Correct" and label in ["type-1", "type-2", "type-4"]:
            return "Correct"

        elif groundtruth_correctness == "Overfitting" and label in ["type-1", "type-2", "type-4"]:
            return "Overfitting"

        return "Unknown"

    def _get_f1_type_binary(self, results, labels, ground_truth): #
        f1_values = []
        support = []
        total = []
        tp_values = []
        fp_values = []
        tn_values = []
        fn_values = []

        for result in results:
            classified_result_dir = result[f"classified_result_file_{'-'.join(labels)}"]

            df = pd.read_pickle(classified_result_dir)

            df["groundtruth_correctness"] = ground_truth.loc[df["groundtruth_patch_uid"]]["correctness"].values

            # Get Raw Translation
            df["raw_predicted_binary_label"] = df.apply(lambda x: self._translate_type_label_to_binary(x["groundtruth_correctness"], x["predicted_label"]), axis=1)

            # Use Raw to Get Majority Vote
            majority_labels = majority_vote_labels(df, label_column="raw_predicted_binary_label", id_column="tool_patch_uid")
            df_voted = df[["tool_patch_uid"]].drop_duplicates().copy()

            df_voted["selected_correctness"] = ground_truth.loc[df_voted["tool_patch_uid"]]["correctness"].values
            df_voted["predicted_binary_label"] = df_voted["tool_patch_uid"].map(majority_labels)

            df = df_voted.copy()

            # Get Support and Drop Unknowns
            unknown_count = df["predicted_binary_label"].value_counts().get("Unknown", 0)
            total_count = len(df)
            support.append(total_count - unknown_count)
            total.append(total_count)
            df = df[df["predicted_binary_label"] != "Unknown"]

            df["predicted_correctness_binary"] = df["predicted_binary_label"].apply(lambda x: 0 if x == "Correct" else 1)
            df["selected_correctness_binary"] = df["selected_correctness"].apply(lambda x: 0 if x == "Correct" else 1)

            # Calculate TP, FP, TN, FN
            tp = ((df["predicted_correctness_binary"] == 1) & (df["selected_correctness_binary"] == 1)).sum()
            fp = ((df["predicted_correctness_binary"] == 1) & (df["selected_correctness_binary"] == 0)).sum()
            tn = ((df["predicted_correctness_binary"] == 0) & (df["selected_correctness_binary"] == 0)).sum()
            fn = ((df["predicted_correctness_binary"] == 0) & (df["selected_correctness_binary"] == 1)).sum()
            
            tp_values.append(tp)
            fp_values.append(fp)
            tn_values.append(tn)
            fn_values.append(fn)

            f1 = f1_score(df["selected_correctness_binary"], df["predicted_correctness_binary"], zero_division=0)
            f1_values.append(f1)
            
        return f1_values, support, total, tp_values, fp_values, tn_values, fn_values

    def _get_type_binary_results_table(self, results, labels, ground_truth): #
        f1_values, support, total, tp_values, fp_values, tn_values, fn_values = self._get_f1_type_binary(results, labels, ground_truth)

        table_data = {
            "Processor": [result["processor"]["uid"] for result in results],
            "Prompt": [result["prompt"]["uid"] for result in results],
            "Model": [result["model"]["uid"] for result in results],
            "F1": f1_values,
            "Support": support,
            "Total": total,
            "TP": tp_values,
            "FP": fp_values,
            "TN": tn_values,
            "FN": fn_values
        }

        table = pd.DataFrame(table_data)

        table.to_csv(TMP_TYPE_BINARY_RESULTS_CSV_EXP2)


    def _get_f1_type_binary_exper_label(self, exper_label, ground_truth): #
        exper_label = exper_label.copy()

        # Deduplicate ground_truth with same index
        ground_truth = ground_truth.loc[~ground_truth.index.duplicated(keep='first')]

        exper_label["groundtruth_correctness"] = ground_truth.loc[exper_label["groundtruth_index"]]["correctness"].values
        exper_label["predicted_binary_label"] = exper_label.apply(lambda x: self._translate_type_label_to_binary(x["groundtruth_correctness"], x["expert_label"]), axis=1)

        majority_labels = majority_vote_labels(exper_label, label_column="predicted_binary_label", id_column="uid")

        # Check exp7 branch for numbers 139 TBar is 40/99
        with open(TMP_TYPE_BINARY_VOTED_EXPER_LABEL_RESULTS_EXP2, "w") as f:
            f.write(str(majority_labels.value_counts()))

        exper_label["selected_correctness"] = ground_truth.loc[exper_label["uid"]]["correctness"].values

        # Get Support and Drop Unknowns
        unknown_count = exper_label["predicted_binary_label"].value_counts().get("Unknown", 0)  
        total_count = len(exper_label)
        support = total_count - unknown_count
        exper_label = exper_label[exper_label["predicted_binary_label"] != "Unknown"]

        exper_label["predicted_correctness_binary"] = exper_label["predicted_binary_label"].apply(lambda x: 1 if x == "Correct" else 0)
        exper_label["selected_correctness_binary"] = exper_label["selected_correctness"].apply(lambda x: 1 if x == "Correct" else 0)

        # Calculate TP, FP, TN, FN
        tp = ((exper_label["predicted_correctness_binary"] == 1) & (exper_label["selected_correctness_binary"] == 1)).sum()
        fp = ((exper_label["predicted_correctness_binary"] == 1) & (exper_label["selected_correctness_binary"] == 0)).sum()
        tn = ((exper_label["predicted_correctness_binary"] == 0) & (exper_label["selected_correctness_binary"] == 0)).sum()
        fn = ((exper_label["predicted_correctness_binary"] == 0) & (exper_label["selected_correctness_binary"] == 1)).sum()

        f1 = f1_score(exper_label["selected_correctness_binary"], exper_label["predicted_correctness_binary"], zero_division=0)

        return [f1], [support], [total_count], [tp], [fp], [tn], [fn]

    def _get_type_expert_label_results_table(self, exper_label, ground_truth): #
        f1, support, total, tp, fp, tn, fn = self._get_f1_type_binary_exper_label(exper_label, ground_truth)

        table_data = {
            "Model": ["Expert"],
            "F1": f1,
            "Support": support,
            "Total": total,
            "TP": tp,
            "FP": fp,
            "TN": tn,
            "FN": fn
        }

        table = pd.DataFrame(table_data)

        table.to_csv(TMP_TYPE_BINARY_EXPER_LABEL_RESULTS_CSV_EXP2)


    def _get_f1_type(self, results, labels, ground_truth): ###
        reindexed_ground_truth = ground_truth.set_index(["uid", "groundtruth_index"])
    
        f1_values = {label: [] for label in labels}
        tp_values = {label: [] for label in labels}
        fp_values = {label: [] for label in labels}
        tn_values = {label: [] for label in labels}
        fn_values = {label: [] for label in labels}

        for result in results:
            classified_result_dir = result[f"classified_result_file_{'-'.join(labels)}"]

            df = pd.read_pickle(classified_result_dir)

            # Create a multi-index from the two columns in both dataframes
            df.set_index(["tool_patch_uid", "groundtruth_patch_uid"], inplace=True)

            # Filter the dataframes to only include matching indices
            common_indices = df.index.intersection(reindexed_ground_truth.index)
            df = df.loc[common_indices]
            ground_truth_filtered = reindexed_ground_truth.loc[common_indices]
            merged = df.copy()
            merged["expert_label"] = ground_truth_filtered["expert_label"]

            for label in labels:
                true_positives = len(merged[(merged["predicted_label"] == label) & (merged["expert_label"] == label)])
                false_positives = len(merged[(merged["predicted_label"] == label) & (merged["expert_label"] != label)])
                true_negatives = len(merged[(merged["predicted_label"] != label) & (merged["expert_label"] != label)])
                false_negatives = len(merged[(merged["predicted_label"] != label) & (merged["expert_label"] == label)])

                tp_values[label].append(true_positives)
                fp_values[label].append(false_positives)
                tn_values[label].append(true_negatives)
                fn_values[label].append(false_negatives)

                precision = true_positives / (true_positives + false_positives) if (true_positives + false_positives) > 0 else 0
                recall = true_positives / (true_positives + false_negatives) if (true_positives + false_negatives) > 0 else 0
                f1_score = 2 * (precision * recall) / (precision + recall) if (precision + recall) > 0 else 0

                f1_values[label].append(f1_score)

        return f1_values, tp_values, fp_values, tn_values, fn_values

    def _get_type_results_table(self, results, labels, ground_truth): #
        f1_values, tp_values, fp_values, tn_values, fn_values = self._get_f1_type(results, labels, ground_truth)

        table_data = {
            "Processor": [result["processor"]["uid"] for result in results],
            "Prompt": [result["prompt"]["uid"] for result in results],
            "Model": [result["model"]["uid"] for result in results],
        }

        for label in labels:
            table_data[f"F1_{label}"] = f1_values[label]
            table_data[f"TP_{label}"] = tp_values[label]
            table_data[f"FP_{label}"] = fp_values[label]
            table_data[f"TN_{label}"] = tn_values[label]
            table_data[f"FN_{label}"] = fn_values[label]

        table = pd.DataFrame(table_data)

        table.to_csv(os.path.join(TMP_TYPE_RESULTS_CSV_EXP2))
        

    def _translate_simple_clone_to_binary(self, groundtruth_correctness, label): #
        if groundtruth_correctness == "Correct" and label == "not-clone":
            return "Overfitting"
        
        return "Unknown"

    def _get_f1_simple_clone(self, results, labels, ground_truth): #
        f1_values = []
        support = []
        tp_values = []
        fp_values = []
        tn_values = []
        fn_values = []

        for result in results:
            classified_result_dir = result[f"classified_result_file_{'-'.join(labels)}"]

            df = pd.read_pickle(classified_result_dir)

            df["groundtruth_correctness"] = ground_truth.loc[df.index]["correctness"].values
            df["predicted_correctness"] = df.apply(lambda x: self._translate_simple_clone_to_binary(x["groundtruth_correctness"], x["predicted_label"]), axis=1)

            df["selected_correctness"] = ground_truth.loc[df.index]["correctness"].values

            # Get Support and Drop Unknowns
            unknown_count = df["predicted_correctness"].value_counts().get("Unknown", 0)
            total_count = len(df)
            support.append(total_count - unknown_count)
            df = df[df["predicted_correctness"] != "Unknown"]

            df["predicted_correctness_binary"] = df["predicted_correctness"].apply(lambda x: 1 if x == "Correct" else 0)
            df["selected_correctness_binary"] = df["selected_correctness"].apply(lambda x: 1 if x == "Correct" else 0)

            # Calculate TP, FP, TN, FN
            tp = ((df["predicted_correctness_binary"] == 1) & (df["selected_correctness_binary"] == 1)).sum()
            fp = ((df["predicted_correctness_binary"] == 1) & (df["selected_correctness_binary"] == 0)).sum()
            tn = ((df["predicted_correctness_binary"] == 0) & (df["selected_correctness_binary"] == 0)).sum()
            fn = ((df["predicted_correctness_binary"] == 0) & (df["selected_correctness_binary"] == 1)).sum()
            
            tp_values.append(tp)
            fp_values.append(fp)
            tn_values.append(tn)
            fn_values.append(fn)

            f1 = f1_score(df["selected_correctness_binary"], df["predicted_correctness_binary"], zero_division=0)
            f1_values.append(f1)
            
        return f1_values, support, tp_values, fp_values, tn_values, fn_values

    def _get_simple_clone_results_table(self, results, labels, ground_truth): #
        f1_values, support, tp_values, fp_values, tn_values, fn_values = self._get_f1_simple_clone(results, labels, ground_truth)

        table_data = {
            "Processor": [result["processor"]["uid"] for result in results],
            "Prompt": [result["prompt"]["uid"] for result in results],
            "Model": [result["model"]["uid"] for result in results],
            "F1": f1_values,
            "Support": support,
            "TP": tp_values,
            "FP": fp_values,
            "TN": tn_values,
            "FN": fn_values
        }

        table = pd.DataFrame(table_data)

        table.to_csv(TMP_SIMPLE_CLONE_RESULTS_CSV_EXP2)


class Experiment3Results:
    def __init__(self, selected_tools, input_processor=None, input_model=None, input_prompt=None):
        # Initial Data
        bugs, developer_patches, tool_patches = init(configure=False)

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
        self.selected_tools = selected_tools

        self._merge_results()

        self.results = self._get_results()

        self.pipe = pipeline(model="facebook/bart-large-mnli", device=0)

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
    
    def _classify_text(self, text: str) -> str:
        result = self.pipe(text, candidate_labels=self.labels)
        predicted_label = result["labels"][0]

        return predicted_label

    def classify(self, labels: list, selected_results=None):
        if not selected_results:
            selected_results = self.results

        for result in selected_results:
            self.labels = labels
            classified_result_dir = os.path.join(TMP_CLASSIFICATION_RESULTS_DIR, f"{'-'.join(labels)}-{result['file_name']}")
            result[f"classified_result_file_{'-'.join(labels)}"] = classified_result_dir

            if os.path.exists(classified_result_dir):
                logging.info(f"Skipping classification for {result['file_name']} as it already exists.")

                continue

            else:
                logging.info(f"Classifying Texts {classified_result_dir}")

                df = pd.read_pickle(result["result_file"])

                tqdm.pandas(desc=f"Classifying Texts {classified_result_dir}")

                df["predicted_label"] = df["response"].progress_apply(self._classify_text)

                df.to_pickle(classified_result_dir)

        logging.info("Classification Done.")

class Experiment3Evaluator:
    def __init__(self, results: Results):
        self.results = results

        # Simple Prompts Yes/No (translate yes no to overfitting and correct) Majority Voting NOT Applied
        simple_prompts = [
            "llm4cc-simple_prompt-semantical",
            "llm4cc-reasoning-patch-semantical",
            "llm4cc-similarity_line-patch-semantical",

            "llm4cc-simple_prompt-identical",
            "llm4cc-reasoning-patch-identical",
            "llm4cc-similarity_line-patch-identical",
        ]
        simple_results = [result for result in self.results.results if result["prompt"]["uid"] in simple_prompts]
        self._get_simple_results_table(simple_results, ["yes", "no"], pd.concat((results.input_developer_patches, results.input_tool_patches), axis=0))

        # Simple Prompts Type Binary (translate type to overfitting and correct) Majority Voting Applied, Inverted, Punished
        type_binary_prompts = [
            "llm4cc-clone_type",
            "llm4cc-integrated",
            "llm4cc-clone_type-patch",
            "llm4cc-integrated-patch",
        ]
        type_binary_results = [result for result in self.results.results if result["prompt"]["uid"] in type_binary_prompts]
        self._get_type_binary_results_table(type_binary_results, ["type-1", "type-2", "type-3", "type-4", "not-clone"], pd.concat((results.input_developer_patches, results.input_tool_patches), axis=0))

    def _translate_simple_label_to_binary(self, groundtruth_correctness, label): #
        if groundtruth_correctness == "Correct" and label == "no":
            return "Overfitting"

        elif groundtruth_correctness == "Correct" and label == "yes":
            return "Correct"

        elif groundtruth_correctness == "Overfitting" and label == "yes":
            return "Overfitting"

        return "Unknown"

    def _get_f1_simple(self, results, labels, ground_truth): #
        f1_values = []
        support = []
        tp_values = []
        fp_values = []
        tn_values = []
        fn_values = []

        for result in results:
            classified_result_dir = result[f"classified_result_file_{'-'.join(labels)}"]

            df = pd.read_pickle(classified_result_dir)

            ground_truth_clean = ground_truth[~ground_truth.index.duplicated(keep='first')]
            df["groundtruth_correctness"] = ground_truth_clean.loc[df.index]["correctness"].values

            df["predicted_correctness"] = df.apply(lambda x: self._translate_simple_label_to_binary(x["groundtruth_correctness"], x["predicted_label"]), axis=1)

            df["selected_correctness"] = ground_truth_clean.loc[df.index]["correctness"].values

            # Get Support and Drop Unknowns
            unknown_count = df["predicted_correctness"].value_counts().get("Unknown", 0)
            total_count = len(df)
            support.append(total_count - unknown_count)
            df = df[df["predicted_correctness"] != "Unknown"]

            # 1 corresponds to Correct
            df["predicted_correctness_binary"] = df["predicted_correctness"].apply(lambda x: 1 if x == "Correct" else 0)
            df["selected_correctness_binary"] = df["selected_correctness"].apply(lambda x: 1 if x == "Correct" else 0)


            # Group df by "tool_patch_uid" to get majority voting over oponions for 0 and 1 and assign that correctness to the grouped

            # Calculate TP, FP, TN, FN 
            tp = ((df["predicted_correctness_binary"] == 1) & (df["selected_correctness_binary"] == 1)).sum()
            fp = ((df["predicted_correctness_binary"] == 1) & (df["selected_correctness_binary"] == 0)).sum()
            tn = ((df["predicted_correctness_binary"] == 0) & (df["selected_correctness_binary"] == 0)).sum()
            fn = ((df["predicted_correctness_binary"] == 0) & (df["selected_correctness_binary"] == 1)).sum()
            
            tp_values.append(tp)
            fp_values.append(fp)
            tn_values.append(tn)
            fn_values.append(fn)

            f1 = f1_score(df["selected_correctness_binary"], df["predicted_correctness_binary"], zero_division=0)
            f1_values.append(f1)
            
        return f1_values, support, tp_values, fp_values, tn_values, fn_values

    def _get_simple_results_table(self, results, labels, ground_truth): #
        f1_values, support, tp_values, fp_values, tn_values, fn_values = self._get_f1_simple(results, labels, ground_truth)

        table_data = {
            "Processor": [result["processor"]["uid"] for result in results],
            "Prompt": [result["prompt"]["uid"] for result in results],
            "Model": [result["model"]["uid"] for result in results],
            "Tool": [result["tool"] for result in results],
            "F1": f1_values,
            "Support": support,
            "TP": tp_values,
            "FP": fp_values,
            "TN": tn_values,
            "FN": fn_values
        }

        table = pd.DataFrame(table_data)

        table.to_csv(TMP_SIMPLE_RESULTS_CSV_EXP3)
        

    def _predict_binary(self, row):
        if row["predicted_binary_label"] == "Unknown":
            if row["selected_correctness"] == "Correct":
                return 1
            
            else:
                return 0
            
        else:
            return 0 if row["predicted_binary_label"] == "Correct" else 1

    def _translate_type_label_to_binary(self, groundtruth_correctness, label): #
        if groundtruth_correctness == "Correct" and label == "not-clone":
            return "Overfitting" 

        elif groundtruth_correctness == "Correct" and label in ["type-1", "type-2", "type-4"]:
            return "Correct"

        elif groundtruth_correctness == "Overfitting" and label in ["type-1", "type-2", "type-4"]:
            return "Overfitting"

        return "Unknown"
    
    def _get_f1_type_binary(self, results, labels, ground_truth): #
        f1_values = []
        accuracy_values = []  # Added accuracy values list
        support = []
        total = []
        tp_values = []
        fp_values = []
        tn_values = []
        fn_values = []

        for result in results:
            classified_result_dir = result[f"classified_result_file_{'-'.join(labels)}"]
            df = pd.read_pickle(classified_result_dir)

            ground_truth_clean = ground_truth[~ground_truth.index.duplicated(keep="first")]

            # df["groundtruth_correctness"] = df["groundtruth_patch_uid"].map(ground_truth_clean["correctness"])
            df["groundtruth_correctness"] = ground_truth_clean.loc[df["groundtruth_patch_uid"]]["correctness"].values

            # Get Raw Translation
            df["raw_predicted_binary_label"] = df.apply(lambda x: self._translate_type_label_to_binary(x["groundtruth_correctness"], x["predicted_label"]), axis=1)

            # Use Raw to Get Majority Vote
            majority_labels = majority_vote_labels(df, label_column="raw_predicted_binary_label", id_column="tool_patch_uid")
            df_voted = df[["tool_patch_uid"]].drop_duplicates().copy()

            # df_voted["selected_correctness"] = df_voted["tool_patch_uid"].map(ground_truth_clean["correctness"])
            df_voted["selected_correctness"] = ground_truth_clean.loc[df_voted["tool_patch_uid"]]["correctness"].values

            df_voted["predicted_binary_label"] = df_voted["tool_patch_uid"].map(majority_labels)

            df = df_voted.copy()

            # Get Support and Drop Unknowns
            unknown_count = df["predicted_binary_label"].value_counts().get("Unknown", 0)
            total_count = len(df)
            support.append((total_count - unknown_count)/total_count)
            total.append(total_count)
            # df = df[df["predicted_binary_label"] != "Unknown"]

            df["predicted_correctness_binary"] = df.apply(self._predict_binary, axis=1)

            # df["predicted_correctness_binary"] = df["predicted_binary_label"].apply(lambda x: 0 if x == "Correct" else 1)
            df["selected_correctness_binary"] = df["selected_correctness"].apply(lambda x: 0 if x == "Correct" else 1)

            # Calculate TP, FP, TN, FN
            tp = ((df["predicted_correctness_binary"] == 1) & (df["selected_correctness_binary"] == 1)).sum()
            fp = ((df["predicted_correctness_binary"] == 1) & (df["selected_correctness_binary"] == 0)).sum()
            tn = ((df["predicted_correctness_binary"] == 0) & (df["selected_correctness_binary"] == 0)).sum()
            fn = ((df["predicted_correctness_binary"] == 0) & (df["selected_correctness_binary"] == 1)).sum()
            
            tp_values.append(tp)
            fp_values.append(fp)
            tn_values.append(tn)
            fn_values.append(fn)

            # Calculate accuracy
            accuracy = (tp + tn) / (tp + tn + fp + fn) if (tp + tn + fp + fn) > 0 else 0
            accuracy_values.append(accuracy)

            f1 = f1_score(df["selected_correctness_binary"], df["predicted_correctness_binary"], zero_division=0)
            f1_values.append(f1)
            
        return f1_values, accuracy_values, support, total, tp_values, fp_values, tn_values, fn_values

    def _get_type_binary_results_table(self, results, labels, ground_truth): #
        f1_values, accuracy_score, support, total, tp_values, fp_values, tn_values, fn_values = self._get_f1_type_binary(results, labels, ground_truth)

        table_data = {
            "Processor": [result["processor"]["uid"] for result in results],
            "Prompt": [result["prompt"]["uid"] for result in results],
            "Model": [result["model"]["uid"] for result in results],
            "Tool": [result["tool"] for result in results],
            "F1": f1_values,
            "Accuracy": accuracy_score,
            "Support": support,
            "total": total,
            "TP": tp_values,
            "FP": fp_values,
            "TN": tn_values,
            "FN": fn_values
        }

        table = pd.DataFrame(table_data)

        table.to_csv(TMP_TYPE_BINARY_RESULTS_CSV_EXP3)


def report_exp7():
    # Report the results of experiment 7
    logging.info("Reporting Experiment 7 Results ...")

    # Load result file from EXP7
    result_file = pd.read_pickle(os.path.join(TMP_RESULTS_DIR, "EXP7-sourcerercc.pkl"))

    # Load expert labels
    expert_labels = pd.read_pickle(TMP_EXPERT_CORRECT_LABEL_PKL)

    # Merge expert_label column into result_file based on index (uid)
    result_file = result_file.merge(
        expert_labels[['expert_label']],
        left_index=True,
        right_index=True,
        how='left'
    )
    result_file["groundtruth"] = True

    recalls = {}
    for label in result_file['expert_label'].dropna().unique():
        df_label = result_file[result_file['expert_label'] == label]
        true_positives = df_label[(df_label['clones'] == True) & (df_label['groundtruth'] == True)].shape[0]
        actual_positives = df_label[df_label['groundtruth'] == True].shape[0]
        recall = true_positives / actual_positives if actual_positives > 0 else 0.0
        recalls[label] = recall
        logging.info(f"Recall for expert_label '{label}': {recall:.2f}")
        logging.info(f"Recall for expert_label '{true_positives}': {recall:.2f}")

    return recalls

def count_identical_in_pairs(tool_patches=None, developer_patches=None):
    logging.info("Counting identical in pairs ...")
    if tool_patches is None:
        tool_patches = pd.read_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)

    if developer_patches is None:
        developer_patches = pd.read_pickle(TMP_GENERATOR_NORMALIZED_DEVELOPER_PATHCES_PKL)

    tool_patches = tool_patches[tool_patches["bug_uid"].str.contains("defects4j")].copy()
    all_tool_patches = tool_patches.copy()

    tool_patches = tool_patches[tool_patches["correctness"] == "Correct"].copy()
    # tool_patches = tool_patches[tool_patches["correctness"] == "Overfitting"].copy()

    tool_patches['content'] = tool_patches['location'].apply(read_patch)

    # get bugs with fixes
    unique_bugs = tool_patches["bug_uid"].unique()
    logging.info(f"Unique bugs: {len(unique_bugs)}")

    # Make the bugs a dataframe
    unique_bugs = pd.DataFrame(unique_bugs, columns=["bug_uid"])

    unique_bugs["total_patches"] = unique_bugs["bug_uid"].apply(lambda x: len(tool_patches[tool_patches["bug_uid"] == x]))

    logging.info(unique_bugs)

    # Create a copy of tool_patches to work with
    tool_patches_copy = tool_patches.copy()
    logging.info(f"Current Representatives: {tool_patches_copy}")

    # Remove developer identicals
    logging.info("Removing developer identicals ...")
    logging.info(f"Developer patches: {len(developer_patches)}")
    logging.info(f"Current Representatives: {tool_patches_copy}")
    developer_patches["content"] = developer_patches["location"].apply(read_patch)
    
    # Track patches dropped due to being identical to developer patches
    tool_patches_copy["is_identical_to_dev"] = tool_patches_copy.apply(lambda row: row["content"] == developer_patches[developer_patches["bug_uid"] == row["bug_uid"]]["content"].values[0], axis=1)
    dropped_dev_identical = tool_patches_copy[tool_patches_copy["is_identical_to_dev"]].copy()
    tool_patches_copy = tool_patches_copy[~tool_patches_copy["is_identical_to_dev"]]
    
    all_tool_patches["content"] = all_tool_patches["location"].apply(read_patch)
    all_tool_patches["is_identical_to_dev"] = all_tool_patches.apply(lambda row: row["content"] == developer_patches[developer_patches["bug_uid"] == row["bug_uid"]]["content"].values[0], axis=1)
    all_tool_patches = all_tool_patches[~all_tool_patches["is_identical_to_dev"]]
    all_tool_patches_correct = all_tool_patches[all_tool_patches["correctness"] == "Correct"]
    all_tool_patches_overfitting = all_tool_patches[all_tool_patches["correctness"] == "Overfitting"]
    
    logging.info(f"Deduplicated patches with identical to dev: {len(all_tool_patches)}")
    logging.info(f"Deduplicated patches with identical to dev and correct ->: {len(all_tool_patches_correct)}")
    logging.info(f"Deduplicated patches with identical to dev and overfitting ->: {len(all_tool_patches_overfitting)}")
    
    tool_patches_copy.drop(columns=["is_identical_to_dev"], inplace=True)
    logging.info(f"After Representatives: {tool_patches_copy}")
    logging.info("Finished Removing developer identicals ...")

    
    # Dictionary to store identical patch groups for each bug
    deduplicated_patches = []
    dropped_duplicate_patches = []  # Track patches dropped during deduplication
    
    # Process each bug separately
    for bug_uid in unique_bugs["bug_uid"]:
        bug_patches = tool_patches_copy[tool_patches_copy["bug_uid"] == bug_uid].copy()
        
        if len(bug_patches) == 0:
            continue
            
        # Create a list to track which patches have been processed
        processed_patches = set()
        
        # For each patch in this bug
        for idx, patch in bug_patches.iterrows():
            if idx in processed_patches:
                continue
                
            # Find all patches identical to this one within the same bug
            identical_patches = [idx]  # Start with the current patch
            
            for other_idx, other_patch in bug_patches.iterrows():
                if other_idx != idx and other_idx not in processed_patches:
                    # Check if patches are identical
                    # if are_codes_identical(patch["content"], other_patch["content"]):
                    if patch["content"] == other_patch["content"]:
                        identical_patches.append(other_idx)
            
            # Mark all these patches as processed
            processed_patches.update(identical_patches)
            
            # Keep the first patch as representative and add count
            representative_patch = bug_patches.loc[idx].copy()
            representative_patch["number_of_identical"] = len(identical_patches)
            
            deduplicated_patches.append(representative_patch)
            
            # Add the duplicates (excluding the representative) to dropped patches
            if len(identical_patches) > 1:
                for duplicate_idx in identical_patches[1:]:  # Skip the first one (representative)
                    dropped_patch = bug_patches.loc[duplicate_idx].copy()
                    dropped_patch["representative_idx"] = idx  # Link to the representative patch
                    dropped_patch["reason_dropped"] = "duplicate_content"
                    dropped_duplicate_patches.append(dropped_patch)
    
    # Convert back to DataFrame while preserving original indices
    if deduplicated_patches:
        deduplicated_tool_patches = pd.DataFrame(deduplicated_patches)
        # Set the index to match the original indices
        original_indices = [patch.name for patch in deduplicated_patches]
        deduplicated_tool_patches.index = original_indices
    else:
        deduplicated_tool_patches = pd.DataFrame()
    
    if dropped_duplicate_patches:
        dropped_duplicates_df = pd.DataFrame(dropped_duplicate_patches)
        # Set the index to match the original indices
        original_indices = [patch.name for patch in dropped_duplicate_patches]
        dropped_duplicates_df.index = original_indices
    else:
        dropped_duplicates_df = pd.DataFrame()
    
    # Combine all dropped patches
    # Add reason for developer identical patches
    if len(dropped_dev_identical) > 0:
        dropped_dev_identical["reason_dropped"] = "identical_to_developer"
        dropped_dev_identical["representative_idx"] = None  # No representative for dev identical
    
    # Combine all dropped patches into one dataframe while preserving indices
    all_dropped_patches = []
    if len(dropped_dev_identical) > 0:
        all_dropped_patches.append(dropped_dev_identical.drop(columns=["is_identical_to_dev"], errors='ignore'))
    if len(dropped_duplicates_df) > 0:
        all_dropped_patches.append(dropped_duplicates_df)
    
    if all_dropped_patches:
        all_dropped_df = pd.concat(all_dropped_patches, ignore_index=False)  # Keep original indices
    else:
        all_dropped_df = pd.DataFrame()
    
    logging.info(f"Original patches: {len(tool_patches)}")
    logging.info(f"Deduplicated by dev identical patches: {len(tool_patches_copy)}")
    logging.info(f"Deduplicated by representatives patches ->: {len(deduplicated_tool_patches)}")
    logging.info(f"Total dropped patches: {len(all_dropped_df)}")
    logging.info(f"  - Dropped due to dev identical: {len(dropped_dev_identical) if len(dropped_dev_identical) > 0 else 0}")
    logging.info(f"  - Dropped due to duplicates: {len(dropped_duplicates_df)}")
    
    # Show summary by bug
    summary = deduplicated_tool_patches.groupby("bug_uid").agg({
        "number_of_identical": ["count", "sum"]
    }).round(2)
    summary.columns = ["unique_patch_groups", "total_original_patches"]
    
    logging.info("Summary by bug:")
    logging.info(summary)
    
    # Show examples of patches with multiple identical copies
    examples = deduplicated_tool_patches[deduplicated_tool_patches["number_of_identical"] > 1] # 42 # 27
    # examples = deduplicated_tool_patches[deduplicated_tool_patches["number_of_identical"] > 0] # 100
    if len(examples) > 0:
        logging.info(f"\nBugs with identical patches:")
        logging.info("Excluded all patches that have no identical and then got bugs of them.")
        logging.info(len(examples["bug_uid"].unique()))
        logging.info(examples[["bug_uid", "number_of_identical"]])
    
    df = pd.DataFrame(deduplicated_patches)
    df['number_of_identical'] = df['number_of_identical'].astype(int)
    total_identical = df['number_of_identical'].sum()
    logging.info(f"sum of all identical feature should be equal to all correct patches, after removing dev identical? (depends not this) in ignore whitesapce: {total_identical}")

    return deduplicated_tool_patches, all_dropped_df

def create_dot_plot_identical_patches_number_color(deduplicated_patches):
    """
    Create a dot plot where each bug is on x-axis and dots represent groups of identical patches
    """
    # Group by bug_uid and collect the number_of_identical values
    bug_groups = defaultdict(list)
    
    for _, row in deduplicated_patches.iterrows():
        bug_uid = row['bug_uid']
        num_identical = row['number_of_identical']
        bug_groups[bug_uid].append(num_identical)
    
    # Sort bugs by total number of patches (sum of identical counts)
    bug_totals = [(bug, sum(counts)) for bug, counts in bug_groups.items()]
    bug_totals.sort(key=lambda x: x[1], reverse=True)
    
    # Filter out bugs with only 1 patch
    # bug_totals_filtered = [(bug, total) for bug, total in bug_totals if True]
    bug_totals_filtered = [(bug, total) for bug, total in bug_totals if total > 1]
    
    # Log statistics before filtering
    logging.info(f"Original bugs before filtering: {len(bug_totals)}")
    logging.info(f"Bugs after removing single-patch bugs: {len(bug_totals_filtered)}")
    logging.info(f"Bugs removed (single patch): {len(bug_totals) - len(bug_totals_filtered)}")
    
    # Create the plot
    fig, ax = plt.subplots(figsize=(20, 8))
    
    # Generate colors for different group sizes (only for filtered data)
    unique_group_sizes = set()
    for bug, _ in bug_totals_filtered:
        counts = bug_groups[bug]
        unique_group_sizes.update(counts)
    
    colors = plt.cm.Set3(np.linspace(0, 1, len(unique_group_sizes)))
    color_map = dict(zip(sorted(unique_group_sizes), colors))
    
    x_positions = []
    bug_labels = []
    total_dots = 0
    
    for i, (bug_uid, total_patches) in enumerate(bug_totals_filtered):
        bug_labels.append(bug_uid.replace('defects4j-', '').replace('bugsjar-', ''))
        x_positions.append(i)
        
        # Get the groups for this bug
        groups = bug_groups[bug_uid]
        
        # Stack dots vertically for each group
        y_offset = 0
        
        for group_size in groups:
            # Create dots for this group
            for dot in range(group_size):
                ax.scatter(i, y_offset + dot, 
                          c=[color_map[group_size]], 
                          s=50, 
                          alpha=0.8,
                          edgecolors='black',
                          linewidth=0.5)
                total_dots += 1
            y_offset += group_size
    
    # Log plot dimensions and dot count
    logging.info(f"Total dots plotted: {total_dots}")
    logging.info(f"X-axis length (number of bugs): {len(bug_totals_filtered)}")
    logging.info(f"Y-axis max height: {max([sum(bug_groups[bug]) for bug, _ in bug_totals_filtered]) if bug_totals_filtered else 0}")
    
    # Customize the plot
    ax.set_xlabel('Bug ID', fontsize=12, fontweight='bold')
    ax.set_ylabel('Number of Patches', fontsize=12, fontweight='bold')
    ax.set_title('Distribution of Identical Patches per Bug', fontsize=14, fontweight='bold')
    
    # logging.info("\n(Each color represents a group of identical patches)\n(Each dot represents a correct patch)\n(490 pairs are exact match out of 3944)")
    # Set x-axis
    ax.set_xticks(x_positions)
    ax.set_xticklabels(bug_labels, rotation=45, ha='right', fontsize=8)
    
    # Add grid
    ax.grid(True, alpha=0.3, axis='y')
    
    # Create legend
    legend_elements = []
    for group_size in sorted(unique_group_sizes):
        legend_elements.append(plt.scatter([], [], c=[color_map[group_size]], 
                                         s=50, alpha=0.8, edgecolors='black', linewidth=0.5,
                                         label=f'{group_size} identical patches'))
    
    ax.legend(handles=legend_elements, 
             title='Group Sizes', 
             bbox_to_anchor=(1.05, 1), 
             loc='upper left',
             fontsize=9)
    
    plt.tight_layout()
    plt.savefig(os.path.join(TMP_PLOTS_DIR, 'create_dot_plot_identical_patches.png'), dpi=300, bbox_inches='tight')
    
    # Log summary statistics
    logging.info(f"\nSummary Statistics:")
    logging.info(f"Total bugs displayed: {len(bug_totals_filtered)}")
    logging.info(f"Total unique patch groups displayed: {sum(len(bug_groups[bug]) for bug, _ in bug_totals_filtered)}")
    logging.info(f"Total original patches displayed: {sum(total for _, total in bug_totals_filtered)}")
    
    # Show top bugs with most patches
    logging.info(f"\nTop bugs with most patches:")
    for i, (bug_uid, total) in enumerate(bug_totals_filtered):
        groups = bug_groups[bug_uid]
        logging.info(f"{i+1:2d}. {bug_uid}: {total} patches in {len(groups)} groups {groups}")

def create_dot_plot_identical_patches(deduplicated_patches):
    """
    Create a dot plot where each bug is on x-axis and dots represent groups of identical patches
    Each group of identical patches gets the same color, different groups get different colors
    """
    # Group by bug_uid and collect the number_of_identical values
    bug_groups = defaultdict(list)
    
    for _, row in deduplicated_patches.iterrows():
        bug_uid = row['bug_uid']
        num_identical = row['number_of_identical']
        bug_groups[bug_uid].append(num_identical)
    
    # Sort bugs by total number of patches (sum of identical counts)
    bug_totals = [(bug, sum(counts)) for bug, counts in bug_groups.items()]
    bug_totals.sort(key=lambda x: x[1], reverse=True)
    
    # Filter out bugs with only 1 patch
    bug_totals_filtered = [(bug, total) for bug, total in bug_totals if total > 1]
    
    # Log statistics before filtering
    logging.info(f"Original bugs before filtering: {len(bug_totals)}")
    logging.info(f"Bugs after removing single-patch bugs: {len(bug_totals_filtered)}")
    logging.info(f"Bugs removed (single patch): {len(bug_totals) - len(bug_totals_filtered)}")
    
    # Create the plot
    fig, ax = plt.subplots(figsize=(20, 8))
    
    # Generate a large pool of distinct colors
    num_colors_needed = sum(len(bug_groups[bug]) for bug, _ in bug_totals_filtered)
    colors = plt.cm.tab20(np.linspace(0, 1, 20))  # 20 distinct colors
    if num_colors_needed > 20:
        # If we need more colors, combine multiple colormaps
        colors1 = plt.cm.tab20(np.linspace(0, 1, 20))
        colors2 = plt.cm.Set3(np.linspace(0, 1, 12))
        colors3 = plt.cm.Pastel1(np.linspace(0, 1, 9))
        colors = np.vstack([colors1, colors2, colors3])
    
    x_positions = []
    bug_labels = []
    total_dots = 0
    color_index = 0
    
    # Keep track of colors used for legend
    color_usage = {}  # group_size -> color
    
    for i, (bug_uid, total_patches) in enumerate(bug_totals_filtered):
        bug_labels.append(bug_uid.replace('defects4j-', '').replace('bugsjar-', ''))
        x_positions.append(i)
        
        # Get the groups for this bug
        groups = bug_groups[bug_uid]
        
        # Stack dots vertically for each group
        y_offset = 0
        
        for group_size in groups:
            # Assign a unique color to this group
            current_color = colors[color_index % len(colors)]
            
            # Track this color for the legend (group size -> color mapping)
            if group_size not in color_usage:
                color_usage[group_size] = current_color
            
            # Create dots for this group - all dots in this group get the same color
            for dot in range(group_size):
                ax.scatter(i, y_offset + dot, 
                          c=[current_color], 
                          s=50, 
                          alpha=0.8,
                          edgecolors='black',
                          linewidth=0.5)
                total_dots += 1
            
            y_offset += group_size
            color_index += 1  # Move to next color for next group
    
    # Log plot dimensions and dot count
    logging.info(f"Total dots plotted: {total_dots}")
    logging.info(f"X-axis length (number of bugs): {len(bug_totals_filtered)}")
    logging.info(f"Y-axis max height: {max([sum(bug_groups[bug]) for bug, _ in bug_totals_filtered]) if bug_totals_filtered else 0}")
    
    # Customize the plot
    ax.set_xlabel('Bug ID', fontsize=12, fontweight='bold')
    ax.set_ylabel('Number of Patches', fontsize=12, fontweight='bold')
    ax.set_title('Distribution of Identical Patches per Bug\n(Each color represents one group of identical patches)', 
                 fontsize=14, fontweight='bold')
    
    # Set x-axis
    ax.set_xticks(x_positions)
    ax.set_xticklabels(bug_labels, rotation=45, ha='right', fontsize=8)
    
    # Add grid
    ax.grid(True, alpha=0.3, axis='y')
    
    # Create legend showing group sizes
    legend_elements = []
    for group_size in sorted(color_usage.keys()):
        legend_elements.append(plt.scatter([], [], c=[color_usage[group_size]], 
                                         s=50, alpha=0.8, edgecolors='black', linewidth=0.5,
                                         label=f'Groups of {group_size} identical patches'))
    
    ax.legend(handles=legend_elements, 
             title='Group Sizes', 
             bbox_to_anchor=(1.05, 1), 
             loc='upper left',
             fontsize=9)
    
    plt.tight_layout()
    plt.savefig(os.path.join(TMP_PLOTS_DIR, 'create_dot_plot_identical_patches.png'), dpi=300, bbox_inches='tight')
    
    # Log summary statistics
    logging.info(f"\nSummary Statistics:")
    logging.info(f"Total bugs displayed: {len(bug_totals_filtered)}")
    logging.info(f"Total unique patch groups displayed: {sum(len(bug_groups[bug]) for bug, _ in bug_totals_filtered)}")
    logging.info(f"Total original patches displayed: {sum(total for _, total in bug_totals_filtered)}")
    logging.info(f"Total colors used: {color_index}")
    
    # Show top bugs with most patches
    logging.info(f"\nTop bugs with most patches:")
    for i, (bug_uid, total) in enumerate(bug_totals_filtered):
        groups = bug_groups[bug_uid]
        logging.info(f"{i+1:2d}. {bug_uid}: {total} patches in {len(groups)} groups {groups}")

def create_dot_plot_identical_patches_v2(deduplicated_patches):
    """
    Enhanced version with better color management and visual separation
    """
    # Group by bug_uid and collect the number_of_identical values
    bug_groups = defaultdict(list)
    
    for _, row in deduplicated_patches.iterrows():
        bug_uid = row['bug_uid']
        num_identical = row['number_of_identical']
        bug_groups[bug_uid].append(num_identical)
    
    # Sort bugs by total number of patches (sum of identical counts)
    bug_totals = [(bug, sum(counts)) for bug, counts in bug_groups.items()]
    bug_totals.sort(key=lambda x: x[1], reverse=True)
    
    # Filter out bugs with only 1 patch
    bug_totals_filtered = [(bug, total) for bug, total in bug_totals if total > 1]
    
    # Log statistics
    logging.info(f"Original bugs before filtering: {len(bug_totals)}")
    logging.info(f"Bugs after removing single-patch bugs: {len(bug_totals_filtered)}")
    
    # Create the plot
    fig, ax = plt.subplots(figsize=(20, 8))
    
    # Use a color palette that provides good contrast
    colors = plt.cm.tab20(np.linspace(0, 1, 20))
    additional_colors = plt.cm.Set1(np.linspace(0, 1, 9))
    more_colors = plt.cm.Dark2(np.linspace(0, 1, 8))
    all_colors = np.vstack([colors, additional_colors, more_colors])
    
    x_positions = []
    bug_labels = []
    total_dots = 0
    color_index = 0
    
    # Statistics for different group sizes
    group_size_stats = defaultdict(int)
    
    for i, (bug_uid, total_patches) in enumerate(bug_totals_filtered):
        bug_labels.append(bug_uid.replace('defects4j-', '').replace('bugsjar-', ''))
        x_positions.append(i)
        
        # Get the groups for this bug
        groups = bug_groups[bug_uid]
        
        # Stack dots vertically for each group
        y_offset = 0
        
        for group_size in groups:
            # Get color for this group
            current_color = all_colors[color_index % len(all_colors)]
            group_size_stats[group_size] += 1
            
            # Create dots for this group with slight vertical spacing between groups
            for dot in range(group_size):
                ax.scatter(i, y_offset + dot + 0.1 * (len([g for g in groups[:groups.index(group_size)]])), 
                          c=[current_color], 
                          s=60, 
                          alpha=0.9,
                          edgecolors='white',
                          linewidth=0.8)
                total_dots += 1
            
            y_offset += group_size
            color_index += 1
    
    # Customize the plot
    ax.set_xlabel('Bug ID', fontsize=12, fontweight='bold')
    ax.set_ylabel('Number of Patches', fontsize=12, fontweight='bold')
    ax.set_title('Distribution of Identical Patches per Bug\n(Each color = one group of identical patches)', 
                 fontsize=14, fontweight='bold')
    
    # Set x-axis
    ax.set_xticks(x_positions)
    ax.set_xticklabels(bug_labels, rotation=45, ha='right', fontsize=8)
    
    # Add grid
    ax.grid(True, alpha=0.3, axis='y')
    
    # Create informative legend
    legend_text = []
    for size, count in sorted(group_size_stats.items()):
        legend_text.append(f"Size {size}: {count} groups")
    
    ax.text(1.02, 0.5, "\n".join(legend_text), 
            transform=ax.transAxes, fontsize=9, 
            verticalalignment='center',
            bbox=dict(boxstyle="round,pad=0.3", facecolor="lightgray", alpha=0.7))
    
    plt.tight_layout()
    plt.savefig(os.path.join(TMP_PLOTS_DIR, 'create_dot_plot_identical_patches_v2.png'), dpi=300, bbox_inches='tight')
    
    # Log summary
    logging.info(f"Total dots plotted: {total_dots}")
    logging.info(f"Group size distribution: {dict(group_size_stats)}")
    logging.info(f"Total unique groups: {sum(group_size_stats.values())}")

def create_dot_plot_identical_patches_v3(deduplicated_patches):
    """
    Enhanced version with groups sorted by size (smallest groups at left, increasing to right)
    """
    # Group by bug_uid and collect the number_of_identical values
    bug_groups = defaultdict(list)
    
    for _, row in deduplicated_patches.iterrows():
        bug_uid = row['bug_uid']
        num_identical = row['number_of_identical']
        bug_groups[bug_uid].append(num_identical)
    
    # Sort groups within each bug by size (largest first - will be at bottom)
    for bug_uid in bug_groups:
        bug_groups[bug_uid].sort(reverse=True)
    
    # Sort bugs by total number of patches (sum of identical counts) - ASCENDING ORDER
    bug_totals = [(bug, sum(counts)) for bug, counts in bug_groups.items()]
    bug_totals.sort(key=lambda x: x[1])  # Changed: removed reverse=True for increasing order
    
    # Filter out bugs with only 1 patch
    bug_totals_filtered = [(bug, total) for bug, total in bug_totals if total > 0]
    # bug_totals_filtered = [(bug, total) for bug, total in bug_totals if total > 1]
    
    # Log statistics
    logging.info(f"Original bugs before filtering: {len(bug_totals)}")
    logging.info(f"Bugs after removing single-patch bugs: {len(bug_totals_filtered)}")
    
    # Create the plot with tighter width
    fig, ax = plt.subplots(figsize=(16, 8))  # Reduced width from 20 to 16
    
    # Define a consistent color sequence for each level
    level_colors = [
        '#1f77b4',  # Blue (level 0 - bottom/largest groups)
        '#ff7f0e',  # Orange (level 1)
        '#2ca02c',  # Green (level 2)
        '#d62728',  # Red (level 3)
        '#9467bd',  # Purple (level 4)
        '#8c564b',  # Brown (level 5)
        '#e377c2',  # Pink (level 6)
        '#7f7f7f',  # Gray (level 7)
        '#bcbd22',  # Olive (level 8)
        '#17becf',  # Cyan (level 9)
    ]
    
    # Extend with more colors if needed
    while len(level_colors) < 20:
        level_colors.extend(level_colors[:10])  # Repeat pattern if needed
    
    x_positions = []
    bug_labels = []
    total_dots = 0
    
    # Statistics for different group sizes
    group_size_stats = defaultdict(int)
    
    for i, (bug_uid, total_patches) in enumerate(bug_totals_filtered):
        bug_labels.append(bug_uid.replace('defects4j-', '').replace('bugsjar-', ''))
        x_positions.append(i)
        
        # Get the groups for this bug (already sorted largest to smallest)
        groups = bug_groups[bug_uid]
        
        # Stack dots vertically for each group, starting from bottom
        y_offset = 0
        
        for group_idx, group_size in enumerate(groups):
            # Get color for this level (same level = same color across all bugs)
            current_color = level_colors[group_idx % len(level_colors)]
            group_size_stats[group_size] += 1
            
            # Create dots for this group with slight vertical spacing between groups
            for dot in range(group_size):
                ax.scatter(i, y_offset + dot + 0.1 * group_idx, 
                          c=current_color, 
                          s=60, 
                          alpha=0.9,
                          edgecolors='white',
                          linewidth=0.8)
                total_dots += 1
            
            # Move to next group position (larger groups are at bottom due to sorting)
            y_offset += group_size + 0.7  # Small gap between groups
    
    # Customize the plot
    ax.set_xlabel('Bug ID', fontsize=12, fontweight='bold')
    
    # Remove default y-axis label and ticks
    ax.set_ylabel('')  # Remove default y-axis label
    
    # Move y-axis tick labels inside the plot
    ax.tick_params(axis='y', direction='in', pad=-15)  # Move tick labels inside
    
    # Place y-axis label inside the plot area - CHANGED: rotation=90 for vertical text, centered vertically
    ax.text(0.02, 0.5, 'Number of Patches', transform=ax.transAxes, 
            rotation=90, verticalalignment='center', horizontalalignment='left',
            fontsize=12, fontweight='bold')
    
    # Set x-axis with improved readability
    ax.set_xticks(x_positions)
    
    # Option 1: Larger font with vertical rotation for better readability
    ax.set_xticklabels(bug_labels, rotation=90, ha='center', fontsize=12)
    
    # Option 2: Alternative - show every nth label to reduce crowding
    # if len(bug_labels) > 50:  # Only thin out if too many labels
    #     step = max(1, len(bug_labels) // 30)  # Show ~30 labels max
    #     thinned_positions = x_positions[::step]
    #     thinned_labels = bug_labels[::step]
    #     ax.set_xticks(thinned_positions)
    #     ax.set_xticklabels(thinned_labels, rotation=90, ha='center', fontsize=10)
    
    ax.set_xlim(-0.5, len(x_positions) - 0.5)  # Remove whitespace on sides
    
    # Set y-axis to start from 0 with no extra space
    ax.set_ylim(bottom=-0.5)
    
    # Add grid
    # ax.grid(True, alpha=0.3, axis='y')
    
    # Remove top and right spines
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
    # Create informative legend showing both level colors and group size distribution
    legend_text = ["Cluster Size:"]
    for size, count in sorted(group_size_stats.items(), reverse=True):
        legend_text.append(f"Size {size}: {count} groups")
    
    # Add color level legend
    color_legend_text = "\nLevel Colors:\n"
    color_levels = ["Bottom (Blue)", "2nd (Orange)", "3rd (Green)", "4th (Red)", "5th (Purple)"]
    for i, level_name in enumerate(color_levels):
        if i < len(level_colors):
            color_legend_text += f"Level {i+1}: {level_name}\n"
    
    # Updated legend with transparent background and no border
    # ax.text(0.98, 0.97, "\n".join(legend_text), 
    #         transform=ax.transAxes, fontsize=12, 
    #         verticalalignment='top', horizontalalignment='right',  # Right-aligned at top-right
    #         bbox=dict(boxstyle="round,pad=0.3", facecolor="none", edgecolor="none", alpha=0))
    
    plt.tight_layout()
    plt.savefig(os.path.join(TMP_PLOTS_DIR, 'create_dot_plot_identical_patches_v3.png'), dpi=300, bbox_inches='tight')
    
    # Log summary with examples
    logging.info(f"Total dots plotted: {total_dots}")
    logging.info(f"Group size distribution: {dict(group_size_stats)}")
    logging.info(f"Total unique groups: {sum(group_size_stats.values())}")
    
    # Show examples of how groups are stacked for first few bugs
    logging.info("\nExample stacking for first 5 bugs:")
    for i, (bug_uid, total_patches) in enumerate(bug_totals_filtered[:5]):
        groups = bug_groups[bug_uid]
        logging.info(f"  {bug_uid}: {groups} (bottom to top: {' -> '.join(map(str, groups))})")

def create_groups_distribution_plot(deduplicated_patches):
    """
    Create a plot where:
    - X-axis: Number of groups (distinct identical patch groups per bug)
    - Y-axis: Number of bugs that have that many groups
    """
    # Group by bug_uid and count the number of groups per bug
    bug_groups = defaultdict(list)
    
    for _, row in deduplicated_patches.iterrows():
        bug_uid = row['bug_uid']
        num_identical = row['number_of_identical']
        bug_groups[bug_uid].append(num_identical)
    
    # Count the number of groups per bug
    groups_per_bug = {}
    for bug_uid, groups in bug_groups.items():
        groups_per_bug[bug_uid] = len(groups)  # Number of distinct groups
    
    # Count how many bugs have each number of groups
    groups_count_distribution = Counter(groups_per_bug.values())
    
    # Prepare data for plotting
    x_values = sorted(groups_count_distribution.keys())
    y_values = [groups_count_distribution[x] for x in x_values]
    
    # Create the plot
    fig, ax = plt.subplots(figsize=(12, 8))
    
    # Create bar plot
    bars = ax.bar(x_values, y_values, 
                  color='steelblue', 
                  alpha=0.7, 
                  edgecolor='navy', 
                  linewidth=1.2)
    
    # Add value labels on top of bars
    for bar, count in zip(bars, y_values):
        height = bar.get_height()
        ax.text(bar.get_x() + bar.get_width()/2., height + 0.1,
                f'{count}', ha='center', va='bottom', fontweight='bold')
    
    # Customize the plot
    ax.set_xlabel('Number of Groups per Bug', fontsize=12, fontweight='bold')
    ax.set_ylabel('Number of Bugs', fontsize=12, fontweight='bold')
    ax.set_title('Distribution of Number of Groups per Bug\n(How many bugs have X number of distinct identical patch groups)', 
                 fontsize=14, fontweight='bold')
    
    # Set integer ticks on x-axis
    ax.set_xticks(x_values)
    ax.set_xticklabels([str(x) for x in x_values])
    
    # Add grid for better readability
    ax.grid(True, alpha=0.3, axis='y')
    ax.set_axisbelow(True)
    
    # Add statistics text box
    total_bugs = len(groups_per_bug)
    avg_groups = np.mean(list(groups_per_bug.values()))
    max_groups = max(groups_per_bug.values())
    min_groups = min(groups_per_bug.values())
    
    stats_text = f"""Statistics:
        Total Bugs: {total_bugs}
        Average Groups per Bug: {avg_groups:.2f}
        Max Groups per Bug: {max_groups}
        Min Groups per Bug: {min_groups}
        Most Common: {max(groups_count_distribution, key=groups_count_distribution.get)} groups
    """
    
    ax.text(0.02, 0.98, stats_text, 
            transform=ax.transAxes, 
            fontsize=10, 
            verticalalignment='top',
            bbox=dict(boxstyle="round,pad=0.5", facecolor="lightgray", alpha=0.8))
    
    plt.tight_layout()
    
    # Save if TMP_PLOTS_DIR is defined
    plt.savefig(os.path.join(TMP_PLOTS_DIR, 'groups_distribution_plot.png'), 
                dpi=300, bbox_inches='tight')
    
    # Log detailed statistics
    logging.info(f"Groups distribution analysis:")
    logging.info(f"Total bugs analyzed: {total_bugs}")
    logging.info(f"Distribution of number of groups per bug:")
    
    for num_groups in sorted(groups_count_distribution.keys()):
        count = groups_count_distribution[num_groups]
        percentage = (count / total_bugs) * 100
        logging.info(f"  {num_groups} groups: {count} bugs ({percentage:.1f}%)")
    
    # Show examples of bugs with different numbers of groups
    logging.info(f"\nExamples of bugs by number of groups:")
    
    for num_groups in sorted(set(groups_per_bug.values())):
        example_bugs = [bug for bug, count in groups_per_bug.items() if count == num_groups]
        if example_bugs:
            # Show first 3 examples
            examples = example_bugs[:3]
            example_details = []
            for bug in examples:
                groups = bug_groups[bug]
                total_patches = sum(groups)
                example_details.append(f"{bug} (groups: {groups}, total: {total_patches})")
            
            logging.info(f"  {num_groups} groups ({len(example_bugs)} bugs): {'; '.join(example_details)}")
    
    return groups_count_distribution, groups_per_bug

def create_detailed_groups_analysis(deduplicated_patches):
    """
    Create a more detailed analysis including group sizes
    """
    # Group by bug_uid and collect detailed information
    bug_analysis = {}
    
    for _, row in deduplicated_patches.iterrows():
        bug_uid = row['bug_uid']
        num_identical = row['number_of_identical']
        
        if bug_uid not in bug_analysis:
            bug_analysis[bug_uid] = {
                'groups': [],
                'total_patches': 0,
                'num_groups': 0
            }
        
        bug_analysis[bug_uid]['groups'].append(num_identical)
        bug_analysis[bug_uid]['total_patches'] += num_identical
        bug_analysis[bug_uid]['num_groups'] += 1
    
    # Create subplots for multiple analyses with better styling
    fig, ((ax1, ax2), (ax3, ax4)) = plt.subplots(2, 2, figsize=(16, 12))
    fig.suptitle('Comprehensive Bug Group Analysis Dashboard', fontsize=16, fontweight='bold', y=0.98)
    
    # Define color scheme
    colors = {
        'primary': '#2E86AB',
        'secondary': '#A23B72', 
        'accent1': '#F18F01',
        'accent2': '#C73E1D',
        'light_blue': '#E3F2FD',
        'light_orange': '#FFF3E0',
        'light_green': '#E8F5E8',
        'light_red': '#FFEBEE'
    }
    
    # Plot 1: Number of groups distribution (main request)
    groups_per_bug = {bug: info['num_groups'] for bug, info in bug_analysis.items()}
    groups_count_distribution = Counter(groups_per_bug.values())
    
    x_values = sorted(groups_count_distribution.keys())
    y_values = [groups_count_distribution[x] for x in x_values]
    
    bars1 = ax1.bar(x_values, y_values, color=colors['primary'], alpha=0.8, 
                    edgecolor='white', linewidth=1.5)
    
    # Add value labels on bars
    for bar, count in zip(bars1, y_values):
        height = bar.get_height()
        ax1.text(bar.get_x() + bar.get_width()/2., height + max(y_values) * 0.01,
                f'{count}', ha='center', va='bottom', fontweight='bold', fontsize=10)
    
    ax1.set_xlabel('Number of Groups per Bug', fontsize=12, fontweight='bold')
    ax1.set_ylabel('Number of Bugs', fontsize=12, fontweight='bold')
    ax1.set_title('Distribution of Groups per Bug', fontsize=13, fontweight='bold', pad=20)
    ax1.grid(True, alpha=0.3, axis='y', linestyle='--')
    ax1.set_facecolor(colors['light_blue'])
    ax1.set_xlim(min(x_values) - 0.5, max(x_values) + 0.5)
    ax1.set_ylim(0, max(y_values) * 1.15)
    
    # Add stats box
    total_bugs_1 = sum(y_values)
    avg_groups = sum(x * y for x, y in zip(x_values, y_values)) / total_bugs_1
    stats_text_1 = f"Total: {total_bugs_1}\nAvg: {avg_groups:.1f}"
    ax1.text(0.98, 0.98, stats_text_1, transform=ax1.transAxes, fontsize=9,
            verticalalignment='top', horizontalalignment='right',
            bbox=dict(boxstyle="round,pad=0.3", facecolor="white", alpha=0.8, edgecolor=colors['primary']))
    
    # Plot 2: Total patches distribution
    total_patches_per_bug = {bug: info['total_patches'] for bug, info in bug_analysis.items()}
    patches_distribution = Counter(total_patches_per_bug.values())
    
    x_vals2 = sorted(patches_distribution.keys())
    y_vals2 = [patches_distribution[x] for x in x_vals2]
    
    bars2 = ax2.bar(x_vals2, y_vals2, color=colors['accent1'], alpha=0.8, 
                    edgecolor='white', linewidth=1.5)
    
    # Add value labels on bars
    for bar, count in zip(bars2, y_vals2):
        height = bar.get_height()
        ax2.text(bar.get_x() + bar.get_width()/2., height + max(y_vals2) * 0.01,
                f'{count}', ha='center', va='bottom', fontweight='bold', fontsize=10)
    
    ax2.set_xlabel('Total Patches per Bug', fontsize=12, fontweight='bold')
    ax2.set_ylabel('Number of Bugs', fontsize=12, fontweight='bold')
    ax2.set_title('Distribution of Total Patches per Bug', fontsize=13, fontweight='bold', pad=20)
    ax2.grid(True, alpha=0.3, axis='y', linestyle='--')
    ax2.set_facecolor(colors['light_orange'])
    ax2.set_xlim(min(x_vals2) - 0.5, max(x_vals2) + 0.5)
    ax2.set_ylim(0, max(y_vals2) * 1.15)
    
    # Add stats box
    total_bugs_2 = sum(y_vals2)
    avg_patches = sum(x * y for x, y in zip(x_vals2, y_vals2)) / total_bugs_2
    stats_text_2 = f"Total: {total_bugs_2}\nAvg: {avg_patches:.1f}"
    ax2.text(0.98, 0.98, stats_text_2, transform=ax2.transAxes, fontsize=9,
            verticalalignment='top', horizontalalignment='right',
            bbox=dict(boxstyle="round,pad=0.3", facecolor="white", alpha=0.8, edgecolor=colors['accent1']))
    
    # Plot 3: Scatter plot - Groups vs Total Patches
    groups_list = list(groups_per_bug.values())
    patches_list = list(total_patches_per_bug.values())
    
    scatter = ax3.scatter(groups_list, patches_list, alpha=0.7, color=colors['secondary'], 
                         s=60, edgecolors='white', linewidth=1)
    ax3.set_xlabel('Number of Groups', fontsize=12, fontweight='bold')
    ax3.set_ylabel('Total Patches', fontsize=12, fontweight='bold')
    ax3.set_title('Groups vs Total Patches Correlation', fontsize=13, fontweight='bold', pad=20)
    ax3.grid(True, alpha=0.3, linestyle='--')
    ax3.set_facecolor(colors['light_green'])
    
    # Add correlation coefficient
    correlation = np.corrcoef(groups_list, patches_list)[0, 1]
    ax3.text(0.02, 0.98, f"Correlation: {correlation:.3f}", transform=ax3.transAxes, 
            fontsize=10, verticalalignment='top',
            bbox=dict(boxstyle="round,pad=0.3", facecolor="white", alpha=0.8, edgecolor=colors['secondary']))
    
    # Plot 4: Group size distribution
    all_group_sizes = []
    for info in bug_analysis.values():
        all_group_sizes.extend(info['groups'])
    
    group_size_dist = Counter(all_group_sizes)
    x_vals4 = sorted(group_size_dist.keys())
    y_vals4 = [group_size_dist[x] for x in x_vals4]
    
    bars4 = ax4.bar(x_vals4, y_vals4, color=colors['accent2'], alpha=0.8, 
                    edgecolor='white', linewidth=1.5)
    
    # Add value labels on bars
    for bar, count in zip(bars4, y_vals4):
        height = bar.get_height()
        ax4.text(bar.get_x() + bar.get_width()/2., height + max(y_vals4) * 0.01,
                f'{count}', ha='center', va='bottom', fontweight='bold', fontsize=10)
    
    ax4.set_xlabel('Group Size (Identical Patches)', fontsize=12, fontweight='bold')
    ax4.set_ylabel('Number of Groups', fontsize=12, fontweight='bold')
    ax4.set_title('Distribution of Group Sizes', fontsize=13, fontweight='bold', pad=20)
    ax4.grid(True, alpha=0.3, axis='y', linestyle='--')
    ax4.set_facecolor(colors['light_red'])
    ax4.set_xlim(min(x_vals4) - 0.5, max(x_vals4) + 0.5)
    ax4.set_ylim(0, max(y_vals4) * 1.15)
    
    # Add stats box
    total_groups = sum(y_vals4)
    avg_group_size = sum(x * y for x, y in zip(x_vals4, y_vals4)) / total_groups
    stats_text_4 = f"Total: {total_groups}\nAvg Size: {avg_group_size:.1f}"
    ax4.text(0.98, 0.98, stats_text_4, transform=ax4.transAxes, fontsize=9,
            verticalalignment='top', horizontalalignment='right',
            bbox=dict(boxstyle="round,pad=0.3", facecolor="white", alpha=0.8, edgecolor=colors['accent2']))
    
    # Improve overall layout
    plt.tight_layout(rect=[0, 0.03, 1, 0.95])  # Leave space for suptitle
    
    # Save with better quality
    plt.savefig(os.path.join(TMP_PLOTS_DIR, 'detailed_groups_analysis.png'), 
                dpi=300, bbox_inches='tight', facecolor='white', edgecolor='none')
    
    return bug_analysis

def get_group_count_distribution(deduplicated_patches) -> dict:
    """
    Analyze how many bugs have each number of groups.
    
    Returns:
        dict: {number_of_groups: number_of_bugs_with_that_many_groups}
    """
    # Group by bug_uid and count how many groups each bug has
    bug_groups = defaultdict(list)
    
    for _, row in deduplicated_patches.iterrows():
        bug_uid = row['bug_uid']
        num_identical = row['number_of_identical']
        bug_groups[bug_uid].append(num_identical)
    
    # Count how many groups each bug has
    bug_group_counts = {}
    for bug_uid, groups in bug_groups.items():
        bug_group_counts[bug_uid] = len(groups)
    
    # Create distribution: {number_of_groups: count_of_bugs}
    group_count_distribution = Counter(bug_group_counts.values())
    
    # Convert to regular dict and sort by number of groups
    distribution_dict = dict(sorted(group_count_distribution.items()))
    
    # Log some statistics
    logging.info(f"Group count distribution: {distribution_dict}")
    logging.info(f"Total bugs analyzed: {len(bug_group_counts)}")
    logging.info(f"Range of groups per bug: {min(distribution_dict.keys())} to {max(distribution_dict.keys())}")
    
    # Show examples for each group count
    logging.info("\nExamples by group count:")
    examples_by_count = defaultdict(list)
    for bug_uid, group_count in bug_group_counts.items():
        examples_by_count[group_count].append(bug_uid)
    
    for group_count in sorted(examples_by_count.keys()):
        bugs = examples_by_count[group_count]
        logging.info(f"  {group_count} groups: {len(bugs)} bugs (e.g., {bugs[0] if bugs else 'none'})")
    
    return distribution_dict

def plot_group_count_distribution(deduplicated_patches, distribution, save_path=None):
    """
    Create a horizontal bar plot showing the distribution of group counts.
    X-axis: Number of bugs that have that many groups
    Y-axis: Number of groups
    """
    
    # Create the plot with more compact dimensions
    fig, ax = plt.subplots(figsize=(8, 8))  # Changed from (12, 8) to (8, 8)
    
    x_values = list(distribution.keys())
    x_values[-1] = 25
    y_values = list(distribution.values())
    
    # Create horizontal bars with distinct colors
    distinct_colors = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd', 
                      '#8c564b', '#e377c2', '#7f7f7f', '#bcbd22', '#17becf',
                      '#aec7e8', '#ffbb78', '#98df8a', '#ff9896', '#c5b0d5']
    
    # Extend colors if needed
    if len(x_values) > len(distinct_colors):
        distinct_colors = distinct_colors * ((len(x_values) // len(distinct_colors)) + 1)
    
    colors = distinct_colors[:len(x_values)]
    bars = ax.barh(x_values, y_values, color=colors, alpha=0.8, edgecolor='black', linewidth=0.5)
    
    # Add value labels at the end of bars with reduced padding
    for i, (x, y) in enumerate(zip(x_values, y_values)):
        ax.text(y + max(y_values) * 0.005, x, str(y),  # Reduced padding from 0.01 to 0.005
                ha='left', va='center', fontweight='bold', fontsize=14)
    
    # Customize the plot
    ax.set_xlabel('Number of Bugs', fontsize=14, fontweight='bold')
    ax.set_ylabel('Number of Cluster', fontsize=14, fontweight='bold')
    # ax.set_title('Distribution of Group Counts Across Bugs', fontsize=14)
    
    # Set y-axis ticks and remove whitespace
    ax.set_yticks(x_values)
    ax.set_yticklabels(x_values)
    
    # Remove whitespace by setting tight limits
    ax.set_ylim(min(x_values) - 0.5, max(x_values) + 0.5)
    ax.set_xlim(0, max(y_values) * 1.05)  # Reduced padding from 1.1 to 1.05
    
    # Add grid
    # ax.grid(True, alpha=0.3, axis='x')
    
    # Remove top and right spines
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
    # Add statistics text box - moved to top right corner with transparent background and no border
    total_bugs = sum(y_values)
    avg_groups = sum(x * y for x, y in zip(x_values, y_values)) / total_bugs
    max_groups = max(x_values)
    min_groups = min(x_values)
    
    stats_text = f"\nTotal bugs: {total_bugs}\nRange: {min_groups}-{max_groups} groups"
    # ax.text(0.98, 0.98, stats_text, transform=ax.transAxes, fontsize=13,
    #         verticalalignment='top', horizontalalignment='right',
    #         bbox=dict(boxstyle="round,pad=0.3", facecolor="none", edgecolor="none", alpha=0))
    
    plt.tight_layout()
    
    # Save if path provided
    if save_path:
        plt.savefig(save_path, dpi=300, bbox_inches='tight')
        logging.info(f"Plot saved to: {save_path}")
    
    return distribution

def analyze_group_patterns(deduplicated_patches):
    """
    More detailed analysis of group patterns
    """
    # Group by bug_uid and collect the number_of_identical values
    bug_groups = defaultdict(list)
    
    for _, row in deduplicated_patches.iterrows():
        bug_uid = row['bug_uid']
        num_identical = row['number_of_identical']
        bug_groups[bug_uid].append(num_identical)
    
    # Sort groups within each bug by size
    for bug_uid in bug_groups:
        bug_groups[bug_uid].sort(reverse=True)
    
    # Analyze patterns
    pattern_analysis = {
        'total_bugs': len(bug_groups),
        'total_unique_groups': sum(len(groups) for groups in bug_groups.values()),
        'total_patches': sum(sum(groups) for groups in bug_groups.values()),
        'group_count_distribution': Counter(len(groups) for groups in bug_groups.values()),
        'group_size_distribution': Counter(size for groups in bug_groups.values() for size in groups)
    }
    
    # Find most common group patterns
    pattern_signatures = []
    for bug_uid, groups in bug_groups.items():
        signature = tuple(sorted(groups, reverse=True))
        pattern_signatures.append(signature)
    
    common_patterns = Counter(pattern_signatures).most_common(10)
    
    logging.info("=== GROUP PATTERN ANALYSIS ===")
    logging.info(f"Total bugs: {pattern_analysis['total_bugs']}")
    logging.info(f"Total unique groups: {pattern_analysis['total_unique_groups']}")
    logging.info(f"Total patches: {pattern_analysis['total_patches']}")
    
    logging.info(f"\nGroup count distribution: {dict(pattern_analysis['group_count_distribution'])}")
    logging.info(f"Group size distribution: {dict(pattern_analysis['group_size_distribution'])}")
    
    logging.info(f"\nMost common group patterns:")
    for i, (pattern, count) in enumerate(common_patterns, 1):
        logging.info(f"  {i:2d}. {pattern}: {count} bugs")
    
    return pattern_analysis


if __name__ == "__main__":
    # logging.info("Counting identical groups in correct patches 1-Deduplicated and tool generated ...") 
    # deduplicated_patches, dropped = count_identical_in_pairs() # Deduplicated with, and Keep Presentatives

    # # If you ant to EXPLORE DROPPED PATCHES UNCOMMENT THIS
    # dropped = dropped[dropped["reason_dropped"] == "duplicate_content"]
    # # keep deduplicated if their index is in representative_idx of dropped
    # kept = deduplicated_patches[deduplicated_patches.index.isin(dropped["representative_idx"])]
    # deduplicated_patches = pd.concat([kept, dropped])
    # # sort by bug_uid
    # deduplicated_patches.sort_values(by='bug_uid', inplace=True)

    # pairs = get_pairs(deduplicated_patches) # Use function from build.py


    # create_dot_plot_identical_patches(deduplicated_patches)
    # create_dot_plot_identical_patches_v2(deduplicated_patches)
    # create_dot_plot_identical_patches_v3(deduplicated_patches)
    # plot_group_count_distribution(deduplicated_patches, get_group_count_distribution(deduplicated_patches), save_path=os.path.join(TMP_PLOTS_DIR, 'group_count_distribution.png'))

    # # Detailed analysis
    # groups_dist, groups_per_bug = create_groups_distribution_plot(deduplicated_patches)

    # bug_analysis = create_detailed_groups_analysis(deduplicated_patches)
    # distribution = get_group_count_distribution(deduplicated_patches)
    # analyze_group_patterns(deduplicated_patches)

    # logging.info("Experiment #1 is done. Running results.py ...")
    # results = Results()
    # results.classify(labels=["yes", "no"])
    # results.classify(labels=["type-1", "type-2", "type-3", "type-4"], selected_results=[result for result in results.results if result["prompt"]["type"] in ["type", "integrated"]])
    # evaluator = Evaluator(results)
    # Plotter = Plotter()

    logging.info("Experiment #2 is done. Running results.py ...")
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

    # input_prompts = [
    #     "llm4cc-clone_type",
    #     "llm4cc-integrated",
    #     "llm4cc-simple_prompt-semantical",
    #     "llm4cc-reasoning-patch-semantical",
    #     "llm4cc-similarity_line-patch-semantical",
    #     "llm4cc-simple_prompt-identical",
    #     "llm4cc-reasoning-patch-identical",
    #     "llm4cc-similarity_line-patch-identical"
    # ]

    # results = Experiment2Results(selected_tool="tbar", input_processors=["method"], input_models=input_models, input_prompts=input_prompts)
    # results.classify(labels=["yes", "no"])
    # results.classify(labels=["type-1", "type-2", "type-3", "type-4", "not-clone"], selected_results=[result for result in results.results if result["prompt"]["type"] in ["type", "integrated"]])
    # evaluator = Experiment2Evaluator(results)

    input_prompts = [
        "llm4cc-clone_type-patch",
        "llm4cc-integrated-patch",
        "llm4cc-simple_prompt-semantical",
        "llm4cc-reasoning-patch-semantical",
        "llm4cc-similarity_line-patch-semantical",
        "llm4cc-simple_prompt-identical",
        "llm4cc-reasoning-patch-identical",
        "llm4cc-similarity_line-patch-identical"
    ]

    results = Experiment2Results(selected_tool="tbar", input_processors=["defaultpatch"], input_models=input_models, input_prompts=input_prompts)
    results.classify(labels=["yes", "no"])
    results.classify(labels=["type-1", "type-2", "type-3", "type-4", "not-clone"], selected_results=[result for result in results.results if result["prompt"]["type"] in ["type", "integrated"]])
    evaluator = Experiment2Evaluator(results)
    
    # logging.info("Experiment #7 is done. Running results.py ...")
    # report_exp7()






    # logging.info("Running Experiment #3 ...")
    # tools = [
    #     'Arja', 'Jaid', 'TBar', 'FixMiner', 'jKali', 'Nopol', 'HDRepair', 'ACS',
    #     'jGenProg', 'SketchFix', 'SimFix', 'AVATAR', 'GenProg', 'kPAR', 'Cardumen',
    #     'SequenceR', 'Kali', 'DynaMoth', 'SOFix', 'CapGen', 'jMutRepair', 'RSRepair'
    # ]
    
    # results = Experiment3Results(selected_tools=tools, input_processor="defaultpatch", input_model="qwen2.5:7b", input_prompt="llm4cc-clone_type-patch")
    # results.classify(labels=["yes", "no"])
    # results.classify(labels=["type-1", "type-2", "type-3", "type-4", "not-clone"], selected_results=[result for result in results.results if result["prompt"]["type"] in ["type", "integrated"]])

    # evaluator = Experiment3Evaluator(results)



    
