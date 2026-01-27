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
# DETAILED VOTING TABLE FOR ALL 139 PATCHES
# ============================================================
print("\n" + "="*60)
print("DETAILED VOTING TABLE FOR ALL 139 PATCHES")
print("="*60)

def get_detailed_voting(uid, df):
    """Get detailed voting breakdown for a patch"""
    patch_pairs = df[df['uid'] == uid]
    
    # Count verdicts
    verdict_counts = patch_pairs['verdict'].value_counts().to_dict()
    
    # For each verdict, get the breakdown by expert_label
    verdict_details = {}
    for verdict in ['Correct', 'Overfitting', 'Unknown']:
        verdict_pairs = patch_pairs[patch_pairs['verdict'] == verdict]
        if len(verdict_pairs) > 0:
            label_counts = verdict_pairs['expert_label'].value_counts().to_dict()
            verdict_details[verdict] = {
                'total': len(verdict_pairs),
                'breakdown': label_counts
            }
    
    return verdict_details

def format_verdict_details(details):
    """Format verdict details as a string"""
    parts = []
    for verdict in ['Unknown', 'Overfitting', 'Correct']:
        if verdict in details:
            info = details[verdict]
            breakdown_str = ', '.join([f"{count} {label}" for label, count in info['breakdown'].items()])
            parts.append(f"'{verdict}': {info['total']} ({breakdown_str})")
    return ', '.join(parts)

# Build the detailed table
detailed_rows = []
for uid in voted_df['uid']:
    row_data = voted_df[voted_df['uid'] == uid].iloc[0]
    details = get_detailed_voting(uid, labeled_pairs)
    
    detailed_rows.append({
        'uid': uid,
        'gt_correctness': row_data['gt_correctness'],
        'inferred_label': row_data['inferred_label'],
        'voting_details': format_verdict_details(details),
        'total_pairs': len(labeled_pairs[labeled_pairs['uid'] == uid]),
        'correct_votes': details.get('Correct', {}).get('total', 0),
        'overfitting_votes': details.get('Overfitting', {}).get('total', 0),
        'unknown_votes': details.get('Unknown', {}).get('total', 0),
        'details_dict': details  # Keep for later analysis
    })

detailed_df = pd.DataFrame(detailed_rows)

# Print the table
print("\n{:<70} | {:<12} | {:<12} | {:<6} | {}".format(
    "Patch UID", "GT", "Inferred", "Pairs", "Voting Details"
))
print("-" * 180)

for idx, row in detailed_df.iterrows():
    print("{:<70} | {:<12} | {:<12} | {:<6} | {}".format(
        row['uid'][:70],
        row['gt_correctness'],
        row['inferred_label'],
        row['total_pairs'],
        row['voting_details']
    ))

# Save detailed table to CSV (without the dict column)
detailed_df_save = detailed_df.drop(columns=['details_dict'])
detailed_df_save.to_csv("rq2/rq2_detailed_voting_table.csv", index=False)
print(f"\nDetailed voting table saved to: rq2/rq2_detailed_voting_table.csv")

# ============================================================
# CONFLICTING VOTES ANALYSIS
# ============================================================
print("\n" + "="*60)
print("CONFLICTING VOTES ANALYSIS")
print("="*60)
print("(Patches with BOTH 'Correct' and 'Overfitting' votes)")
print("="*60)

# Find patches with both Correct and Overfitting votes
conflicting_patches = detailed_df[
    (detailed_df['correct_votes'] > 0) & 
    (detailed_df['overfitting_votes'] > 0)
]

print(f"\nTotal patches with conflicting votes: {len(conflicting_patches)}")

if len(conflicting_patches) > 0:
    print("\n{:<70} | {:<12} | {:<12} | {:<8} | {:<8} | {:<8} | {}".format(
        "Patch UID", "GT", "Inferred", "Correct", "Overfit", "Unknown", "Voting Details"
    ))
    print("-" * 200)
    
    for idx, row in conflicting_patches.iterrows():
        print("{:<70} | {:<12} | {:<12} | {:<8} | {:<8} | {:<8} | {}".format(
            row['uid'][:70],
            row['gt_correctness'],
            row['inferred_label'],
            row['correct_votes'],
            row['overfitting_votes'],
            row['unknown_votes'],
            row['voting_details']
        ))
    
    # Summary statistics for conflicting patches
    print("\n--- Summary of Conflicting Patches ---")
    print(f"Total conflicting patches: {len(conflicting_patches)}")
    print(f"  - GT Correct: {len(conflicting_patches[conflicting_patches['gt_correctness'] == 'Correct'])}")
    print(f"  - GT Overfitting: {len(conflicting_patches[conflicting_patches['gt_correctness'] == 'Overfitting'])}")
    print(f"\nInferred labels for conflicting patches:")
    print(conflicting_patches['inferred_label'].value_counts().to_string())
    
    # Check accuracy on conflicting patches
    conflicting_covered = conflicting_patches[conflicting_patches['inferred_label'] != 'Unknown']
    if len(conflicting_covered) > 0:
        conflicting_correct = (conflicting_covered['gt_correctness'] == conflicting_covered['inferred_label']).sum()
        print(f"\nAccuracy on conflicting patches (covered only): {conflicting_correct}/{len(conflicting_covered)} = {conflicting_correct/len(conflicting_covered)*100:.1f}%")
    
    # Save conflicting patches to separate CSV
    conflicting_df_save = conflicting_patches.drop(columns=['details_dict'])
    conflicting_df_save.to_csv("rq2/rq2_conflicting_votes.csv", index=False)
    print(f"\nConflicting votes saved to: rq2/rq2_conflicting_votes.csv")

else:
    print("\nNo patches with conflicting votes found!")

# ============================================================
# Save results
# ============================================================
voted_df.to_csv("rq2/rq2_expert_labeled_tbar_voted.csv", index=False)
voted_df.to_pickle("rq2/rq2_expert_labeled_tbar_voted.pkl")

print("\n" + "="*60)
print("Results saved!")
print("="*60)

# ============================================================
# LATEX TABLE GENERATION (LONGTABLE WITH ORANGE FOR OVERFITTING)
# ============================================================
print("\n" + "="*60)
print("LATEX TABLE GENERATION")
print("="*60)

def escape_latex(text):
    """Escape special LaTeX characters"""
    replacements = {
        '_': r'\_',
        '&': r'\&',
        '%': r'\%',
        '#': r'\#',
        '{': r'\{',
        '}': r'\}',
    }
    for old, new in replacements.items():
        text = text.replace(old, new)
    return text

def shorten_uid(uid):
    """Shorten UID for display"""
    parts = uid.split('-')
    if 'defects4j' in uid:
        try:
            d4j_idx = parts.index('defects4j')
            project = parts[d4j_idx + 1]
            bug_num = parts[d4j_idx + 2]
            patch_info = '-'.join(parts[d4j_idx + 3:])
            return f"{project}-{bug_num}-{patch_info}"
        except:
            return uid[:50]
    return uid[:50]

def format_label_summary_latex(details):
    """Format label counts for LaTeX"""
    label_order = ['type-1', 'type-2', 'type-3', 'type-4', 'not-clone']
    all_labels = {}
    
    for verdict, info in details.items():
        for label, count in info['breakdown'].items():
            if label not in all_labels:
                all_labels[label] = 0
            all_labels[label] += count
    
    parts = []
    for label in label_order:
        if label in all_labels:
            short_label = label.replace('type-', 'T').replace('not-clone', 'NC')
            parts.append(f"{short_label}:{all_labels[label]}")
    
    return ', '.join(parts)

def format_verdict_summary_latex(details):
    """Format verdict counts for LaTeX"""
    parts = []
    verdict_order = ['Correct', 'Overfitting', 'Unknown']
    
    for verdict in verdict_order:
        if verdict in details:
            short_verdict = verdict[0]
            parts.append(f"{short_verdict}:{details[verdict]['total']}")
    
    return ', '.join(parts)

# ============================================================
# MAIN LATEX TABLE (LONGTABLE VERSION)
# ============================================================
print("\n--- MAIN LATEX TABLE (LONGTABLE) ---")

latex_main = r"""% Add to preamble:
% \usepackage{longtable}
% \usepackage{xcolor}

\section{Appendix}
\label{sec:appendix}

\begin{longtable}{l|c|l|l|c}
\caption{Oracle Experiment Results: Expert-Labeled Clone Types to Patch Correctness Verdicts for TBar Patches}
\label{tab:oracle-results} \\
\hline
\textbf{TBar Patch} & \textbf{Pairs} & \textbf{Clone Labels} & \textbf{Verdicts} & \textbf{Pred.} \\
\hline
\endfirsthead

\multicolumn{5}{c}%
{{\tablename\ \thetable{} -- continued from previous page}} \\
\hline
\textbf{TBar Patch} & \textbf{Pairs} & \textbf{Clone Labels} & \textbf{Verdicts} & \textbf{Pred.} \\
\hline
\endhead

\hline
\multicolumn{5}{r}{{Continued on next page}} \\
\endfoot

\hline
\multicolumn{5}{l}{\footnotesize 
\textbf{Clone Labels:} T1-T4: Type-1 to Type-4, NC: Not-Clone. \quad
\textbf{Verdicts:} C: Correct, O: Overfitting, U: Unknown.} \\
\multicolumn{5}{l}{\footnotesize 
\textcolor{green!60!black}{Green}: Correct prediction, \textcolor{orange}{Orange}: Overfitting prediction, \textcolor{gray}{Gray}: Unknown.} \\
\endlastfoot

"""

for idx, row in detailed_df.iterrows():
    short_uid = shorten_uid(row['uid'])
    escaped_uid = escape_latex(short_uid)
    
    label_summary = format_label_summary_latex(row['details_dict'])
    verdict_summary = format_verdict_summary_latex(row['details_dict'])
    
    predicted = row['inferred_label']
    gt = row['gt_correctness']
    
    # Color coding: Green for Correct, Orange for Overfitting, Gray for Unknown
    if predicted == 'Unknown':
        pred_formatted = r'\textcolor{gray}{U}'
    elif predicted == 'Correct':
        pred_formatted = r'\textcolor{green!60!black}{C}'
    elif predicted == 'Overfitting':
        pred_formatted = r'\textcolor{orange}{O}'
    else:
        pred_formatted = predicted[0]
    
    latex_main += f"{escaped_uid} & {row['total_pairs']} & {label_summary} & {verdict_summary} & {pred_formatted} \\\\\n"

latex_main += r"""
\end{longtable}
"""

print(latex_main)

with open("rq2/latex_main_table.tex", "w") as f:
    f.write(latex_main)
print("\nMain LaTeX table saved to: rq2/latex_main_table.tex")

# ============================================================
# CHERRY-PICKED LATEX TABLES
# ============================================================
print("\n" + "="*60)
print("CHERRY-PICKED LATEX TABLES")
print("="*60)

def generate_cherry_picked_latex(tbar_uid, labeled_pairs, voted_df):
    """Generate a detailed LaTeX table for a specific TBar patch"""
    
    patch_pairs = labeled_pairs[labeled_pairs['uid'] == tbar_uid].copy()
    patch_pairs = patch_pairs.sort_values(by=['verdict', 'expert_label'])
    
    tbar_info = voted_df[voted_df['uid'] == tbar_uid].iloc[0]
    short_tbar = shorten_uid(tbar_uid)
    
    latex = f"""\\begin{{table}}[t]
\\centering
\\caption{{Cherry-Picked: {escape_latex(short_tbar)} (GT: {tbar_info['gt_correctness']}, Pred: {tbar_info['inferred_label']})}}
\\label{{tab:cherry-{short_tbar.replace('-', '').replace('_', '')}}}
\\footnotesize
\\renewcommand{{\\arraystretch}}{{1.1}}
\\setlength{{\\tabcolsep}}{{4pt}}
\\begin{{tabular}}{{l|c|c|c}}
\\hline
\\textbf{{Reference Patch}} & \\textbf{{Type}} & \\textbf{{Ref. GT}} & \\textbf{{Verdict}} \\\\
\\hline
"""
    
    for _, pair in patch_pairs.iterrows():
        ref_uid = shorten_uid(pair['groundtruth_index'])
        escaped_ref = escape_latex(ref_uid)
        
        expert_label = pair['expert_label'].replace('type-', 'T').replace('not-clone', 'NC')
        ref_gt = str(pair['ref_correctness'])[:1] if pd.notna(pair['ref_correctness']) else '-'
        
        verdict = pair['verdict']
        if verdict == 'Correct':
            verdict_formatted = r'\textcolor{green!60!black}{C}'
        elif verdict == 'Overfitting':
            verdict_formatted = r'\textcolor{orange}{O}'
        else:
            verdict_formatted = r'\textcolor{gray}{U}'
        
        latex += f"{escaped_ref} & {expert_label} & {ref_gt} & {verdict_formatted} \\\\\n"
    
    details = get_detailed_voting(tbar_uid, labeled_pairs)
    verdict_summary = format_verdict_summary_latex(details)
    
    latex += f"""\\hline
\\multicolumn{{3}}{{r|}}{{\\textbf{{Verdict Counts:}}}} & {verdict_summary} \\\\
\\multicolumn{{3}}{{r|}}{{\\textbf{{Final Prediction:}}}} & \\textbf{{{tbar_info['inferred_label']}}} \\\\
\\hline
\\end{{tabular}}
\\vspace{{1mm}}
\\parbox{{\\linewidth}}{{\\centering\\footnotesize 
\\vspace{{1mm}}
\\textbf{{Type:}} T1-T4: Type-1 to Type-4, NC: Not-Clone. \\;
\\textbf{{Ref. GT:}} C: Correct, O: Overfitting. \\\\
\\textbf{{Verdict:}} \\textcolor{{green!60!black}}{{C}}: Correct, \\textcolor{{orange}}{{O}}: Overfitting, \\textcolor{{gray}}{{U}}: Unknown.}}
\\end{{table}}
"""
    
    return latex

# Define cherry-picked patches - CHANGE THESE TO YOUR DESIRED PATCHES
cherry_picked_patches = [
    "aprenfl-defects4j-Chart-11-TBar-Patch_102_60",
    "aprenfl-defects4j-Chart-26-TBar-Patch_5751_2399",
]

available_patches = voted_df['uid'].tolist()
cherry_picked_patches = [p for p in cherry_picked_patches if p in available_patches]

print(f"\nGenerating cherry-picked tables for {len(cherry_picked_patches)} patches:")

all_cherry_latex = ""
for i, tbar_uid in enumerate(cherry_picked_patches, 1):
    print(f"\n{i}. {tbar_uid}")
    
    cherry_latex = generate_cherry_picked_latex(tbar_uid, labeled_pairs, voted_df)
    all_cherry_latex += cherry_latex + "\n\n"
    
    print(cherry_latex)

with open("rq2/latex_cherry_picked_tables.tex", "w") as f:
    f.write(all_cherry_latex)
print(f"\nCherry-picked LaTeX tables saved to: rq2/latex_cherry_picked_tables.tex")

print("\n" + "="*60)
print("ALL DONE!")
print("="*60)