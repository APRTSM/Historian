import os
import re
import pandas as pd
import numpy as np
from tqdm import tqdm
from transformers import pipeline

# Configuration
SELECTED_TOOL = "tbar"
TEMPERATURE_UID = "0.1"
TMP_RESULTS_DIR = "./tmp/results"  # Adjust this path
OUTPUT_DIR = "./rq3"
os.makedirs(OUTPUT_DIR, exist_ok=True)

SAMPLES_PER_FILE = 20

# Models
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

# All file configurations as a dict
FILE_CONFIGS = {
    "yes_no_method": {
        "processor": "method",
        "prompts": [
            "llm4cc-simple_prompt-semantical",
            "llm4cc-reasoning-patch-semantical",
            "llm4cc-similarity_line-patch-semantical",
            "llm4cc-simple_prompt-identical",
            "llm4cc-reasoning-patch-identical",
            "llm4cc-similarity_line-patch-identical"
        ],
        "labels": ["yes", "no"]
    },
    "yes_no_diff": {
        "processor": "defaultpatch",
        "prompts": [
            "llm4cc-simple_prompt-semantical",
            "llm4cc-reasoning-patch-semantical",
            "llm4cc-similarity_line-patch-semantical",
            "llm4cc-simple_prompt-identical",
            "llm4cc-reasoning-patch-identical",
            "llm4cc-similarity_line-patch-identical"
        ],
        "labels": ["yes", "no"]
    },
    "clone_type_method": {
        "processor": "method",
        "prompts": [
            "llm4cc-clone_type",
            "llm4cc-integrated"
        ],
        "labels": ["type-1", "type-2", "type-3", "type-4", "not-clone"]
    },
    "clone_type_diff": {
        "processor": "defaultpatch",
        "prompts": [
            "llm4cc-clone_type-patch",
            "llm4cc-integrated-patch"
        ],
        "labels": ["type-1", "type-2", "type-3", "type-4", "not-clone"]
    }
}

YES_NO_LABELS = ["yes", "no"]
CLONE_TYPE_LABELS = ["type-1", "type-2", "type-3", "type-4", "not-clone"]


def extract_label_regex(text, valid_labels):
    """Extract label using regex"""
    if pd.isna(text):
        return None
    
    text_lower = str(text).lower()
    found_labels = []
    
    for label in valid_labels:
        pattern = r'\b' + re.escape(label.lower()) + r'\b'
        if re.search(pattern, text_lower):
            found_labels.append(label)
    
    return found_labels[0] if len(found_labels) == 1 else None


def get_all_file_paths():
    """Generate all 128 file paths in order"""
    all_files = []
    
    for config_name, config in FILE_CONFIGS.items():
        processor = config["processor"]
        prompts = config["prompts"]
        labels = config["labels"]
        
        for model in INPUT_MODELS:
            for prompt in prompts:
                file_name = f"EXP2-{SELECTED_TOOL}-{processor}-{model}-{TEMPERATURE_UID}-{prompt}.pkl"
                all_files.append({
                    "file_name": file_name,
                    "file_path": os.path.join(TMP_RESULTS_DIR, file_name),
                    "config": config_name,
                    "processor": processor,
                    "model": model,
                    "prompt": prompt,
                    "labels": labels
                })
    
    return all_files


def sample_non_regex_from_file(file_info, n_samples=20):
    """
    Sample n non-regex-detectable responses from a file
    
    Returns:
        tuple: (samples_df, status, count)
    """
    file_path = file_info["file_path"]
    labels = file_info["labels"]
    
    if not os.path.exists(file_path):
        return pd.DataFrame(), 'not_found', 0
    
    try:
        df = pd.read_pickle(file_path)
    except Exception as e:
        return pd.DataFrame(), 'error', 0
    
    df['regex_label'] = df['response'].apply(lambda x: extract_label_regex(x, labels))
    non_regex_df = df[df['regex_label'].isna()].copy()
    
    available = len(non_regex_df)
    
    if available == 0:
        return pd.DataFrame(), 'empty', 0
    
    status = 'insufficient' if available < n_samples else 'ok'
    
    sample_size = min(n_samples, available)
    sampled = non_regex_df.sample(n=sample_size, random_state=42).copy()
    
    # Add metadata
    sampled['source_file'] = file_info['file_name']
    sampled['config'] = file_info['config']
    sampled['processor'] = file_info['processor']
    sampled['model'] = file_info['model']
    sampled['prompt'] = file_info['prompt']
    sampled['valid_labels'] = str(labels)
    sampled['manual_label'] = ''
    
    return sampled, status, available


def create_interleaved_df(all_samples_by_file):
    """
    Create DataFrame with interleaved samples
    """
    non_empty = [s for s in all_samples_by_file if len(s) > 0]
    
    if not non_empty:
        return pd.DataFrame()
    
    max_samples = max(len(samples) for samples in non_empty)
    
    interleaved_rows = []
    
    for sample_idx in range(max_samples):
        for file_idx, samples in enumerate(all_samples_by_file):
            if len(samples) > 0 and sample_idx < len(samples):
                row = samples.iloc[sample_idx].to_dict()
                row['file_index'] = file_idx + 1
                row['sample_index_in_file'] = sample_idx + 1
                interleaved_rows.append(row)
    
    return pd.DataFrame(interleaved_rows)


def apply_zeroshot_classification(df, pipe):
    """
    Apply zero-shot classification to all rows in dataframe
    """
    zeroshot_labels = []
    zeroshot_scores = []
    zeroshot_all_scores = []
    
    for idx, row in tqdm(df.iterrows(), total=len(df), desc="Zero-shot classifying"):
        text = row['response']
        
        # Determine which labels to use
        if row['config'] in ['yes_no_method', 'yes_no_diff']:
            labels = YES_NO_LABELS
        else:
            labels = CLONE_TYPE_LABELS
        
        if pd.isna(text):
            zeroshot_labels.append(None)
            zeroshot_scores.append(None)
            zeroshot_all_scores.append(None)
            continue
        
        try:
            result = pipe(str(text), candidate_labels=labels)
            zeroshot_labels.append(result["labels"][0])
            zeroshot_scores.append(result["scores"][0])
            zeroshot_all_scores.append(dict(zip(result["labels"], result["scores"])))
        except Exception as e:
            zeroshot_labels.append(None)
            zeroshot_scores.append(None)
            zeroshot_all_scores.append(None)
    
    df['zeroshot_label'] = zeroshot_labels
    df['zeroshot_score'] = zeroshot_scores
    df['zeroshot_all_scores'] = zeroshot_all_scores
    
    return df


def reorder_columns(df):
    """Reorder columns to put important ones first"""
    important_cols = ['patch_uid', 'uid', 'response', 'zeroshot_label', 'zeroshot_score',
                      'source_file', 'config', 'processor', 'model', 'prompt',
                      'valid_labels', 'manual_label', 'file_index', 'sample_index_in_file']
    
    existing_important = [c for c in important_cols if c in df.columns]
    other_cols = [c for c in df.columns if c not in important_cols]
    
    return df[existing_important + other_cols]


def main():
    print("=" * 60)
    print("STEP 1: Stratified Sampling of Non-Regex Responses")
    print("=" * 60)
    
    all_files = get_all_file_paths()
    print(f"\nTotal files to process: {len(all_files)}")
    
    # Track issues
    files_not_found = []
    files_with_errors = []
    files_empty = []
    files_insufficient = []
    
    all_samples_by_file = []
    
    print("\nSampling from files...")
    for file_info in tqdm(all_files, desc="Sampling"):
        samples, status, available = sample_non_regex_from_file(file_info, n_samples=SAMPLES_PER_FILE)
        all_samples_by_file.append(samples)
        
        if status == 'not_found':
            files_not_found.append(file_info['file_name'])
        elif status == 'error':
            files_with_errors.append(file_info['file_name'])
        elif status == 'empty':
            files_empty.append(file_info['file_name'])
        elif status == 'insufficient':
            files_insufficient.append((file_info['file_name'], available))
    
    # Summary of sampling
    print("\n" + "-" * 40)
    print("Sampling Summary")
    print("-" * 40)
    
    total_samples = sum(len(s) for s in all_samples_by_file)
    print(f"Total samples collected: {total_samples}")
    print(f"Expected (if all files had {SAMPLES_PER_FILE}): {len(all_files) * SAMPLES_PER_FILE}")
    
    if files_not_found:
        print(f"\n❌ FILES NOT FOUND ({len(files_not_found)}):")
        for fname in files_not_found:
            print(f"   {fname}")
    
    if files_with_errors:
        print(f"\n⚠️ FILES WITH LOAD ERRORS ({len(files_with_errors)}):")
        for fname in files_with_errors:
            print(f"   {fname}")
    
    if files_empty:
        print(f"\n🔹 FILES WITH 0 NON-REGEX SAMPLES ({len(files_empty)}):")
        for fname in files_empty:
            print(f"   {fname}")
    
    if files_insufficient:
        print(f"\n🔸 FILES WITH < {SAMPLES_PER_FILE} NON-REGEX SAMPLES ({len(files_insufficient)}):")
        for fname, count in files_insufficient:
            print(f"   {fname}: {count} samples")
    
    # Create interleaved dataframe
    print("\n" + "=" * 60)
    print("STEP 2: Creating Interleaved DataFrame")
    print("=" * 60)
    
    result_df = create_interleaved_df(all_samples_by_file)
    print(f"Interleaved DataFrame created with {len(result_df)} rows")
    
    # Apply zero-shot classification
    print("\n" + "=" * 60)
    print("STEP 3: Zero-Shot Classification")
    print("=" * 60)
    
    print("\nInitializing pipeline...")
    pipe = pipeline(model="facebook/bart-large-mnli", device=0)
    print("Pipeline ready!\n")
    
    result_df = apply_zeroshot_classification(result_df, pipe)
    
    # Reorder columns
    result_df = reorder_columns(result_df)
    
    # Save interleaved CSV
    print("\n" + "=" * 60)
    print("STEP 4: Saving Results")
    print("=" * 60)
    
    output_csv = os.path.join(OUTPUT_DIR, "stratified_samples_interleaved.csv")
    result_df.to_csv(output_csv, index=False)
    print(f"\nSaved interleaved CSV: {output_csv}")
    print(f"Total rows: {len(result_df)}")
    
    # Also save grouped version
    concat_df = pd.concat([s for s in all_samples_by_file if len(s) > 0], ignore_index=True)
    concat_df = apply_zeroshot_classification(concat_df, pipe)
    concat_df = reorder_columns(concat_df)
    
    concat_csv = os.path.join(OUTPUT_DIR, "stratified_samples_by_file.csv")
    concat_df.to_csv(concat_csv, index=False)
    print(f"Saved grouped CSV: {concat_csv}")
    
    # Print columns
    print(f"\nColumns in CSV:")
    for col in result_df.columns:
        print(f"   - {col}")
    
    # Zero-shot summary
    print("\n" + "=" * 60)
    print("Zero-Shot Classification Summary")
    print("=" * 60)
    print(f"\nLabel distribution:")
    print(result_df['zeroshot_label'].value_counts())
    print(f"\nAverage confidence: {result_df['zeroshot_score'].mean():.4f}")
    
    print(f"\n📋 Labeling guide:")
    print(f"   First 128 rows = 1 sample from each of the 128 files")
    print(f"   First 256 rows = 2 samples from each file")


if __name__ == "__main__":
    main()