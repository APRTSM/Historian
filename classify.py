# import re
from huggingface_hub import InferenceClient
from transformers import pipeline
from itertools import combinations, product
from sklearn.metrics import cohen_kappa_score, precision_score, recall_score, accuracy_score, f1_score
from matplotlib.colors import LinearSegmentedColormap
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
from utils.config import *
from utils.benchmark import *
from utils.utils import *
from utils.tool import *
from utils.dataset import *
from build import *
import json


pipe = pipeline(model="facebook/bart-large-mnli", device=0)

# General
def _get_files(experiments, representations, models, temperatures, prompts):
    results = []

    for experiment in experiments:
        experiment_uid = experiment["uid"]

        for representation in representations:
            representation_uid = representation["uid"]

            for model in models:
                model_uid = model["uid"]

                for temperature in temperatures:
                    temperature_uid = temperature["uid"]

                    for prompt in prompts:
                        prompt_uid = prompt["uid"]
                        uid = f"{experiment_uid}-{representation_uid}-{model_uid}-{temperature_uid}-{prompt_uid}"
                        file_name = f"{uid}.pkl"
                        file_dir = os.path.join(TMP_RESULTS_DIR, file_name)

                        if not os.path.exists(file_dir):
                            logging.info(f"The result {file_name} does not exist.")

                            continue
                        
                        logging.info(f"The result {file_name} is added to the list.")

                        result = {
                            "uid": uid,
                            "experiment": experiment_uid,
                            "representation": representation_uid,
                            "model": model_uid,
                            "temperature": temperature_uid,
                            "prompt": prompt_uid,
                            "location": file_dir
                        }

                        results.append(result)

    return results


# Boolean
def _get_classification_boolean(response, classes):
    if "oneshot_boolean" in response:
        return response["oneshot_boolean"] 
    
    label = pipe(response["response"],
        candidate_labels=classes,
    )    

    return label

def _get_oneshot_labels_boolean(results, classes):
    for result in results:
        labeled_pickle_file = os.path.join(TMP_CLASSIFICATION_RESULTS_DIR, os.path.basename(result["location"]))

        if os.path.exists(labeled_pickle_file):
            result["location"] = labeled_pickle_file

            continue

        responses = pd.read_pickle(result["location"])
        responses["oneshot_boolean"] = responses.progress_apply(lambda x: _get_classification_boolean(x, classes), axis=1)

        pd.to_pickle(responses, labeled_pickle_file)

        result["location"] = labeled_pickle_file

    return results

def get_result_files_boolean_one_shot(experiments, representations, models, temperatures, prompts, one_shot_classes):
    if os.path.exists(TMP_BOOLEAN_RESULTS_JSON_FILE):
        with open(TMP_BOOLEAN_RESULTS_JSON_FILE, 'r') as file:
            results = json.load(file)

        return results
    
    results = _get_files(experiments, representations, models, temperatures, prompts)
    
    results = _get_oneshot_labels_boolean(results, one_shot_classes)

    with open(TMP_BOOLEAN_RESULTS_JSON_FILE, 'w') as file:
        json.dump(results, file)

    return results

def _get_groundtruth_label_boolean(response):
    yes_pattern = re.compile(r'\byes\b', re.IGNORECASE)
    no_pattern = re.compile(r'\bno\b', re.IGNORECASE)
    
    features = {}
    features['yes'] = bool(yes_pattern.search(response))
    features['no'] = bool(no_pattern.search(response))
    
    feature_list = []

    if features['yes']:
        feature_list.append(1)

    if features['no']:
        feature_list.append(0)

    if not len(feature_list) == 1:
        return None
    
    return feature_list[0]

def get_all_results_with_groundtruth_boolean(results) -> pd.DataFrame:
    discarded_df_list = []
    kept_df_list = []
    discarded_count = 0
    kept_count = 0

    for result in results:
        result_df = pd.read_pickle(result["location"])
        
        result_df['groundtruth'] = result_df['response'].apply(_get_groundtruth_label_boolean)

        discarded_df = result_df[result_df['groundtruth'].isna()]
        kept_df = result_df[result_df['groundtruth'].notna()]

        discarded_df_list.append(discarded_df)
        kept_df_list.append(result_df)

        discarded_count += len(discarded_df)
        kept_count += len(kept_df)

        result_df.dropna(subset=['groundtruth'], inplace=True)

    kept_df_all = pd.concat(kept_df_list, ignore_index=True) # Trivial Kept
    discarded_df_all = pd.concat(discarded_df_list, ignore_index=True) # Trivial Discarded

    kept_df_all["groundtruth"] = kept_df_all["groundtruth"].astype(int)

    logging.info(f"discarded: {discarded_count}, kept: {kept_count}")   

    return kept_df_all, discarded_df_all

def plot_boxes_boolean(kept_df, output_path):
    kept_df["oneshot_boolean_score"] = kept_df["oneshot_boolean"].apply(lambda x: x["scores"][0] if x["labels"][0] == "‘yes‘" else x["scores"][1])

    plt.figure(figsize=(10, 6))
    sns.boxplot(x="groundtruth", y="oneshot_boolean_score", hue="groundtruth", data=kept_df, palette="Set3", legend=False)
    plt.xlabel('Ground Truth')
    plt.ylabel('One-shot Boolean Score')
    plt.title('Box Plot of One-shot Boolean Scores by Ground Truth')
    plt.savefig(output_path)
    plt.close()

def _decide_threshold_boolean(response, threshold, label_name):
    label = dict(response)

    if label["labels"][0] == label_name:
        index = 0

    else:
        index = 1

    if label["scores"][index] < threshold:
        return 0
    
    return 1

def get_roc_exp1_each_boolean(results, one_shot_true_class, save=False):
    discarded_df_list = []
    kept_df_list = []
    discarded_count = 0
    kept_count = 0

    for result in results:
        logging.info(f"Processing Thresholds {result['uid']}")

        result[f'threshold_f1'] = []
        result_df = pd.read_pickle(result["location"])

        result_df['groundtruth'] = result_df['response'].apply(_get_groundtruth_label_boolean)

        discarded_df = result_df[result_df['groundtruth'].isna()]
        kept_df = result_df[result_df['groundtruth'].notna()]

        discarded_df_list.append(discarded_df)

        result["discarded"] = len(discarded_df)
        result["kept"] = len(kept_df)

        discarded_count += len(discarded_df)
        kept_count += len(kept_df)

        result_df.dropna(subset=['groundtruth'], inplace=True)

        for threshold in np.arange(0, 1.00001, 0.01):
            threshold = round(threshold, 2)

            result_df[f'threshold_{threshold}_label'] = result_df['oneshot_boolean'].apply(lambda x: _decide_threshold_boolean(x, threshold, one_shot_true_class))

            result["threshold_f1"].append({
                "threshold": threshold,
                "f1": f1_score(result_df['groundtruth'], result_df[f'threshold_{threshold}_label']),
                "precision": precision_score(result_df['groundtruth'], result_df[f'threshold_{threshold}_label']),
                "recall": recall_score(result_df['groundtruth'], result_df[f'threshold_{threshold}_label']),
            })

        kept_df_list.append(result_df)

    kept_df_all = pd.concat(kept_df_list, ignore_index=True) # Trivial Kept

    # ALL
    thresholds = []
    f1_scores = []
    highest_threshold = 0

    for threshold in np.arange(0, 1.00001, 0.01):
        threshold = round(threshold, 2)
        threshold_label = f'threshold_{threshold}_label'
        
        kept_df_all["groundtruth"] = kept_df_all["groundtruth"].astype(int)
        kept_df_all[threshold_label] = kept_df_all[threshold_label].astype(int)

        f1 = f1_score(kept_df_all['groundtruth'], kept_df_all[threshold_label])

        thresholds.append(threshold)
        f1_scores.append(f1)

        if threshold >= highest_threshold:
            highest_threshold = threshold

        logging.info(f"discarded: {discarded_count}, kept: {kept_count}, f1: {f1}, threshold: {threshold}")

    plt.figure()
    plt.plot(thresholds, f1_scores, marker='o')
    plt.xlabel('Threshold')
    plt.ylabel('F1 Score')
    plt.title(f'F1 Score vs Threshold for ALL')
    plt.grid(True)

    output_path = os.path.join(TMP_PLOTS_DIR, f'ALL_f1_vs_threshold.png')
    plt.savefig(output_path)
    plt.close()

    if save:
        with open(TMP_RESULTS_JSON_FILE, 'w') as file:
            json.dump(results, file)

    return results, highest_threshold

def plot_roc_curve_boolean(results, output_dir):
    for result in results:
        thresholds = [item["threshold"] for item in result["threshold_f1"]]
        f1_scores = [item["f1"] for item in result["threshold_f1"]]

        plt.figure()
        plt.plot(thresholds, f1_scores, marker='o')
        plt.xlabel('Threshold')
        plt.ylabel('F1 Score')
        plt.title(f'F1 Score vs Threshold for {result["uid"]}')
        plt.grid(True)

        output_path = os.path.join(output_dir, f'{result["uid"]}_f1_vs_threshold.png')
        plt.savefig(output_path)
        plt.close()

def get_roc_exp1_boolean(kept_df, one_shot_true_class, step_size):
    thresholds = []
    f1_scores = []
    highest_threshold = 0
    highest_f1 = 0

    for threshold in np.arange(0, 1.00001, step_size):
        threshold = round(threshold, 2)

        threshold_decision = kept_df['oneshot_boolean'].apply(lambda x: _decide_threshold_boolean(x, threshold, one_shot_true_class))
        
        f1 = f1_score(kept_df['groundtruth'], threshold_decision)

        thresholds.append(threshold)
        f1_scores.append(f1)

        if f1 >= highest_f1:
            highest_f1 = f1
            highest_threshold = threshold

    plt.figure()
    plt.plot(thresholds, f1_scores, marker='o')
    plt.xlabel('Threshold')
    plt.ylabel('F1 Score')
    plt.title(f'F1 Score vs Threshold for ALL')
    plt.grid(True)

    output_path = os.path.join(TMP_PLOTS_DIR, f'ALL_f1_vs_threshold.png')
    plt.savefig(output_path)
    plt.close()

    return highest_threshold

def _decide_threshold_boolean_with_discarded(response, threshold, a, b, label_name):
    label = dict(response)

    if label["labels"][0] == label_name:
        index = 0

    else:
        index = 1

    if label["scores"][index] < threshold - b:
        return 0

    if label["scores"][index] > threshold + a:
        return 1
    
    return None


def get_a_b_boolean(kept_df, threshold, one_shot_true_class, step_size):
    a = 0
    max_a = 1 - threshold
    b = 0
    max_b = threshold 

    a_values = [round(x, 2) for x in np.arange(a, max_a, step_size)]
    b_values = [round(x, 2) for x in np.arange(b, max_b, step_size)]

    combinations = list(product(a_values, b_values))

    a_values = []
    b_values = []   
    objectives = []

    highest_a = 0
    highest_b = 0
    highest_objective = 0

    for combination in combinations:
        a, b = combination

        threshold_decision = kept_df['oneshot_boolean'].apply(lambda x: _decide_threshold_boolean_with_discarded(x, threshold, a, b, one_shot_true_class))

        interval_discarded_df = kept_df[threshold_decision.isna()]
        interval_discarded_ratio = (len(interval_discarded_df) + 1) / len(kept_df)

        interval_kept_df = kept_df[threshold_decision.notna()]
        interval_kept_ratio = (len(interval_kept_df) + 1) / len(kept_df)

        f1 = f1_score(interval_kept_df['groundtruth'], threshold_decision[interval_kept_df.index], zero_division=np.nan)

        if f1 == np.nan:
            logging.info(f"None Value for a: {a}, b: {b}, objective: {objective}, interval_kept: {len(interval_kept_df)}, interval_discarded: {len(interval_discarded_df)}, total: {len(kept_df)}")

            continue

        objective = f1 - 0.1 * interval_discarded_ratio

        if objective == highest_objective:
            logging.info(f"Same Value for a: {a}, b: {b}, objective: {objective}, interval_kept: {len(interval_kept_df)}, interval_discarded: {len(interval_discarded_df)}, total: {len(kept_df)}")

        a_values.append(a)
        b_values.append(b)
        objectives.append(objective)

        if objective >= highest_objective:
            print(objective, highest_objective)
            highest_objective = objective
            highest_a = a
            highest_b = b

    logging.info(f"highest_a: {highest_a}, highest_b: {highest_b}, highest_objective: {highest_objective}, interval_kept: {len(interval_kept_df)}, interval_discarded: {len(interval_discarded_df)}, total: {len(kept_df)}")
    
    fig = plt.figure()
    ax = fig.add_subplot(111, projection='3d')
    ax.plot_trisurf(a_values, b_values, objectives, cmap='viridis')
    ax.set_xlabel('Alpha')
    ax.set_ylabel('Beta')
    ax.set_zlabel('Objective')
    plt.title('3D Surface Plot of Alpha, Beta, and Objective')
    output_path = os.path.join(TMP_PLOTS_DIR, 'ALL_3d_surface_plot_a_b_objectives.png')
    plt.savefig(output_path)
    plt.close()

    return highest_a, highest_b
    

if __name__ == "__main__":
    # init
    bugs, developer_patches, tool_patches = init(configure=False)
    experiments = [
        {"uid": "EXP1"}
    ]
    representations = get_patch_processors()
    prompts, models, temperatures = get_tool_settings() 

    # Boolean
    boolean_prompts = [prompt for prompt in prompts if prompt["type"] == "boolean"]
    boolean_results = get_result_files_boolean_one_shot(experiments, representations, models, temperatures, boolean_prompts, one_shot_classes=["‘yes‘", "‘no‘"]) 
    kept_df, discarded_df = get_all_results_with_groundtruth_boolean(boolean_results)

    ## Threshold SVM
    plot_boxes_boolean(kept_df, os.path.join(TMP_PLOTS_DIR, "boxes-boolean.png"))
    
    ## Threshold ROC / Add to Results / For each result
    # boolean_results, threshold_boolean = get_roc_exp1_each_boolean(boolean_results, one_shot_true_class="‘yes‘", save=True)
    # plot_roc_curve_boolean(boolean_results, os.path.join(TMP_PLOTS_DIR, "roc"))

    ## Threshold ROC / For all results
    threshold_boolean = get_roc_exp1_boolean(kept_df, one_shot_true_class="‘yes‘", step_size=0.01)
    alpha_boolean, beta_boolean = get_a_b_boolean(kept_df, threshold_boolean, one_shot_true_class="‘yes‘", step_size=0.01)

    print(alpha_boolean, beta_boolean)









