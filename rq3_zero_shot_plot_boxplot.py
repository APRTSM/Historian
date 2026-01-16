import os
import logging
import re
import json
import pandas as pd
from transformers import pipeline
import matplotlib.pyplot as plt
import numpy as np
import seaborn as sns
from tqdm import tqdm

# Setup logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Import from utils.config - we'll define fallbacks if not available
try:
    from utils.config import TMP_RESULTS_DIR, TMP_CLASSIFICATION_RESULTS_DIR
except ImportError:
    TMP_RESULTS_DIR = "./tmp/results"
    TMP_CLASSIFICATION_RESULTS_DIR = "./tmp/classification_results"

# Output directory
OUTPUT_DIR = "./rq3"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Configuration
SELECTED_TOOL = "tbar"
TEMPERATURE_UID = "0.1"

INPUT_MODELS = [
    "magicoder:7b-s-cl",
    "codellama:7b-instruct",
    "deepseek-coder:6.7b",
    "codegemma:7b-instruct",
    "qwen2.5:7b",
    "qwen2.5-coder:7b",
    "yi-coder:9b",
    "hermes3:8b"
]

# Yes/No prompts
YES_NO_PROMPTS_METHOD = [
    "llm4cc-simple_prompt-semantical",
    "llm4cc-reasoning-patch-semantical",
    "llm4cc-similarity_line-patch-semantical",
    "llm4cc-simple_prompt-identical",
    "llm4cc-reasoning-patch-identical",
    "llm4cc-similarity_line-patch-identical"
]

YES_NO_PROMPTS_DIFF = [
    "llm4cc-simple_prompt-semantical",
    "llm4cc-reasoning-patch-semantical",
    "llm4cc-similarity_line-patch-semantical",
    "llm4cc-simple_prompt-identical",
    "llm4cc-reasoning-patch-identical",
    "llm4cc-similarity_line-patch-identical"
]

# Clone type prompts
CLONE_TYPE_PROMPTS_METHOD = [
    "llm4cc-clone_type",
    "llm4cc-integrated"
]

CLONE_TYPE_PROMPTS_DIFF = [
    "llm4cc-clone_type-patch",
    "llm4cc-integrated-patch"
]

PROCESSORS = {
    "method": "method",
    "diff": "defaultpatch"
}

YES_NO_LABELS = ["yes", "no"]
CLONE_TYPE_LABELS = ["type-1", "type-2", "type-3", "type-4", "not-clone"]


def extract_label_regex(text, valid_labels):
    """Extract label using regex - same as in original code"""
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


def get_zero_shot_probabilities(pipe, texts, labels, ground_truth_labels):
    """
    Get zero-shot classification probabilities for ground truth labels
    
    Args:
        pipe: The zero-shot classification pipeline
        texts: List of texts to classify
        labels: List of candidate labels
        ground_truth_labels: List of ground truth labels (from regex)
    
    Returns:
        List of probabilities for the ground truth label
    """
    probabilities = []
    
    for text, gt_label in tqdm(zip(texts, ground_truth_labels), total=len(texts), desc="Getting probabilities"):
        if pd.isna(text) or gt_label is None:
            probabilities.append(None)
            continue
            
        result = pipe(str(text), candidate_labels=labels)
        
        # Find probability of ground truth label
        label_to_score = dict(zip(result["labels"], result["scores"]))
        prob = label_to_score.get(gt_label, 0.0)
        probabilities.append(prob)
    
    return probabilities


def collect_data_for_violin_plots(pipe, processor_uid, prompts, labels, label_type):
    """
    Collect probability data for violin plots
    
    Args:
        pipe: Zero-shot classification pipeline
        processor_uid: "method" or "defaultpatch"
        prompts: List of prompt UIDs
        labels: List of labels (yes/no or clone types)
        label_type: "yes_no" or "clone_type"
    
    Returns:
        DataFrame with columns: ground_truth, probability, model, prompt
    """
    all_data = []
    
    for model in INPUT_MODELS:
        for prompt in prompts:
            file_name = f"EXP2-{SELECTED_TOOL}-{processor_uid}-{model}-{TEMPERATURE_UID}-{prompt}.pkl"
            result_file = os.path.join(TMP_RESULTS_DIR, file_name)
            
            if not os.path.exists(result_file):
                logging.warning(f"File not found: {result_file}")
                continue
            
            logging.info(f"Processing: {file_name}")
            
            try:
                df = pd.read_pickle(result_file)
            except Exception as e:
                logging.error(f"Error loading {result_file}: {e}")
                continue
            
            # Apply regex to find ground truth labels
            df['regex_label'] = df['response'].apply(lambda x: extract_label_regex(x, labels))
            
            # Filter to only regex-detectable responses
            regex_df = df[df['regex_label'].notna()].copy()
            
            if len(regex_df) == 0:
                logging.warning(f"No regex-detectable responses for {file_name}")
                continue
            
            logging.info(f"Found {len(regex_df)} regex-detectable responses out of {len(df)}")
            
            # Get zero-shot probabilities for ground truth labels
            probabilities = get_zero_shot_probabilities(
                pipe,
                regex_df['response'].tolist(),
                labels,
                regex_df['regex_label'].tolist()
            )
            
            # Add to collected data
            for gt_label, prob in zip(regex_df['regex_label'].tolist(), probabilities):
                if prob is not None:
                    all_data.append({
                        'ground_truth': gt_label,
                        'probability': prob,
                        'model': model,
                        'prompt': prompt,
                        'processor': processor_uid
                    })
    
    return pd.DataFrame(all_data)


def create_violin_plot_yes_no(data, output_filename, title):
    """Create violin plot for yes/no classification"""
    if len(data) == 0:
        logging.warning(f"No data for {output_filename}")
        return
    
    fig, ax = plt.subplots(figsize=(8, 6))
    
    # Create violin plot
    sns.violinplot(x='ground_truth', y='probability', data=data, 
                   order=['no', 'yes'], ax=ax, palette='Set2')
    
    ax.set_xlabel('Ground Truth (Regex Label)', fontsize=12)
    ax.set_ylabel('Probability of Ground Truth Label', fontsize=12)
    ax.set_title(title, fontsize=14)
    
    # Remove top and right spines
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
    # Add horizontal line at 0.5
    ax.axhline(y=0.5, color='gray', linestyle='--', alpha=0.5)
    
    plt.tight_layout()
    plt.savefig(os.path.join(OUTPUT_DIR, output_filename), dpi=300, bbox_inches='tight')
    plt.close()
    
    logging.info(f"Saved: {output_filename}")


def create_violin_plot_clone_type(data, output_filename, title):
    """Create violin plot for clone type classification"""
    if len(data) == 0:
        logging.warning(f"No data for {output_filename}")
        return
    
    fig, ax = plt.subplots(figsize=(12, 6))
    
    # Order for clone types
    order = ['not-clone', 'type-1', 'type-2', 'type-3', 'type-4']
    
    # Filter to only existing labels
    existing_labels = [l for l in order if l in data['ground_truth'].unique()]
    
    # Create violin plot
    sns.violinplot(x='ground_truth', y='probability', data=data, 
                   order=existing_labels, ax=ax, palette='Set3')
    
    ax.set_xlabel('Ground Truth (Regex Label)', fontsize=12)
    ax.set_ylabel('Probability of Ground Truth Label', fontsize=12)
    ax.set_title(title, fontsize=14)
    
    # Remove top and right spines
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
    # Add horizontal line at 0.2 (1/5 for 5 classes)
    ax.axhline(y=0.2, color='gray', linestyle='--', alpha=0.5)
    
    plt.tight_layout()
    plt.savefig(os.path.join(OUTPUT_DIR, output_filename), dpi=300, bbox_inches='tight')
    plt.close()
    
    logging.info(f"Saved: {output_filename}")


def create_combined_violin_plot(data_method, data_diff, labels_type, output_filename, title):
    """Create combined violin plot comparing method and diff representations"""
    if len(data_method) == 0 and len(data_diff) == 0:
        logging.warning(f"No data for {output_filename}")
        return
    
    # Add representation column
    data_method = data_method.copy()
    data_diff = data_diff.copy()
    data_method['representation'] = 'Method'
    data_diff['representation'] = 'Diff'
    
    combined = pd.concat([data_method, data_diff], ignore_index=True)
    
    if labels_type == 'yes_no':
        order = ['no', 'yes']
        fig, ax = plt.subplots(figsize=(10, 6))
    else:
        order = ['not-clone', 'type-1', 'type-2', 'type-3', 'type-4']
        order = [l for l in order if l in combined['ground_truth'].unique()]
        fig, ax = plt.subplots(figsize=(14, 6))
    
    # Create violin plot with hue
    sns.violinplot(x='ground_truth', y='probability', hue='representation', 
                   data=combined, order=order, ax=ax, palette='Set2', split=True)
    
    ax.set_xlabel('Ground Truth (Regex Label)', fontsize=12)
    ax.set_ylabel('Probability of Ground Truth Label', fontsize=12)
    ax.set_title(title, fontsize=14)
    
    # Remove top and right spines
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
    # Add horizontal line
    if labels_type == 'yes_no':
        ax.axhline(y=0.5, color='gray', linestyle='--', alpha=0.5)
    else:
        ax.axhline(y=0.2, color='gray', linestyle='--', alpha=0.5)
    
    ax.legend(title='Representation', loc='lower right')
    
    plt.tight_layout()
    plt.savefig(os.path.join(OUTPUT_DIR, output_filename), dpi=300, bbox_inches='tight')
    plt.close()
    
    logging.info(f"Saved: {output_filename}")


def save_statistics(data, filename_prefix):
    """Save summary statistics"""
    if len(data) == 0:
        return
    
    stats = data.groupby('ground_truth')['probability'].agg(['mean', 'std', 'median', 'count'])
    stats.to_csv(os.path.join(OUTPUT_DIR, f"{filename_prefix}_stats.csv"))
    
    logging.info(f"Statistics for {filename_prefix}:")
    logging.info(stats)


def main():
    logging.info("Initializing zero-shot classification pipeline...")
    pipe = pipeline(model="facebook/bart-large-mnli", device=0)
    
    # Collect data for Yes/No - Method representation
    logging.info("=" * 50)
    logging.info("Processing Yes/No prompts - Method representation")
    logging.info("=" * 50)
    data_yes_no_method = collect_data_for_violin_plots(
        pipe, "method", YES_NO_PROMPTS_METHOD, YES_NO_LABELS, "yes_no"
    )
    save_statistics(data_yes_no_method, "yes_no_method")
    data_yes_no_method.to_pickle(os.path.join(OUTPUT_DIR, "data_yes_no_method.pkl"))
    
    # Collect data for Yes/No - Diff representation
    logging.info("=" * 50)
    logging.info("Processing Yes/No prompts - Diff representation")
    logging.info("=" * 50)
    data_yes_no_diff = collect_data_for_violin_plots(
        pipe, "defaultpatch", YES_NO_PROMPTS_DIFF, YES_NO_LABELS, "yes_no"
    )
    save_statistics(data_yes_no_diff, "yes_no_diff")
    data_yes_no_diff.to_pickle(os.path.join(OUTPUT_DIR, "data_yes_no_diff.pkl"))
    
    # Collect data for Clone Type - Method representation
    logging.info("=" * 50)
    logging.info("Processing Clone Type prompts - Method representation")
    logging.info("=" * 50)
    data_clone_method = collect_data_for_violin_plots(
        pipe, "method", CLONE_TYPE_PROMPTS_METHOD, CLONE_TYPE_LABELS, "clone_type"
    )
    save_statistics(data_clone_method, "clone_type_method")
    data_clone_method.to_pickle(os.path.join(OUTPUT_DIR, "data_clone_type_method.pkl"))
    
    # Collect data for Clone Type - Diff representation  
    logging.info("=" * 50)
    logging.info("Processing Clone Type prompts - Diff representation")
    logging.info("=" * 50)
    data_clone_diff = collect_data_for_violin_plots(
        pipe, "defaultpatch", CLONE_TYPE_PROMPTS_DIFF, CLONE_TYPE_LABELS, "clone_type"
    )
    save_statistics(data_clone_diff, "clone_type_diff")
    data_clone_diff.to_pickle(os.path.join(OUTPUT_DIR, "data_clone_type_diff.pkl"))
    
    # Create individual violin plots
    logging.info("=" * 50)
    logging.info("Creating violin plots")
    logging.info("=" * 50)
    
    create_violin_plot_yes_no(
        data_yes_no_method, 
        "violin_yes_no_method.png",
        "Zero-Shot Probability Distribution (Yes/No) - Method Representation"
    )
    
    create_violin_plot_yes_no(
        data_yes_no_diff,
        "violin_yes_no_diff.png", 
        "Zero-Shot Probability Distribution (Yes/No) - Diff Representation"
    )
    
    create_violin_plot_clone_type(
        data_clone_method,
        "violin_clone_type_method.png",
        "Zero-Shot Probability Distribution (Clone Types) - Method Representation"
    )
    
    create_violin_plot_clone_type(
        data_clone_diff,
        "violin_clone_type_diff.png",
        "Zero-Shot Probability Distribution (Clone Types) - Diff Representation"
    )
    
    # Create combined plots
    create_combined_violin_plot(
        data_yes_no_method, data_yes_no_diff, 'yes_no',
        "violin_yes_no_combined.png",
        "Zero-Shot Probability Distribution (Yes/No) - Method vs Diff"
    )
    
    create_combined_violin_plot(
        data_clone_method, data_clone_diff, 'clone_type',
        "violin_clone_type_combined.png",
        "Zero-Shot Probability Distribution (Clone Types) - Method vs Diff"
    )
    
    logging.info("=" * 50)
    logging.info("All plots generated successfully!")
    logging.info(f"Output saved to: {OUTPUT_DIR}")
    logging.info("=" * 50)


if __name__ == "__main__":
    main()