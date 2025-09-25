
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

class Results:
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

                        file_name = f"EXP2-{self.selected_tool}-{processor['uid']}-{model['uid']}-{temperature['uid']}-{prompt['uid']}.pkl" #
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
            result[f"classified_result_file_{'-'.join(labels)}"] = classified_result_dir #

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

class RegexEvaluator:
    def __init__(self, results: Results):
        self.results = results
        self.results = self.evaluate(labels=["yes", "no"])
        self.results = self.evaluate(labels=["type-1", "type-2", "type-3", "type-4", "not-clone"], selected_results=[result for result in results.results if result["prompt"]["type"] in ["type", "integrated"]])

        self.averages = self.calculate_averages()
        self.per_model_averages = self.calculate_per_model_averages()
        
        self.save_results()


    def calculate_per_model_averages(self):
        per_model_averages = {}
        
        # Group results by model
        for result in self.results.results:
            model_uid = result["model"]["uid"]
            
            if model_uid not in per_model_averages:
                per_model_averages[model_uid] = {
                    "regex_portion_yes_no": [],
                    "regex_portion_clone_type": [],
                    "zero_shot_acc_yes_no": [],
                    "zero_shot_acc_clone_type": []
                }
            
            # Collect metrics for this model
            if "regex_portion_yes-no" in result:
                per_model_averages[model_uid]["regex_portion_yes_no"].append(result["regex_portion_yes-no"])
            
            if "regex_portion_type-1-type-2-type-3-type-4-not-clone" in result:
                per_model_averages[model_uid]["regex_portion_clone_type"].append(result["regex_portion_type-1-type-2-type-3-type-4-not-clone"])
            
            if "zero_shot_acc_yes-no" in result:
                per_model_averages[model_uid]["zero_shot_acc_yes_no"].append(result["zero_shot_acc_yes-no"])
            
            if "zero_shot_acc_type-1-type-2-type-3-type-4-not-clone" in result:
                per_model_averages[model_uid]["zero_shot_acc_clone_type"].append(result["zero_shot_acc_type-1-type-2-type-3-type-4-not-clone"])
        
        # Calculate averages for each model
        for model_uid in per_model_averages:
            model_data = per_model_averages[model_uid]
            
            per_model_averages[model_uid] = {
                "avg_regex_portion_yes_no": sum(model_data["regex_portion_yes_no"]) / len(model_data["regex_portion_yes_no"]) if model_data["regex_portion_yes_no"] else 0,
                "avg_regex_portion_clone_type": sum(model_data["regex_portion_clone_type"]) / len(model_data["regex_portion_clone_type"]) if model_data["regex_portion_clone_type"] else 0,
                "avg_zero_shot_acc_yes_no": sum(model_data["zero_shot_acc_yes_no"]) / len(model_data["zero_shot_acc_yes_no"]) if model_data["zero_shot_acc_yes_no"] else 0,
                "avg_zero_shot_acc_clone_type": sum(model_data["zero_shot_acc_clone_type"]) / len(model_data["zero_shot_acc_clone_type"]) if model_data["zero_shot_acc_clone_type"] else 0
            }
            
            logging.info(f"Model {model_uid} - Regex portion (yes-no): {per_model_averages[model_uid]['avg_regex_portion_yes_no']:.3f}")
            logging.info(f"Model {model_uid} - Regex portion (clone type): {per_model_averages[model_uid]['avg_regex_portion_clone_type']:.3f}")
            logging.info(f"Model {model_uid} - Zero-shot accuracy (yes-no): {per_model_averages[model_uid]['avg_zero_shot_acc_yes_no']:.3f}")
            logging.info(f"Model {model_uid} - Zero-shot accuracy (clone type): {per_model_averages[model_uid]['avg_zero_shot_acc_clone_type']:.3f}")
        
        return per_model_averages

    def calculate_averages(self):
        # Average regex_portion for yes-no labels
        yes_no_portions = [result["regex_portion_yes-no"] for result in self.results.results if "regex_portion_yes-no" in result]
        avg_regex_portion_yes_no = sum(yes_no_portions) / len(yes_no_portions) if yes_no_portions else 0

        # Average regex_portion for clone type labels  
        clone_type_portions = [result["regex_portion_type-1-type-2-type-3-type-4-not-clone"] for result in self.results.results if "regex_portion_type-1-type-2-type-3-type-4-not-clone" in result]
        avg_regex_portion_clone_type = sum(clone_type_portions) / len(clone_type_portions) if clone_type_portions else 0

        # Average zero_shot_acc for yes-no labels
        yes_no_accs = [result["zero_shot_acc_yes-no"] for result in self.results.results if "zero_shot_acc_yes-no" in result]
        avg_zero_shot_acc_yes_no = sum(yes_no_accs) / len(yes_no_accs) if yes_no_accs else 0

        # Average zero_shot_acc for clone type labels
        clone_type_accs = [result["zero_shot_acc_type-1-type-2-type-3-type-4-not-clone"] for result in self.results.results if "zero_shot_acc_type-1-type-2-type-3-type-4-not-clone" in result]
        avg_zero_shot_acc_clone_type = sum(clone_type_accs) / len(clone_type_accs) if clone_type_accs else 0

        logging.info(f"Average regex portion (yes-no): {avg_regex_portion_yes_no:.3f}")
        logging.info(f"Average regex portion (clone type): {avg_regex_portion_clone_type:.3f}")
        logging.info(f"Average zero-shot accuracy (yes-no): {avg_zero_shot_acc_yes_no:.3f}")
        logging.info(f"Average zero-shot accuracy (clone type): {avg_zero_shot_acc_clone_type:.3f}")
        
        return {
            "avg_regex_portion_yes_no": avg_regex_portion_yes_no,
            "avg_regex_portion_clone_type": avg_regex_portion_clone_type,
            "avg_zero_shot_acc_yes_no": avg_zero_shot_acc_yes_no,
            "avg_zero_shot_acc_clone_type": avg_zero_shot_acc_clone_type
        }

    def save_results(self):
        json.dump([{k: v if isinstance(v, (str, int, float, bool, type(None))) else str(v) for k, v in result.items()} for result in self.results.results], open(os.path.join(TMP_RQ3_PLOTS_DIR, f"evaluation_results_{self.results.patch_processors[0]['uid']}.json"), "w"), indent=4)
        json.dump(self.averages, open(os.path.join(TMP_RQ3_PLOTS_DIR, f"evaluation_averages_{self.results.patch_processors[0]['uid']}.json"), "w"), indent=4)
        json.dump(self.per_model_averages, open(os.path.join(TMP_RQ3_PLOTS_DIR, f"evaluation_per_model_averages_{self.results.patch_processors[0]['uid']}.json"), "w"), indent=4)

    def evaluate(self, labels: list, selected_results=None):
        if not selected_results:
            selected_results = self.results.results

        for result in selected_results:
            logging.info(f"Evaluating {result['file_name']} with labels {labels}")
            
            # Load the classified results
            classified_file = result[f"classified_result_file_{'-'.join(labels)}"]
            if not os.path.exists(classified_file):
                logging.warning(f"Classified file not found: {classified_file}")
                continue
                
            df = pd.read_pickle(classified_file)
            
            # Function to extract label using regex
            def extract_label_regex(text, valid_labels):
                if pd.isna(text):
                    return None
                    
                text_lower = str(text).lower()
                found_labels = []
                
                for label in valid_labels:
                    # Create regex pattern for the label
                    pattern = r'\b' + re.escape(label.lower()) + r'\b'
                    if re.search(pattern, text_lower):
                        found_labels.append(label)
                
                # Return label only if exactly one is found
                return found_labels[0] if len(found_labels) == 1 else None
            
            # Apply regex extraction
            df['regex_label'] = df['response'].apply(lambda x: extract_label_regex(x, labels))
            
            # Calculate metrics
            total_responses = len(df)
            regex_detected = df['regex_label'].notna().sum()
            regex_portion = regex_detected / total_responses if total_responses > 0 else 0
            
            # Calculate zero-shot accuracy on regex-detectable responses
            regex_detectable_df = df[df['regex_label'].notna()]
            if len(regex_detectable_df) > 0:
                # Compare regex labels with zero-shot predictions
                zero_shot_acc = (regex_detectable_df['regex_label'] == regex_detectable_df['predicted_label']).mean()
            else:
                zero_shot_acc = 0
            
            # Store results with label-dependent keys
            labels_key = '-'.join(labels)
            result[f"regex_portion_{labels_key}"] = regex_portion
            result[f"total_{labels_key}"] = total_responses
            result[f"zero_shot_acc_{labels_key}"] = zero_shot_acc
            result[f"regex_detected_{labels_key}"] = regex_detected
            
            logging.info(f"Results for {result['file_name']}: "
                        f"Total: {total_responses}, "
                        f"Regex detected: {regex_detected}, "
                        f"Regex portion: {regex_portion:.3f}, "
                        f"Zero-shot accuracy: {zero_shot_acc:.3f}")

        logging.info("Evaluation Done.")
        return self.results

def plot_json_data(json_file_path):
    # Load JSON data
    with open(json_file_path, 'r') as file:
        data = json.load(file)
    
    # Extract model names and metrics
    models = list(data.keys())
    zero_shot_acc = [data[model]['avg_zero_shot_acc_yes_no'] for model in models]
    regex_portion = [data[model]['avg_regex_portion_yes_no'] for model in models]
    
    # Create DataFrame for easy plotting
    df = pd.DataFrame({
        'Model': models,
        'Zero Shot Accuracy': zero_shot_acc,
        'Regex Portion': regex_portion
    })
    
    # Create the plot
    fig, ax = plt.subplots(figsize=(12, 8))
    
    # Create bar chart
    x = range(len(models))
    width = 0.35
    bars1 = ax.bar([i - width/2 for i in x], zero_shot_acc, width, label='Accuracy', alpha=0.8)
    bars2 = ax.bar([i + width/2 for i in x], regex_portion, width, label='Regex Portion', alpha=0.8)
    
    # Customize the plot
    # ax.set_xlabel('Models')
    # ax.set_ylabel('Values')
    ax.set_title('Model Performance Comparison for Diff Representation')
    ax.set_xticks(x)
    ax.set_xticklabels(models, rotation=45, ha='right')
    
    # Remove grid and make legend smaller
    ax.legend(fontsize='small')
    
    # Remove top and right spines (borders)
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
    # Add value labels on bars
    for bar in bars1:
        height = bar.get_height()
        ax.text(bar.get_x() + bar.get_width()/2., height + 0.01,
                f'{height:.3f}', ha='center', va='bottom', fontsize=8)
    
    for bar in bars2:
        height = bar.get_height()
        ax.text(bar.get_x() + bar.get_width()/2., height + 0.01,
                f'{height:.3f}', ha='center', va='bottom', fontsize=8)
    
    plt.tight_layout()
    plt.savefig(os.path.join(TMP_RQ3_PLOTS_DIR, f"model_comparison_zeroshot.png"), dpi=300, bbox_inches='tight')
    plt.close()


def create_latex_table(json_file_path):
    # Load JSON data
    with open(json_file_path, 'r') as file:
        data = json.load(file)
    
    # Extract model names and metrics
    models = list(data.keys())
    zero_shot_acc = [data[model]['avg_zero_shot_acc_yes_no'] for model in models]
    regex_portion = [data[model]['avg_regex_portion_yes_no'] for model in models]
    
    # Create LaTeX table
    table_content = []
    
    # Table header
    header = " & ".join(models) + " \\\\"
    table_content.append(header)
    table_content.append("\\hline")
    
    # Zero shot accuracy row
    acc_row = "Zero Shot Accuracy & " + " & ".join([f"{acc:.3f}" for acc in zero_shot_acc]) + " \\\\"
    table_content.append(acc_row)
    
    # Regex portion row  
    regex_row = "Regex Portion & " + " & ".join([f"{portion:.3f}" for portion in regex_portion]) + " \\\\"
    table_content.append(regex_row)
    
    # Complete LaTeX table
    num_cols = len(models) + 1  # +1 for the metric name column
    col_spec = "l" + "c" * len(models)  # left-aligned first column, centered others
    
    latex_table = f"""\\begin{{tabular}}{{{col_spec}}}
        \\toprule
        {table_content[0]}
        \\midrule
        {table_content[2]}
        {table_content[3]}
        \\bottomrule
        \\end{{tabular}}
    """
    
    # Save to file
    output_path = os.path.join(TMP_RQ3_PLOTS_DIR, "model_comparison_table.tex")
    with open(output_path, 'w') as f:
        f.write(latex_table)
    
    # Also print to console
    print(latex_table)
    
    return latex_table


if __name__ == "__main__":
    # input_models = [
    #     "magicoder:7b-s-cl",
    #     "codellama:7b-instruct",
    #     "codellama:13b-instruct",
    #     "deepseek-coder:6.7b",
    #     "codegemma:7b-instruct",
    #     "qwen2.5:7b",
    #     "qwen2.5-coder:7b",
    #     "yi-coder:9b",
    #     "hermes3:8b"
    # ]

    # input_prompts = [
    #     # "llm4cc-simple_prompt",
    #     # "llm4cc-reasoning",
    #     # "llm4cc-similarity_line",
        
    #     "llm4cc-clone_type",
    #     "llm4cc-integrated",

    #     "llm4cc-simple_prompt-semantical",
    #     "llm4cc-reasoning-patch-semantical",
    #     "llm4cc-similarity_line-patch-semantical",

    #     "llm4cc-simple_prompt-identical",
    #     "llm4cc-reasoning-patch-identical",
    #     "llm4cc-similarity_line-patch-identical"
    # ]

    # input_processors = ["method"]

    # results = Results(selected_tool="tbar", input_processors=input_processors, input_models=input_models, input_prompts=input_prompts)
    # results.classify(labels=["yes", "no"])
    # results.classify(labels=["type-1", "type-2", "type-3", "type-4", "not-clone"], selected_results=[result for result in results.results if result["prompt"]["type"] in ["type", "integrated"]])
    # evaluator = RegexEvaluator(results)

    # input_prompts = [
    #     "llm4cc-clone_type-patch",
    #     "llm4cc-integrated-patch",

    #     "llm4cc-simple_prompt-semantical",
    #     "llm4cc-reasoning-patch-semantical",
    #     "llm4cc-similarity_line-patch-semantical",

    #     "llm4cc-simple_prompt-identical",
    #     "llm4cc-reasoning-patch-identical",
    #     "llm4cc-similarity_line-patch-identical"
    # ]

    # input_processors = ["defaultpatch"]

    # results = Results(selected_tool="tbar", input_processors=input_processors, input_models=input_models, input_prompts=input_prompts)
    # results.classify(labels=["yes", "no"])
    # results.classify(labels=["type-1", "type-2", "type-3", "type-4", "not-clone"], selected_results=[result for result in results.results if result["prompt"]["type"] in ["type", "integrated"]])
    # evaluator = RegexEvaluator(results)

    plot_json_data(os.path.join(TMP_RQ3_PLOTS_DIR, f"evaluation_per_model_averages_defaultpatch.json"))
    create_latex_table(os.path.join(TMP_RQ3_PLOTS_DIR, f"evaluation_per_model_averages_defaultpatch.json"))