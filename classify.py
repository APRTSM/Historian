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










"""
# import re
from huggingface_hub import InferenceClient
from transformers import pipeline
from itertools import combinations
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
def get_result_files(experiments, representations, models, temperatures, prompts):
    if os.path.exists(TMP_RESULTS_JSON_FILE):
        with open(TMP_RESULTS_JSON_FILE, 'r') as file:
            results = json.load(file)

        return results
    
    results = get_files(experiments, representations, models, temperatures, prompts)
    results = get_oneshot_labels(results, ["code clone", "not code clone"])

    with open(TMP_RESULTS_JSON_FILE, 'w') as file:
        json.dump(results, file)

    return results

# Clone/Not Clone
def get_all_results_with_groundtruth(results) -> pd.DataFrame:
    discarded_df_list = []
    kept_df_list = []
    discarded_count = 0
    kept_count = 0

    for result in results:
        result_df = pd.read_pickle(result["location"])
        
        result_df['groundtruth'] = result_df['response'].apply(get_groundtruth_label_boolean)

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

def get_oneshot_distribution(results):
    kept_count = get_all_results_with_groundtruth(results)







        


# Get OneShot Boolean Labels
def get_classification_boolean(response, classes):
    if "oneshot_boolean" in response:
        return response["oneshot_boolean"] 
    
    label = pipe(response["response"],
        candidate_labels=classes,
    )    

    return label

def get_oneshot_labels(results, classes):
    for result in results:
        labeled_pickle_file = os.path.join(TMP_CLASSIFICATION_RESULTS_DIR, os.path.basename(result["location"]))

        if os.path.exists(labeled_pickle_file):
            result["location"] = labeled_pickle_file

            continue

        responses = pd.read_pickle(result["location"])
        responses["oneshot_boolean"] = responses.progress_apply(lambda x: get_classification_boolean(x, classes), axis=1)

        pd.to_pickle(responses, labeled_pickle_file)

        result["location"] = labeled_pickle_file

    return results

def decide_boolean(response, threshold):
    label = dict(response)

    if label["labels"][0] == "code clone":
        index = 0

    else:
        index = 1

    if label["scores"][index] < threshold:
        return 0
    
    return 1

def get_boolean_labels(results):
    for result in results:
        result_df = pd.read_pickle(result["location"])
        result_df['oneshot_boolean_label'] = result_df['oneshot_boolean'].progress_apply(decide)
        pd.to_pickle(result_df, result["location"])

    return results

def extract_clone_features_from_response(response):
    yes_pattern = re.compile(r'\byes\b', re.IGNORECASE)
    no_pattern = re.compile(r'\bno\b', re.IGNORECASE)
    type_patterns = {}
    for type in ['1','2','3','4']:
        type_patterns[type] = re.compile(rf'\b(type {type}|type-{type}|t{type})\b', re.IGNORECASE)
    clone_pattern = re.compile(r'\b(are clones|are code clones)\b',  re.IGNORECASE)
    not_clone_pattern = re.compile(r"\b(are not clones|aren't clones|are not code clones|aren't code clones)\b",  re.IGNORECASE)
    
    features = {}
    features['yes'] = bool(yes_pattern.search(response))
    features['no'] = bool(no_pattern.search(response))
    features['clone'] = bool(clone_pattern.search(response))
    features['not_clone'] = bool(not_clone_pattern.search(response))
    features['1'] = bool(type_patterns['1'].search(response))
    features['2'] = bool(type_patterns['2'].search(response))
    features['3'] = bool(type_patterns['3'].search(response))
    features['4'] = bool(type_patterns['4'].search(response))
    
    feature_list = []
    if features['yes']:
        feature_list.append('yes')
    elif features['no']:
        feature_list.append('no')
    
    for type in ['1','2','3','4']:
        if features[type]:
            feature_list.append('t'+type)
    
    return feature_list

def get_files(experiments, representations, models, temperatures, prompts):
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

def get_exp1_boolean_recall(results):
    for result in results:
        result_df = pd.read_pickle(result["location"])

        boolean_result = result_df["oneshot_boolean_label"]

        true_positives = boolean_result[boolean_result == 1].count()
        false_negatives = boolean_result[boolean_result == 0].count()

        result["recall"] = true_positives / (true_positives + false_negatives)
        result["discarded"] = int(boolean_result[boolean_result == 2].count())
        result["total"] = int(len(boolean_result))

    return results

def plot_roc_curve(results, output_dir):
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

def get_roc_exp1_boolean(results, save=False):
    discarded_df_list = []
    kept_df_list = []
    discarded_count = 0
    kept_count = 0

    for result in results:
        result[f'threshold_f1'] = []
        result_df = pd.read_pickle(result["location"])

        result_df['groundtruth'] = result_df['response'].apply(get_groundtruth_label_boolean)

        discarded_df = result_df[result_df['groundtruth'].isna()]
        kept_df = result_df[result_df['groundtruth'].notna()]

        discarded_df_list.append(discarded_df)

        result["discarded"] = len(discarded_df)
        result["kept"] = len(kept_df)

        discarded_count += len(discarded_df)
        kept_count += len(kept_df)

        result_df.dropna(subset=['groundtruth'], inplace=True)

        for threshold in np.arange(0, 1.0001, 0.1):
            threshold = round(threshold, 2)
            logging.info(f"Threshold: {threshold}, Result: {result['uid']}")

            result_df[f'threshold_{threshold}_label'] = result_df['oneshot_boolean'].apply(lambda x: decide_boolean(x, threshold))

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

    for threshold in np.arange(0, 1.0001, 0.1):
        threshold = round(threshold, 2)
        threshold_label = f'threshold_{threshold}_label'
        
        kept_df_all["groundtruth"] = kept_df_all["groundtruth"].astype(int)
        kept_df_all[threshold_label] = kept_df_all[threshold_label].astype(int)

        f1 = f1_score(kept_df_all['groundtruth'], kept_df_all[threshold_label])

        thresholds.append(threshold)
        f1_scores.append(f1)

        logging.info(f"discarded: {discarded_count}, kept: {kept_count}, f1: {f1}, threshold: {threshold}")

    plt.figure()
    plt.plot(thresholds, f1_scores, marker='o')
    plt.xlabel('Threshold')
    plt.ylabel('F1 Score')
    plt.title(f'F1 Score vs Threshold for {result["uid"]}')
    plt.grid(True)

    output_path = os.path.join(TMP_PLOTS_DIR, f'ALL_f1_vs_threshold.png')
    plt.savefig(output_path)
    plt.close()

    if save:
        with open(TMP_RESULTS_JSON_FILE, 'w') as file:
            json.dump(results, file)

    return results

def get_results_json(experiments, representations, models, temperatures, prompts):
    if os.path.exists(TMP_RESULTS_JSON_FILE):
        with open(TMP_RESULTS_JSON_FILE, 'r') as file:
            results = json.load(file)

        return results

    results = get_files(experiments, representations, models, temperatures, prompts)
    results = get_oneshot_labels(results)
    results = get_boolean_labels(results)

    results = get_exp1_boolean_recall(results)

    plot_recall(results, "recall.png")

    with open(TMP_RESULTS_JSON_FILE, 'w') as file:
        json.dump(results, file)

    return results

def get_exp1_boolean_cohen_model(results, tool_patches, models):
    prompts_uids = list(set(item["prompt"] for item in results))
    model_pairs = list(combinations(range(len(models)), 2))
    cohens = []

    for pair in model_pairs:
        model_1_index, model_2_index = pair
        model_1, model_2 = models[model_1_index], models[model_2_index]

        model_1_results = get_objects_by_feature(results, "model", model_1["uid"])
        model_2_results = get_objects_by_feature(results, "model", model_2["uid"])

        values_1 = []
        values_2 = []

        for prompt_uid in prompts_uids:
            model_1_result = get_object_by_unique_feature(model_1_results, "prompt", prompt_uid)
            model_1_result_df = pd.read_pickle(model_1_result["location"])
            model_1_result_labels = model_1_result_df.progress_apply(get_classification_boolean, axis=1)

            model_2_result = get_object_by_unique_feature(model_2_results, "prompt", prompt_uid)
            model_2_result_df = pd.read_pickle(model_2_result["location"])
            model_2_result_labels = model_2_result_df.progress_apply(get_classification_boolean, axis=1)

            values_1.append(model_1_result_labels.values)
            values_2.append(model_2_result_labels.values)

        kappa = cohen_kappa_score(np.concatenate(values_1), np.concatenate(values_2))

        cohen = {
            "uids": [model_1["uid"], model_2["uid"]],
            "kappa": kappa
        }

        cohens.append(cohen)

    return cohens

def get_exp1_boolean_cohen_prompt(results, tool_patches, models):
    prompts_uids = list(set(item["prompt"] for item in results))
    prompt_pairs = list(combinations(range(len(prompts_uids)), 2))
    cohens = []

    for pair in prompt_pairs:
        prompt_1_index, prompt_2_index = pair
        prompt_uid_1, prompt_uid_2 = prompts_uids[prompt_1_index], prompts_uids[prompt_2_index]

        prompt_1_results = get_objects_by_feature(results, "prompt", prompt_uid_1)
        prompt_2_results = get_objects_by_feature(results, "prompt", prompt_uid_2)

        values_1 = []
        values_2 = []

        for model in models:
            prompt_1_result = get_object_by_unique_feature(prompt_1_results, "model", model["uid"])
            prompt_1_result_df = pd.read_pickle(prompt_1_result["location"])
            prompt_1_result_labels = prompt_1_result_df.progress_apply(get_classification_boolean, axis=1)

            prompt_2_result = get_object_by_unique_feature(prompt_2_results, "model", model["uid"])
            prompt_2_result_df = pd.read_pickle(prompt_2_result["location"])
            prompt_2_result_labels = prompt_2_result_df.progress_apply(get_classification_boolean, axis=1)

            values_1.append(prompt_1_result_labels.values)
            values_2.append(prompt_2_result_labels.values)

        kappa = cohen_kappa_score(np.concatenate(values_1), np.concatenate(values_2))

        cohen = {
            "uids": [prompt_uid_1, prompt_uid_2],
            "kappa": kappa
        }

        cohens.append(cohen)

    return cohens
            
def plot_cohen(json_file, output_file):
    # JSON data
    with open(os.path.join(TMP_PLOTS_DIR, json_file)) as file:
        kappa_values = json.load(file)

    unique_uids = set()
    for item in kappa_values:
        unique_uids.update(item["uids"])

    # Convert to list
    labels = list(unique_uids)

    # Create the dictionary
    kappa_values = {(tuple(item["uids"])): item["kappa"] for item in kappa_values}

    # Initialize the matrix with zeros
    matrix = np.zeros((len(labels), len(labels)))

    # Fill the matrix with the kappa values
    for i, model1 in enumerate(labels):
        for j, model2 in enumerate(labels):
            if i != j:
                kappa = kappa_values.get((model1, model2), kappa_values.get((model2, model1), 0))
                matrix[i, j] = kappa

    # Set diagonal values to 1
    np.fill_diagonal(matrix, 1)

    # Create a custom colormap with balanced shades
    cmap = LinearSegmentedColormap.from_list("balanced_red_blue", ["#ff9999", "#9999ff"], N=256)

    # Create the heatmap with increased width
    plt.figure(figsize=(12, 4))  # Adjust width and height as needed
    ax = sns.heatmap(matrix, annot=True, xticklabels=labels, yticklabels=labels, cmap=cmap, vmin=-1, vmax=1)

    # Set column labels at the top
    ax.set_xticklabels(ax.get_xticklabels(), rotation=45, ha='center')
    ax.set_yticklabels(ax.get_yticklabels(), rotation=0)

    # Move x-axis labels to the top
    ax.xaxis.set_label_position('top')
    ax.xaxis.tick_top()

    fp = os.path.join(TMP_PLOTS_DIR, output_file)
    # Adjust layout and save the heatmap to a file
    plt.tight_layout()
    plt.savefig(fp, bbox_inches='tight')
    plt.close()

def plot_recall(data, output_file):
    # Create a DataFrame
    df = pd.DataFrame(data)

    # Pivot the DataFrame for recall, discarded, and total values
    heatmap_data_recall = df.pivot(index="prompt", columns="model", values="recall")
    heatmap_data_discarded = df.pivot(index="prompt", columns="model", values="discarded")
    heatmap_data_total = df.pivot(index="prompt", columns="model", values="total")

    # Calculate discarded ratio as discarded / total
    heatmap_data_discarded_ratio = (heatmap_data_discarded / heatmap_data_total).round(2)

    # Round recall values to 2 decimal places
    heatmap_data_recall_rounded = heatmap_data_recall.round(2)

    # Calculate row-wise and column-wise averages for recall and discarded ratio
    avg_recall_row = heatmap_data_recall.mean(axis=1).round(2)  # Average recall per prompt
    avg_recall_col = heatmap_data_recall.mean(axis=0).round(2)  # Average recall per model

    avg_discarded_row = heatmap_data_discarded_ratio.mean(axis=1).round(2)  # Average discard ratio per prompt
    avg_discarded_col = heatmap_data_discarded_ratio.mean(axis=0).round(2)  # Average discard ratio per model

    # Append the average values to the data
    heatmap_data_recall_rounded['Average'] = avg_recall_row  # Add row average for recall
    heatmap_data_recall_rounded.loc['Average'] = avg_recall_col.tolist() + [avg_recall_col.mean().round(2)]  # Add column average for recall

    heatmap_data_discarded_ratio['Average'] = avg_discarded_row  # Add row average for discarded ratio
    heatmap_data_discarded_ratio.loc['Average'] = avg_discarded_col.tolist() + [avg_discarded_col.mean().round(2)]  # Add column average for discarded ratio

    # Create a custom colormap with balanced shades
    cmap = LinearSegmentedColormap.from_list("balanced_red_blue", ["#ff9999", "#9999ff"], N=256)

    # Create custom annotations showing recall and discarded ratio (rounded to 2 decimal places)
    annot_data = heatmap_data_recall_rounded.astype(str) + "\n" + heatmap_data_discarded_ratio.astype(str)

    # Adjust figsize to increase cell height
    plt.figure(figsize=(12, len(df['prompt'].unique()) * 0.5))  # Adjust height dynamically based on the number of rows
    ax = sns.heatmap(heatmap_data_recall_rounded, annot=annot_data, cmap=cmap, cbar_kws={'label': 'Recall'}, vmin=0, vmax=1, fmt="")

    # Set the x-axis labels at the top
    ax.set_xticklabels(ax.get_xticklabels(), rotation=45, ha='center')
    ax.set_yticklabels(ax.get_yticklabels(), rotation=0)

    # Move x-axis labels to the top
    ax.xaxis.set_label_position('top')
    ax.xaxis.tick_top()

    fp = os.path.join(TMP_PLOTS_DIR, output_file)

    # Adjust layout and save the heatmap
    plt.tight_layout()
    plt.savefig(fp, dpi=300, bbox_inches='tight')

    # Show the plot (optional)
    plt.close()

def get_cohen_plot(results, developer_patches, models):
    ## Calculate Cohen's Kappa for models
    cohens = get_exp1_boolean_cohen_model(results, developer_patches, models)

    ## Save JSON and PNG
    json_file = os.path.join(TMP_PLOTS_DIR, "cohen_model.json")
    output_file = "cohen_model.png"

    with open(json_file, 'w') as file:
        json.dump(cohens, file)

    plot_cohen(json_file, output_file)

    ## Calculate Cohen's Kappa for prompts
    cohens = get_exp1_boolean_cohen_prompt(results, developer_patches, models)

    ## Save JSON and PNG
    json_file = os.path.join(TMP_PLOTS_DIR, "cohen_prompts.json")
    output_file = "cohen_prompts.png"

    with open(json_file, 'w') as file:
        json.dump(cohens, file)

    plot_cohen(json_file, output_file)

def get_groundtruth_label_boolean(response):
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

def evaluate_classification(results):
    dataframes_list = []
    discarded_dataframes_list = []
    none_count = 0
    kept_count = 0

    for result in results:
        df = pd.read_pickle(result["location"])
        df['groundtruth'] = df['response'].apply(get_groundtruth_label_boolean)
        none_count += df['groundtruth'].isna().sum()
        kept_count += df['groundtruth'].notna().sum()

        discarded_df = df[df['groundtruth'].isna()]
        df = df[df['groundtruth'].notna()]
        
        discarded_dataframes_list.append(discarded_df)
        dataframes_list.append(df)

    kept_df = pd.concat(dataframes_list, ignore_index=True) # Trivial Kept
    filtered_df = kept_df[kept_df['oneshot_boolean_label'] != 2] # Trivial Kept && OneShot Kept
    oneshot_discarded_df = kept_df[kept_df['oneshot_boolean_label'] == 2] # Trivial Kept && OneShot Discarded

    discarded_df = pd.concat(discarded_dataframes_list, ignore_index=True) # Trivial Discarded

    precision = precision_score(filtered_df['groundtruth'], filtered_df['oneshot_boolean_label'])
    recall = recall_score(filtered_df['groundtruth'], filtered_df['oneshot_boolean_label'])
    accuracy = accuracy_score(filtered_df['groundtruth'], filtered_df['oneshot_boolean_label'])

    return none_count, kept_count, precision, recall, accuracy, kept_df, discarded_df

def sample_records(dataframe):
    # Group the dataframe by 'model' and 'prompt'
    grouped = dataframe.groupby(['model', 'prompt'])
    
    # Sample three records from each group
    sampled_df = grouped.apply(lambda x: x.sample(n=3, replace=False) if len(x) >= 3 else x)
    
    # Reset the index to clean up the dataframe
    sampled_df.reset_index(drop=True, inplace=True)
    
    return sampled_df

def sample_records_stratified(dataframe, total_samples):
    # Calculate the total number of records
    total_records = len(dataframe)

    # Group the dataframe by 'model' and 'prompt'
    grouped = dataframe.groupby(['model', 'prompt'])

    # Calculate the sample size for each group proportional to its size
    def get_samples(group):
        group_size = len(group)
        samples_to_take = max(1, int((group_size / total_records) * total_samples))
        return group.sample(n=samples_to_take, replace=False)

    # Apply the sampling function to each group
    sampled_df = grouped.apply(get_samples)
    
    # Reset the index to clean up the dataframe
    sampled_df.reset_index(drop=True, inplace=True)
    
    return sampled_df

def get_htmls(results):
    for result in results:
        df = pd.read_pickle(result["location"])
        df.to_html(result["location"].replace(".pkl", ".html"))

def get_mock_bar_plot():
    # Sample data for the barchart
    prompts = [f'P{i}' for i in range(1, 11)]  # 10 prompts P1 to P10
    fleiss_kappa_values = np.random.uniform(0, 1, size=10)  # Random kappa values between 0 and 1 for illustration

    # Create a bar chart
    plt.figure(figsize=(10, 6))

    for i, value in enumerate(fleiss_kappa_values):
        # Define baby colors for segments
        if value <= 0.2:
            plt.bar(prompts[i], value, color='lightblue')  # Light blue for low values
        else:
            plt.bar(prompts[i], 0.2, color='lightblue')  # Base part in light blue
            if value <= 0.4:
                plt.bar(prompts[i], value - 0.2, bottom=0.2, color='lightgreen')  # Baby green for fair agreement
            elif value <= 0.6:
                plt.bar(prompts[i], 0.2, color='lightgreen', bottom=0.2)  # Fair agreement
                plt.bar(prompts[i], value - 0.4, bottom=0.4, color='yellow')  # Baby yellow for moderate agreement
            elif value <= 0.8:
                plt.bar(prompts[i], 0.2, color='lightgreen', bottom=0.2)  # Fair agreement
                plt.bar(prompts[i], 0.2, color='yellow', bottom=0.4)  # Moderate agreement
                plt.bar(prompts[i], value - 0.6, bottom=0.6, color='orange')  # Baby orange for substantial agreement
            else:
                plt.bar(prompts[i], 0.2, color='lightgreen', bottom=0.2)  # Fair agreement
                plt.bar(prompts[i], 0.2, color='yellow', bottom=0.4)  # Moderate agreement
                plt.bar(prompts[i], 0.2, color='orange', bottom=0.6)  # Substantial agreement
                plt.bar(prompts[i], value - 0.8, bottom=0.8, color='red')  # Baby red for great agreement

    # Add labels and title
    plt.xlabel('Prompts')
    plt.ylabel('Fleiss Kappa Value')
    plt.title('Fleiss Kappa Values for Different Prompts')
    plt.ylim(0, 1)  # Since kappa values range between 0 and 1

    # Save the figure
    plt.savefig(os.path.join(TMP_PLOTS_DIR, 'fleiss_kappa_values_baby_colors.png'))  # Change filename and format as needed
    plt.close()

if __name__ == "__main__":
    # init
    bugs, developer_patches, tool_patches = init(configure=False)
    experiments = [
        {"uid": "EXP1"}
    ]
    representations = get_patch_processors()
    prompts, models, temperatures = get_tool_settings() 

    results = get_result_files(experiments, representations, models, temperatures, prompts)
    kept_df, discarded_df = get_all_results_with_groundtruth(results)


    results = get_roc_exp1_boolean(results, save=True)
    plot_roc_curve(results, os.path.join(TMP_PLOTS_DIR, "roc"))
    none_count, kept_count, precision, recall, accuracy, kept_df, discarded_df = evaluate_classification(results)


    '''
    # Explore results folder
    results = get_results_json(experiments, representations, models, temperatures, prompts)

    logging.info(f"None count: {none_count}, Kept count: {kept_count}, Precision: {precision}, Recall: {recall}, Accuracy: {accuracy}, None count: {len(kept_df)}, Kept count: {len(discarded_df)}")
    
    sample_df = sample_records_stratified(discarded_df, 1000)
    sample_df.to_html("sample.html")
    '''

    # # Save as HTML
    # get_htmls(results)

    # classification_metrics = evaluate_classification(results)
    # logging.info(classification_metrics)

    # # Calculate and plot Cohen's Kappa for models and prompts
    # get_cohen_plot(results, tool_patches, models)
    
    # # get_mock_bar_plot()





"""