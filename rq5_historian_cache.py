
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

def correct_cache_result_uid(label: pd.Series) -> pd.Series:
    pathc_name, project_name, number, generator = label["Patch"].split("/")[1].replace("-plausible", "").replace(".patch", "").split("-")
    # llm4pc-defects4j-Closure-114-GenProg-patch1 
    generator = generator.replace("Nopol2015", "Nopol").replace("Nopol2017", "Nopol")
    patch_uid = f"llm4pc-defects4j-{project_name}-{number}-{generator}-{pathc_name}"

    return patch_uid

def get_cache_labels():
    cache_results = pd.read_csv(os.path.join(RQ5_DIR, "Cache.csv"))

    # apply correct_cache_result_uid
    cache_results["tool_patch_uid"] = cache_results.apply(correct_cache_result_uid, axis=1)

    # Make first character capital
    cache_results["Predicted value"] = cache_results["Predicted value"].str.capitalize()

    return cache_results

def replace_other_apca_labels(df: pd.DataFrame, other_apca_labels: pd.DataFrame, label_column, id_column: str, other_label_column: str) -> pd.DataFrame:
    other_apca_labels.to_csv("notes/other_apca_labels.csv", index=False)  # Debugging line
    df.to_csv("notes/df.csv", index=False)  # Debugging line

    unknown_mask = df[label_column] == "Unknown"
    # Use merge instead of map to handle duplicate IDs
    lookup_dict = other_apca_labels.drop_duplicates(subset=[id_column]).set_index(id_column)[other_label_column].to_dict()
    mapped_values = df.loc[unknown_mask, id_column].map(lookup_dict)
    unmatched = df.loc[unknown_mask & mapped_values.isna(), id_column].unique()
    if len(unmatched) > 0:
        raise ValueError(f"Unmatched IDs: {unmatched.tolist()}")
    df.loc[unknown_mask, label_column] = mapped_values
    return df

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


class Experiment3Results:
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

        # # Simple Prompts Yes/No (translate yes no to overfitting and correct) Majority Voting NOT Applied
        # simple_prompts = [
        #     "llm4cc-simple_prompt-semantical",
        #     "llm4cc-reasoning-patch-semantical",
        #     "llm4cc-similarity_line-patch-semantical",

        #     "llm4cc-simple_prompt-identical",
        #     "llm4cc-reasoning-patch-identical",
        #     "llm4cc-similarity_line-patch-identical",
        # ]
        # simple_results = [result for result in self.results.results if result["prompt"]["uid"] in simple_prompts]


        # self._get_simple_results_table(simple_results, ["yes", "no"], pd.concat((results.input_developer_patches, results.input_tool_patches), axis=0))

        # Simple Prompts Type Binary (translate type to overfitting and correct) Majority Voting Applied, Inverted, Punished
        type_binary_prompts = [
            # "llm4cc-clone_type",
            # "llm4cc-integrated",
            "llm4cc-clone_type-patch",
            # "llm4cc-integrated-patch",
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

        # Each result corresponds to a tool
        for result in results:
            classified_result_dir = result[f"classified_result_file_{'-'.join(labels)}"]
            df = pd.read_pickle(classified_result_dir)

            ground_truth_clean = ground_truth[~ground_truth.index.duplicated(keep='first')]
            # Get groundtruth patch correctness for each comparison
            df["groundtruth_correctness"] = ground_truth_clean.loc[df.index]["correctness"].values

            # Get Raw Translation (Type to Binary), Each comparison
            # Use groundtruth_correctness and predicted_type_label to get raw_predicted_binary_label
            df["raw_predicted_binary_label"] = df.apply(lambda x: self._translate_type_label_to_binary(x["groundtruth_correctness"], x["predicted_label"]), axis=1)

            # Use Raw to Get Majority Vote (Overfitting/Correct/Unknown) for Each tool_patch_uid
            majority_labels = majority_vote_labels(df, label_column="raw_predicted_binary_label", id_column="tool_patch_uid")
            # tool_patch_uid is the uid of the selected tool, Deduplication makes reduce from comparison to selected tool patch
            df_voted = df[["tool_patch_uid"]].drop_duplicates().copy()

            # selected_correctness is the selected tool patch actual correctness for the tool_patch_uid Correct/Overfitting
            df_voted["selected_correctness"] = ground_truth_clean.loc[df_voted["tool_patch_uid"]]["correctness"].values

            # predicted_binary_label is the predicted
            df_voted["predicted_binary_label"] = df_voted["tool_patch_uid"].map(majority_labels)

            # Now it is predicted_binary_label(Correct/Overfitting/Unknown) vs selected_correctness(Correct/Overfitting)given for tool_patch_uid

            # Replace unknowns in df_voted["predicted_binary_label"] with other APCA Tool Labels
            df_voted = replace_other_apca_labels(df_voted, get_cache_labels(), "predicted_binary_label", "tool_patch_uid", "Predicted value")
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

        table.to_csv(TMP_TYPE_BINARY_RESULTS_CSV_EXP5)


# def historan_cache(results: Results):
#     for result in results.results:
#         logging.info(f"Processing Historian Cache for {result['tool']} ...")

#         classified_result_dir = result[f"classified_result_file_type-1-type-2-type-3-type-4-not-clone"]

#         df = pd.read_pickle(classified_result_dir)

#         df.to_html("notes/debug.html")
#         raise


#         cache_dir = os.path.join(TMP_HISTORIAN_CACHE_DIR, f"historian_cache_{result['tool']}.pkl")

#         if os.path.exists(cache_dir):
#             logging.info(f"Skipping Historian Cache for {result['tool']} as it already exists.")

#             continue

#         else:
#             logging.info(f"Creating Historian Cache for {result['tool']} at {cache_dir}")

#             historian_cache = df[["tool_patch_uid", "response", "predicted_label"]].copy()
#             historian_cache = historian_cache.rename(columns={
#                 "tool_patch_uid": "patch_uid",
#                 "response": "llm_response",
#                 "predicted_label": "llm_predicted_label"
#             })

#             historian_cache.to_pickle(cache_dir)

if __name__ == "__main__":
    # bugs, developer_patches, tool_patches = init(configure=False)
    # bugs_with_uid = bugs.reset_index()  # This makes 'uid' a regular column
    # bugs_dict = bugs_with_uid.to_dict('records')
    # llm4pc_patches = pd.DataFrame(get_llm4pc_dataset(bugs_dict)).set_index("uid")

    # print(llm4pc_patches)
    # llm4pc_patches.to_html("notes/debug.html")

    # raise

    logging.info("Experiment 5 Historian Cache Results Module")

    logging.info("Running Experiment #3 ...")
    tools = [
            'Arja', 'Jaid', 'TBar', 'FixMiner', 'jKali', 'Nopol', 'HDRepair', 'ACS',
        'jGenProg', 'SketchFix', 'SimFix', 'AVATAR', 'GenProg', 'kPAR', 'Cardumen',
        'SequenceR', 'Kali', 'DynaMoth', 'SOFix', 'CapGen', 'jMutRepair', 'RSRepair'
    ]

    input_processor="defaultpatch"
    input_model="qwen2.5:7b"
    input_prompt="llm4cc-clone_type-patch"
    
    results = Experiment3Results(selected_tools=tools, input_processor=input_processor, input_model=input_model, input_prompt=input_prompt)
    results.classify(labels=["yes", "no"])
    results.classify(labels=["type-1", "type-2", "type-3", "type-4", "not-clone"], selected_results=[result for result in results.results if result["prompt"]["type"] in ["type", "integrated"]])

    evaluator = Experiment3Evaluator(results)
    # historan_cache(results) Delete this



