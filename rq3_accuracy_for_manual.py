#!/usr/bin/env python3
"""
Analyze manual labels from interleaved CSV and generate:
1. LaTeX table showing zero-shot classification accuracy
2. Violin plots showing score distributions
"""

import os
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from collections import defaultdict

# Output directory
OUTPUT_DIR = "./rq3/rq3_manual"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Configuration mappings
PROMPT_TO_COLUMN = {
    # SS (semantic similarity)
    "llm4cc-simple_prompt-semantical": ("SS", "S"),
    "llm4cc-reasoning-patch-semantical": ("SS", "R"),
    "llm4cc-similarity_line-patch-semantical": ("SS", "LS"),
    # SI (semantic identical)
    "llm4cc-simple_prompt-identical": ("SI", "S"),
    "llm4cc-reasoning-patch-identical": ("SI", "R"),
    "llm4cc-similarity_line-patch-identical": ("SI", "LS"),
    # CC (clone classification)
    "llm4cc-clone_type": ("CC", "SCC"),
    "llm4cc-clone_type-patch": ("CC", "SCC"),
    "llm4cc-integrated": ("CC", "I"),
    "llm4cc-integrated-patch": ("CC", "I"),
}

MODEL_MAP = {
    "magicoder:7b-s-cl": "MC7B",
    "codellama:7b-instruct": "CL7B",
    "deepseek-coder:6.7b": "DSC6.7B",
    "codegemma:7b-instruct": "CG7B",
    "qwen2.5:7b": "QW7B",
    "qwen2.5-coder:7b": "QWC7B",
    "yi-coder:9b": "YC9B",
    "hermes3:8b": "H8B",
}

MODEL_ORDER = ["MC7B", "CL7B", "DSC6.7B", "CG7B", "QW7B", "QWC7B", "YC9B", "H8B"]


def load_and_process_csv(csv_path):
    """Load and process the CSV file"""
    df = pd.read_csv(csv_path)
    
    # Ensure manual_label is boolean
    if df['manual_label'].dtype == object:
        df['manual_label'] = df['manual_label'].map({
            'TRUE': True, 'FALSE': False, 
            'True': True, 'False': False, 
            True: True, False: False
        })
    
    # Map model names
    df['model_short'] = df['model'].map(MODEL_MAP)
    
    # Map prompts to categories
    df['prompt_category'] = df['prompt'].map(lambda x: PROMPT_TO_COLUMN.get(x))
    
    # Filter out unmapped entries
    df = df[df['model_short'].notna() & df['prompt_category'].notna()]
    
    return df


def generate_latex_table(df, output_path):
    """Generate LaTeX table similar to the example"""
    
    # Calculate accuracies
    accuracy_data = defaultdict(lambda: defaultdict(dict))
    
    for (processor, model_short, prompt), group in df.groupby(['processor', 'model_short', 'prompt']):
        category = PROMPT_TO_COLUMN.get(prompt)
        if category is None:
            continue
        accuracy = group['manual_label'].mean()
        accuracy_data[model_short][(processor, category)] = accuracy
    
    # Column definitions for iteration
    columns = [
        ('method', 'SS', 'S'), ('method', 'SS', 'R'), ('method', 'SS', 'LS'),
        ('defaultpatch', 'SS', 'S'), ('defaultpatch', 'SS', 'R'), ('defaultpatch', 'SS', 'LS'),
        ('method', 'SI', 'S'), ('method', 'SI', 'R'), ('method', 'SI', 'LS'),
        ('defaultpatch', 'SI', 'S'), ('defaultpatch', 'SI', 'R'), ('defaultpatch', 'SI', 'LS'),
        ('method', 'CC', 'SCC'), ('method', 'CC', 'I'),
        ('defaultpatch', 'CC', 'SCC'), ('defaultpatch', 'CC', 'I'),
    ]
    
    # Build LaTeX table
    latex_lines = []
    
    # Header
    latex_lines.append(r"\begin{table*}[t]")
    latex_lines.append(r"\centering")
    latex_lines.append(r"\caption{Zero-Shot Classification Accuracy (Manual Evaluation)}")
    latex_lines.append(r"\label{tab:manual_zeroshot_accuracy}")
    latex_lines.append(r"\footnotesize")
    latex_lines.append(r"\setlength{\tabcolsep}{2pt}")
    latex_lines.append(r"\renewcommand{\arraystretch}{1.1}")
    latex_lines.append(r"\begin{tabular}{l|ccc|ccc|ccc|ccc|cc|cc|c}")
    
    # Multi-row header
    latex_lines.append(r"\multirow{4}{*}{\textbf{Model}}")
    latex_lines.append(r"& \multicolumn{6}{c|}{\textbf{SS (Yes/No)}}")
    latex_lines.append(r"& \multicolumn{6}{c|}{\textbf{SI (Yes/No)}}")
    latex_lines.append(r"& \multicolumn{4}{c|}{\textbf{CC (No/Clone-Type)}}")
    latex_lines.append(r"& \multirow{4}{*}{\textbf{Avg.}} \\")
    
    latex_lines.append(r"\cline{2-7}\cline{8-13}\cline{14-17}")
    latex_lines.append(r"& \multicolumn{3}{c|}{\textbf{Method}} & \multicolumn{3}{c|}{\textbf{Diff}}")
    latex_lines.append(r"& \multicolumn{3}{c|}{\textbf{Method}} & \multicolumn{3}{c|}{\textbf{Diff}}")
    latex_lines.append(r"& \multicolumn{2}{c|}{\textbf{Method}} & \multicolumn{2}{c|}{\textbf{Diff}} & \\")
    
    latex_lines.append(r"\cline{2-4}\cline{5-7}\cline{8-10}\cline{11-13}\cline{14-15}\cline{16-17}")
    latex_lines.append(r"& S & R & LS & S & R & LS & S & R & LS & S & R & LS & SCC & I & SCC & I & \\")
    latex_lines.append(r"\hline")
    
    # Store all values for average calculation
    column_averages = [[] for _ in columns]
    
    # Data rows
    for model in MODEL_ORDER:
        if model not in accuracy_data:
            continue
            
        row_values = []
        for proc, cat, sub in columns:
            key = (proc, (cat, sub))
            val = accuracy_data[model].get(key)
            row_values.append(val)
            if val is not None:
                column_averages[columns.index((proc, cat, sub))].append(val)
        
        # Find best value(s) for this model
        valid_values = [v for v in row_values if v is not None]
        if valid_values:
            max_val = max(valid_values)
        else:
            max_val = None
        
        # Format row
        row_str = model
        for val in row_values:
            if val is None:
                row_str += " & -"
            elif val == max_val and max_val is not None:
                row_str += f" & \\cellcolor{{blue!15}}{val:.2f}"
            else:
                row_str += f" & {val:.2f}"
        
        # Calculate row average
        if valid_values:
            row_avg = np.mean(valid_values)
            row_str += f" & {row_avg:.2f}"
        else:
            row_str += " & -"
        
        row_str += r" \\"
        latex_lines.append(row_str)
        latex_lines.append(r"\hline")
    
    # Average row
    avg_row = r"\textbf{Avg.}"
    all_avgs = []
    for col_vals in column_averages:
        if col_vals:
            avg = np.mean(col_vals)
            avg_row += f" & {avg:.2f}"
            all_avgs.append(avg)
        else:
            avg_row += " & -"
    
    if all_avgs:
        avg_row += f" & {np.mean(all_avgs):.2f}"
    else:
        avg_row += " & -"
    avg_row += r" \\"
    
    latex_lines.append(avg_row)
    latex_lines.append(r"\bottomrule")
    latex_lines.append(r"\end{tabular}")
    latex_lines.append(r"\vspace{1mm}")
    latex_lines.append(r"\parbox{\linewidth}{\centering\footnotesize \colorbox{blue!15}{Blue} indicates the best value(s) for each model.}")
    latex_lines.append(r"\end{table*}")
    
    # Write to file
    with open(output_path, 'w') as f:
        f.write('\n'.join(latex_lines))
    
    print(f"LaTeX table saved to: {output_path}")
    return '\n'.join(latex_lines)


def create_violin_plots(df, output_dir):
    """Create violin plots showing score distributions"""
    
    df_plot = df.copy()
    df_plot['Correct'] = df_plot['manual_label'].map({True: 'Correct', False: 'Incorrect'})
    df_plot['Representation'] = df_plot['processor'].map({'method': 'Method', 'defaultpatch': 'Diff'})
    
    # Plot 1: Score distribution by correctness
    fig, ax = plt.subplots(figsize=(10, 6))
    
    sns.violinplot(x='Correct', y='zeroshot_score', data=df_plot, 
                   order=['Correct', 'Incorrect'], ax=ax, palette='Set2')
    
    ax.set_xlabel('Zero-Shot Prediction', fontsize=14)
    ax.set_ylabel('Zero-Shot Score', fontsize=14)
    ax.set_title('Score Distribution by Prediction Correctness', fontsize=16)
    ax.tick_params(axis='both', which='major', labelsize=12)
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    ax.axhline(y=0.5, color='gray', linestyle='--', alpha=0.5)
    
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, "violin_correctness.pdf"), bbox_inches='tight')
    plt.close()
    print(f"Saved: violin_correctness.pdf")
    
    # Plot 2: Score distribution by processor and correctness
    fig, ax = plt.subplots(figsize=(10, 6))
    
    sns.violinplot(x='Representation', y='zeroshot_score', hue='Correct', 
                   data=df_plot, ax=ax, palette='Set2', split=True)
    
    ax.set_xlabel('Representation', fontsize=14)
    ax.set_ylabel('Zero-Shot Score', fontsize=14)
    ax.set_title('Score Distribution by Representation and Correctness', fontsize=16)
    ax.tick_params(axis='both', which='major', labelsize=12)
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    ax.legend(title='Prediction', loc='lower right')
    
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, "violin_representation_correctness.pdf"), bbox_inches='tight')
    plt.close()
    print(f"Saved: violin_representation_correctness.pdf")
    
    # Plot 3: Score distribution by prompt category
    fig, axes = plt.subplots(1, 3, figsize=(16, 5))
    
    # SS prompts
    ss_df = df_plot[df_plot['prompt'].str.contains('semantical')]
    if len(ss_df) > 0:
        sns.violinplot(x='Correct', y='zeroshot_score', data=ss_df, 
                       order=['Correct', 'Incorrect'], ax=axes[0], palette='Set2')
        axes[0].set_title('SS (Semantic Similarity)', fontsize=14)
        axes[0].set_xlabel('Prediction', fontsize=12)
        axes[0].set_ylabel('Zero-Shot Score', fontsize=12)
        axes[0].axhline(y=0.5, color='gray', linestyle='--', alpha=0.5)
    
    # SI prompts
    si_df = df_plot[df_plot['prompt'].str.contains('identical')]
    if len(si_df) > 0:
        sns.violinplot(x='Correct', y='zeroshot_score', data=si_df, 
                       order=['Correct', 'Incorrect'], ax=axes[1], palette='Set2')
        axes[1].set_title('SI (Semantic Identical)', fontsize=14)
        axes[1].set_xlabel('Prediction', fontsize=12)
        axes[1].set_ylabel('', fontsize=12)
        axes[1].axhline(y=0.5, color='gray', linestyle='--', alpha=0.5)
    
    # CC prompts
    cc_df = df_plot[df_plot['prompt'].str.contains('clone_type|integrated')]
    if len(cc_df) > 0:
        sns.violinplot(x='Correct', y='zeroshot_score', data=cc_df, 
                       order=['Correct', 'Incorrect'], ax=axes[2], palette='Set2')
        axes[2].set_title('CC (Clone Classification)', fontsize=14)
        axes[2].set_xlabel('Prediction', fontsize=12)
        axes[2].set_ylabel('', fontsize=12)
        axes[2].axhline(y=0.2, color='gray', linestyle='--', alpha=0.5)
    
    for ax in axes:
        ax.spines['top'].set_visible(False)
        ax.spines['right'].set_visible(False)
        ax.tick_params(axis='both', which='major', labelsize=11)
    
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, "violin_by_category.pdf"), bbox_inches='tight')
    plt.close()
    print(f"Saved: violin_by_category.pdf")
    
    # Plot 4: Combined side-by-side plot
    create_combined_violin_plot(df_plot, output_dir)


def create_combined_violin_plot(df, output_dir):
    """Create combined violin plot like the example"""
    
    df_yes_no = df[df['prompt'].str.contains('semantical|identical')]
    df_clone = df[df['prompt'].str.contains('clone_type|integrated')]
    
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))
    
    palette = {'Correct': sns.color_palette('Set2')[0], 'Incorrect': sns.color_palette('Set2')[1]}
    
    # Left: Yes/No tasks
    if len(df_yes_no) > 0:
        sns.violinplot(x='Representation', y='zeroshot_score', hue='Correct',
                       data=df_yes_no, ax=ax1, palette=palette, split=True, legend=False)
        ax1.set_title('(a) Yes/No Classification Tasks', fontsize=16)
        ax1.set_xlabel('Representation', fontsize=14)
        ax1.set_ylabel('Zero-Shot Score', fontsize=14)
        ax1.axhline(y=0.5, color='gray', linestyle='--', alpha=0.5)
        ax1.tick_params(axis='both', which='major', labelsize=12)
        ax1.spines['top'].set_visible(False)
        ax1.spines['right'].set_visible(False)
    
    # Right: Clone Type tasks
    if len(df_clone) > 0:
        sns.violinplot(x='Representation', y='zeroshot_score', hue='Correct',
                       data=df_clone, ax=ax2, palette=palette, split=True, legend=False)
        ax2.set_title('(b) Clone Type Classification Tasks', fontsize=16)
        ax2.set_xlabel('Representation', fontsize=14)
        ax2.set_ylabel('', fontsize=14)
        ax2.axhline(y=0.2, color='gray', linestyle='--', alpha=0.5)
        ax2.tick_params(axis='both', which='major', labelsize=12)
        ax2.tick_params(axis='y', labelleft=False)
        ax2.spines['top'].set_visible(False)
        ax2.spines['right'].set_visible(False)
    
    # Shared legend
    from matplotlib.patches import Patch
    legend_elements = [
        Patch(facecolor=palette['Correct'], label='Correct'),
        Patch(facecolor=palette['Incorrect'], label='Incorrect')
    ]
    fig.legend(handles=legend_elements, loc='upper center', ncol=2,
               fontsize=14, bbox_to_anchor=(0.5, 1.02), frameon=False)
    
    plt.tight_layout()
    plt.subplots_adjust(top=0.88)
    plt.savefig(os.path.join(output_dir, "violin_combined_side_by_side.pdf"), bbox_inches='tight')
    plt.close()
    print(f"Saved: violin_combined_side_by_side.pdf")


def print_summary_statistics(df):
    """Print summary statistics"""
    print("\n" + "="*60)
    print("SUMMARY STATISTICS")
    print("="*60)
    
    print(f"\nTotal samples: {len(df)}")
    print(f"Correct predictions: {df['manual_label'].sum()} ({df['manual_label'].mean()*100:.1f}%)")
    
    print("\nBy Processor:")
    print(df.groupby('processor')['manual_label'].agg(['count', 'sum', 'mean']))
    
    print("\nBy Model:")
    print(df.groupby('model_short')['manual_label'].agg(['count', 'sum', 'mean']).sort_values('mean', ascending=False))
    
    print("\nBy Prompt Category:")
    df['category'] = df['prompt'].apply(lambda x: PROMPT_TO_COLUMN.get(x, (None, None))[0])
    print(df.groupby('category')['manual_label'].agg(['count', 'sum', 'mean']))


def main():
    # Path to the CSV file
    csv_path = "./rq3/Manual-interleaved.csv"
    
    if not os.path.exists(csv_path):
        print(f"Error: CSV file not found at {csv_path}")
        return
    
    print(f"Loading data from: {csv_path}")
    df = load_and_process_csv(csv_path)
    print(f"Loaded {len(df)} samples")
    
    # Print summary statistics
    print_summary_statistics(df)
    
    # Generate LaTeX table
    latex_output = os.path.join(OUTPUT_DIR, "accuracy_table.tex")
    latex_content = generate_latex_table(df, latex_output)
    
    print("\n" + "="*60)
    print("LATEX TABLE CONTENT")
    print("="*60)
    print(latex_content)
    
    # Create violin plots
    print("\n" + "="*60)
    print("GENERATING VIOLIN PLOTS")
    print("="*60)
    create_violin_plots(df, OUTPUT_DIR)
    
    print("\n" + "="*60)
    print(f"All outputs saved to: {OUTPUT_DIR}")
    print("="*60)


if __name__ == "__main__":
    main()