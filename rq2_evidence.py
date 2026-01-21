import pandas as pd
from collections import Counter

def majority_vote_labels(df, label_column="expert_label", id_column="uid"):
    """
    Apply majority voting to get one label per patch.
    Handles ties by returning 'Unknown'.
    """
    voted_labels = {}

    for patch_uid, group in df.groupby(id_column):
        non_unknown_labels = group[group[label_column] != "Unknown"][label_column]
        label_counts = Counter(non_unknown_labels)

        if not label_counts:
            voted_labels[patch_uid] = "Unknown"
            continue

        most_common = label_counts.most_common()
        
        if len(most_common) > 1 and most_common[0][1] == most_common[1][1]:
            voted_labels[patch_uid] = "Unknown"
        else:
            voted_labels[patch_uid] = most_common[0][0]

    return voted_labels


def translate_to_verdict(ref_correctness, expert_label):
    """
    Translate expert label to verdict based on reference correctness.
    
    CC (Clone Type) Logic:
    - Correct + not-clone -> Overfitting
    - Correct + type-1/2/4 -> Correct
    - Correct + type-3 -> Unknown
    - Overfitting + not-clone -> Unknown
    - Overfitting + type-1/2/4 -> Overfitting
    - Overfitting + type-3 -> Unknown
    """
    if ref_correctness == "Correct":
        if expert_label in ['type-1', 'type-2', 'type-4']:
            return "Correct"
        elif expert_label == 'type-3':
            return "Unknown"
        elif expert_label == "not-clone":
            return "Overfitting"
    
    elif ref_correctness == "Overfitting":
        if expert_label in ['type-1', 'type-2', 'type-4']:
            return "Overfitting"
        elif expert_label == 'type-3':
            return "Unknown"
        elif expert_label == "not-clone":
            return "Unknown"
    
    return "Unknown"


# Load the data
labeled_pairs = pd.read_csv("rq2/rq2_expert_labeled_tbar.csv")

from build import init, clean_patches, get_methods, normalaize_names, deduplicate_patches

bugs, developer_patches, tool_patches = init(configure=False)
cleaned_developer_patches, cleaned_tool_patches = clean_patches(bugs, developer_patches, tool_patches)
cleaned_developer_patches, cleaned_tool_patches = get_methods(cleaned_developer_patches, cleaned_tool_patches, bugs)
cleaned_developer_patches, cleaned_tool_patches = normalaize_names(cleaned_developer_patches, cleaned_tool_patches)
cleaned_tool_patches = deduplicate_patches(cleaned_tool_patches)

# Get TBar patches ground truth
tbar_patches = cleaned_tool_patches[cleaned_tool_patches["generator_id"].str.lower().str.contains("tbar")]
ground_truth = tbar_patches[['correctness']].copy()

# All patches for reference correctness lookup
all_patches = pd.concat([cleaned_developer_patches, tool_patches], axis=0)
all_patches = all_patches[~all_patches.index.duplicated(keep='first')]

print(f"Original rows: {len(labeled_pairs)}")
print(f"Unique TBar patches: {labeled_pairs['uid'].nunique()}")

# ============================================================
# Add ref_correctness - MISSING ones will get type-3 treatment
# ============================================================
labeled_pairs['ref_correctness'] = labeled_pairs['groundtruth_index'].map(all_patches['correctness'])

# Count missing before handling
missing_ref_count = labeled_pairs['ref_correctness'].isna().sum()
print(f"\nPairs with missing ref_correctness: {missing_ref_count}")

# ============================================================
# STEP 1: Oracle Input
# ============================================================
print("\n" + "="*60)
print("STEP 1: Oracle Input")
print("="*60)

# For Step 1, only show pairs WITH ref_correctness
labeled_pairs_with_ref = labeled_pairs[labeled_pairs['ref_correctness'].notna()]

step1_table = pd.crosstab(
    labeled_pairs_with_ref['ref_correctness'], 
    labeled_pairs_with_ref['expert_label'],
    margins=True,
    margins_name='Total'
)

clone_types = ['type-1', 'type-2', 'type-3', 'type-4', 'not-clone']
cols_order = [c for c in clone_types if c in step1_table.columns] + ['Total']
step1_table = step1_table[[c for c in cols_order if c in step1_table.columns]]

print("\nRef. GT vs Expert Label (excluding missing ref):")
print(step1_table)

# ============================================================
# STEP 2: Pairwise Inference
# ============================================================
print("\n" + "="*60)
print("STEP 2: Pairwise Inference")
print("="*60)

# Calculate verdict - missing ref_correctness pairs become "Unknown"
def translate_to_verdict_with_missing(row):
    ref_correctness = row['ref_correctness']
    expert_label = row['expert_label']
    
    # If ref_correctness is missing, treat as Unknown
    if pd.isna(ref_correctness):
        return "Unknown"
    
    return translate_to_verdict(ref_correctness, expert_label)

labeled_pairs['verdict'] = labeled_pairs.apply(translate_to_verdict_with_missing, axis=1)

step2_counts = labeled_pairs['verdict'].value_counts()
print("\nVerdict Distribution:")
for verdict in ['Correct', 'Overfitting', 'Unknown']:
    count = step2_counts.get(verdict, 0)
    print(f"  {verdict}: {count}")
print(f"  Total Pairs: {len(labeled_pairs)}")

# ============================================================
# STEP 3: Final Verdicts - majority voting per TBar patch
# ============================================================
print("\n" + "="*60)
print("STEP 3: Final Verdicts (Majority Voting)")
print("="*60)

# Apply majority voting on verdict column
voted_verdicts = majority_vote_labels(labeled_pairs, label_column="verdict", id_column="uid")

# Create voted dataframe
voted_df = pd.DataFrame({
    'uid': list(voted_verdicts.keys()),
    'inferred_label': list(voted_verdicts.values())
})

# Add ground truth correctness for TBar patches
voted_df['gt_correctness'] = voted_df['uid'].map(ground_truth['correctness'])

# For missing gt_correctness, try to get from all_patches
missing_gt_mask = voted_df['gt_correctness'].isna()
if missing_gt_mask.any():
    voted_df.loc[missing_gt_mask, 'gt_correctness'] = voted_df.loc[missing_gt_mask, 'uid'].map(all_patches['correctness'])

# Check how many still missing
still_missing = voted_df['gt_correctness'].isna().sum()
print(f"\nPatches with missing gt_correctness after all lookups: {still_missing}")

if still_missing > 0:
    print("Missing patches:")
    print(voted_df[voted_df['gt_correctness'].isna()]['uid'].tolist()[:10])

# Create confusion matrix
print("\nConfusion Matrix (GT vs Inferred):")
confusion = pd.crosstab(
    voted_df['gt_correctness'].fillna('MISSING'), 
    voted_df['inferred_label'],
    margins=True,
    margins_name='Total'
)

verdict_order = ['Correct', 'Overfitting', 'Unknown', 'Total']
row_order = ['Correct', 'Overfitting', 'MISSING', 'Total']
confusion = confusion.reindex(
    index=[c for c in row_order if c in confusion.index],
    columns=[c for c in verdict_order if c in confusion.columns]
)
print(confusion)

# Calculate metrics (excluding MISSING gt_correctness)
voted_df_clean = voted_df[voted_df['gt_correctness'].notna()]
covered = voted_df_clean[voted_df_clean['inferred_label'] != 'Unknown']
total_patches = len(voted_df_clean)
covered_patches = len(covered)

if covered_patches > 0:
    correct_predictions = (covered['gt_correctness'] == covered['inferred_label']).sum()
    accuracy = (correct_predictions / covered_patches) * 100
else:
    accuracy = 0

coverage = (covered_patches / total_patches) * 100

print(f"\n(Excluding patches with missing GT)")
print(f"Total patches with GT: {total_patches}")
print(f"Covered patches: {covered_patches}")
print(f"Accuracy on Covered Set (%): {accuracy:.1f}")
print(f"Coverage (%): {coverage:.1f}")

# ============================================================
# Also show metrics for ALL 139 patches
# ============================================================
print("\n" + "="*60)
print("METRICS FOR ALL 139 PATCHES")
print("="*60)

covered_all = voted_df[voted_df['inferred_label'] != 'Unknown']
print(f"Total patches: {len(voted_df)}")
print(f"Covered (non-Unknown verdict): {len(covered_all)}")
print(f"Coverage (%): {len(covered_all) / len(voted_df) * 100:.1f}")

# ============================================================
# MISCLASSIFIED PATCHES ANALYSIS
# ============================================================
print("\n" + "="*60)
print("MISCLASSIFIED PATCHES ANALYSIS")
print("="*60)

# Find misclassified patches (only among covered, non-Unknown)
misclassified = covered[covered['gt_correctness'] != covered['inferred_label']]

# Correct patches predicted as Overfitting
correct_as_overfitting = misclassified[
    (misclassified['gt_correctness'] == 'Correct') & 
    (misclassified['inferred_label'] == 'Overfitting')
]

# Overfitting patches predicted as Correct
overfitting_as_correct = misclassified[
    (misclassified['gt_correctness'] == 'Overfitting') & 
    (misclassified['inferred_label'] == 'Correct')
]

print(f"\nTotal misclassified: {len(misclassified)}")

print(f"\n--- Correct patches predicted as Overfitting ({len(correct_as_overfitting)}): ---")
for idx, row in correct_as_overfitting.iterrows():
    print(f"  {row['uid']}")
    # Show the voting details for this patch
    patch_pairs = labeled_pairs[labeled_pairs['uid'] == row['uid']]
    verdict_counts = patch_pairs['verdict'].value_counts().to_dict()
    print(f"    Voting: {verdict_counts}")

print(f"\n--- Overfitting patches predicted as Correct ({len(overfitting_as_correct)}): ---")
for idx, row in overfitting_as_correct.iterrows():
    print(f"  {row['uid']}")
    # Show the voting details for this patch
    patch_pairs = labeled_pairs[labeled_pairs['uid'] == row['uid']]
    verdict_counts = patch_pairs['verdict'].value_counts().to_dict()
    print(f"    Voting: {verdict_counts}")

# ============================================================
# Save results
# ============================================================
voted_df.to_csv("rq2/rq2_expert_labeled_tbar_voted.csv", index=False)
voted_df.to_pickle("rq2/rq2_expert_labeled_tbar_voted.pkl")

print("\n" + "="*60)
print("Results saved!")
print("="*60)
