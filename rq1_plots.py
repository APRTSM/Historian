""" Plots """
import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
import numpy as np
from pathlib import Path
from utils.config import TMP_RQ1_DATA_DIR, TMP_PLOTS_DIR
import os
from utils.config import TMP_DEDUPLICATED_TOOL_PATHCES_PKL, TMP_GENERATOR_NORMALIZED_DEVELOPER_PATHCES_PKL, TMP_BUGS_PKL


def plot_cluster_sizes(experiment_dfs, experiment_names=None, save_path=None, 
                      plot_type='violin', figsize=(12, 8), dpi=300):
    """
    Plot cluster size distributions for multiple experiments.
    
    Parameters:
    -----------
    experiment_dfs : dict or list
        Dictionary with experiment names as keys and DataFrames as values,
        or list of DataFrames (will use default names)
    experiment_names : list, optional
        List of names for experiments if experiment_dfs is a list
    save_path : str, optional
        Path to save the plot (e.g., 'cluster_comparison.png')
    plot_type : str, default='violin'
        Type of plot: 'violin', 'box', 'hist', 'bar', or 'combined'
    figsize : tuple, default=(12, 8)
        Figure size (width, height)
    dpi : int, default=300
        Resolution for saved figure
    
    Returns:
    --------
    matplotlib.figure.Figure : The created figure object
    """
    
    # Handle input format
    if isinstance(experiment_dfs, dict):
        exp_dict = experiment_dfs
    elif isinstance(experiment_dfs, list):
        if experiment_names is None:
            experiment_names = [f'Experiment_{i+1}' for i in range(len(experiment_dfs))]
        exp_dict = dict(zip(experiment_names, experiment_dfs))
    else:
        raise ValueError("experiment_dfs must be a dictionary or list of DataFrames")
    
    # Combine all data
    combined_data = []
    for exp_name, df in exp_dict.items():
        temp_df = df.copy()
        temp_df['experiment'] = exp_name
        combined_data.append(temp_df[['group_size', 'experiment']])
    
    plot_df = pd.concat(combined_data, ignore_index=True)
    
    # Create figure based on plot type
    if plot_type == 'combined':
        fig, axes = plt.subplots(2, 2, figsize=(15, 12))
        fig.suptitle('Cluster Size Distribution Comparison Across Experiments', fontsize=16, fontweight='bold')
        
        # Violin plot
        sns.violinplot(data=plot_df, x='experiment', y='group_size', ax=axes[0,0])
        axes[0,0].set_title('Distribution Shape (Violin Plot)')
        axes[0,0].tick_params(axis='x', rotation=45)
        
        # Box plot
        sns.boxplot(data=plot_df, x='experiment', y='group_size', ax=axes[0,1])
        axes[0,1].set_title('Quartiles and Outliers (Box Plot)')
        axes[0,1].tick_params(axis='x', rotation=45)
        
        # Histogram
        for exp_name in exp_dict.keys():
            exp_data = plot_df[plot_df['experiment'] == exp_name]['group_size']
            axes[1,0].hist(exp_data, alpha=0.7, label=exp_name, bins=20)
        axes[1,0].set_title('Frequency Distribution (Histogram)')
        axes[1,0].set_xlabel('Group Size')
        axes[1,0].set_ylabel('Frequency')
        axes[1,0].legend()
        
        # Summary statistics bar plot
        summary_stats = plot_df.groupby('experiment')['group_size'].agg(['mean', 'median', 'std']).reset_index()
        x_pos = np.arange(len(summary_stats))
        width = 0.25
        
        axes[1,1].bar(x_pos - width, summary_stats['mean'], width, label='Mean', alpha=0.8)
        axes[1,1].bar(x_pos, summary_stats['median'], width, label='Median', alpha=0.8)
        axes[1,1].bar(x_pos + width, summary_stats['std'], width, label='Std Dev', alpha=0.8)
        
        axes[1,1].set_title('Summary Statistics')
        axes[1,1].set_xlabel('Experiment')
        axes[1,1].set_ylabel('Value')
        axes[1,1].set_xticks(x_pos)
        axes[1,1].set_xticklabels(summary_stats['experiment'], rotation=45)
        axes[1,1].legend()
        
        plt.tight_layout()
        
    else:
        fig, ax = plt.subplots(figsize=figsize)
        
        if plot_type == 'violin':
            sns.violinplot(data=plot_df, x='experiment', y='group_size', ax=ax)
            ax.set_title('Cluster Size Distribution Comparison (Violin Plot)', fontsize=14, fontweight='bold')
            
        elif plot_type == 'box':
            sns.boxplot(data=plot_df, x='experiment', y='group_size', ax=ax)
            ax.set_title('Cluster Size Distribution Comparison (Box Plot)', fontsize=14, fontweight='bold')
            
        elif plot_type == 'hist':
            colors = plt.cm.Set3(np.linspace(0, 1, len(exp_dict)))
            for i, (exp_name, color) in enumerate(zip(exp_dict.keys(), colors)):
                exp_data = plot_df[plot_df['experiment'] == exp_name]['group_size']
                ax.hist(exp_data, alpha=0.7, label=exp_name, color=color, bins=20)
            ax.set_title('Cluster Size Distribution Comparison (Histogram)', fontsize=14, fontweight='bold')
            ax.set_xlabel('Group Size')
            ax.set_ylabel('Frequency')
            ax.legend()
            
        elif plot_type == 'bar':
            summary_stats = plot_df.groupby('experiment')['group_size'].agg(['mean', 'median']).reset_index()
            x_pos = np.arange(len(summary_stats))
            width = 0.35
            
            ax.bar(x_pos - width/2, summary_stats['mean'], width, label='Mean', alpha=0.8)
            ax.bar(x_pos + width/2, summary_stats['median'], width, label='Median', alpha=0.8)
            
            ax.set_title('Average Cluster Sizes by Experiment', fontsize=14, fontweight='bold')
            ax.set_xlabel('Experiment')
            ax.set_ylabel('Group Size')
            ax.set_xticks(x_pos)
            ax.set_xticklabels(summary_stats['experiment'])
            ax.legend()
        
        ax.tick_params(axis='x', rotation=45)
        ax.grid(True, alpha=0.3)
    
    # Add summary statistics as text
    stats_text = []
    for exp_name, df in exp_dict.items():
        group_sizes = df['group_size']
        stats_text.append(f"{exp_name}: n={len(group_sizes)}, μ={group_sizes.mean():.2f}, σ={group_sizes.std():.2f}")
    
    if plot_type != 'combined':
        fig.text(0.02, 0.02, '\n'.join(stats_text), fontsize=8, verticalalignment='bottom',
                bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.8))
    
    plt.tight_layout()
    
    # Save if path provided
    if save_path:
        plt.savefig(save_path, dpi=dpi, bbox_inches='tight')
        print(f"Plot saved to: {save_path}")
    
    return fig









""" Usage"""
def run_plots_cluster_sizes():
    exact_match = pd.read_pickle(os.path.join(TMP_RQ1_DATA_DIR, "cluster-sizes-exact-match.pkl"))
    sourcerercc_match = pd.read_pickle(os.path.join(TMP_RQ1_DATA_DIR, "cluster-sizes-sourcerercc-match.pkl"))
    type_1_2 = pd.read_pickle(os.path.join(TMP_RQ1_DATA_DIR, "cluster-sizes-type-1-2.pkl"))

    print(exact_match["group_size"].unique())
    print(sourcerercc_match["group_size"].unique())
    print(type_1_2["group_size"].unique())
    
    experiments = {
        'Exact Match': exact_match,
        'SourcererCC Match': sourcerercc_match,
        'Type 1 and 2 Match': type_1_2
    }
    plot_cluster_sizes(experiments, save_path=os.path.join(TMP_PLOTS_DIR, "cluster_sizes_comparison.png"))

def plot_cluster_size_frequency_bars(experiment_dfs, experiment_names=None, save_path=None, 
                                   figsize=(15, 5), dpi=300, max_cluster_size=None):
    """
    Create 3 separate bar charts showing frequency of cluster sizes for each experiment.
    
    Parameters:
    -----------
    experiment_dfs : dict or list
        Dictionary with experiment names as keys and DataFrames as values,
        or list of DataFrames (will use default names)
    experiment_names : list, optional
        List of names for experiments if experiment_dfs is a list
    save_path : str, optional
        Path to save the plot (e.g., 'cluster_size_frequency.png')
    figsize : tuple, default=(15, 5)
        Figure size (width, height)
    dpi : int, default=300
        Resolution for saved figure
    max_cluster_size : int, optional
        Maximum cluster size to display (useful for limiting x-axis range)
    
    Returns:
    --------
    matplotlib.figure.Figure : The created figure object
    """
    
    # Handle input format
    if isinstance(experiment_dfs, dict):
        exp_dict = experiment_dfs
    elif isinstance(experiment_dfs, list):
        if experiment_names is None:
            experiment_names = [f'Experiment_{i+1}' for i in range(len(experiment_dfs))]
        exp_dict = dict(zip(experiment_names, experiment_dfs))
    else:
        raise ValueError("experiment_dfs must be a dictionary or list of DataFrames")
    
    # Create subplots
    fig, axes = plt.subplots(1, 4, figsize=figsize, sharey=True)
    fig.suptitle('Cluster Size Frequency Distribution', fontsize=16, fontweight='bold')
    
    # Define colors for consistency
    colors = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd', '#8c564b']
    
    # Create bar chart for each experiment
    for idx, (exp_name, df) in enumerate(exp_dict.items()):
        ax = axes[idx]
        
        # Calculate frequency of each cluster size
        cluster_size_counts = df['group_size'].value_counts().sort_index()
        
        # Apply max_cluster_size filter if specified
        if max_cluster_size is not None:
            cluster_size_counts = cluster_size_counts[cluster_size_counts.index <= max_cluster_size]
        
        # Create horizontal bar chart
        bars = ax.barh(cluster_size_counts.index, cluster_size_counts.values, 
                      color=colors[idx % len(colors)], alpha=0.7, edgecolor='black', linewidth=0.5)
        
        # Customize each subplot
        ax.set_title(f'{exp_name}', fontsize=12, fontweight='bold')
        ax.set_ylabel('Cluster Size', fontsize=10)
        if idx == 0:  # Only label x-axis for the first subplot
            ax.set_xlabel('Number of Clusters', fontsize=10)
        
        # Add value labels on right of bars
        for bar, count in zip(bars, cluster_size_counts.values):
            width = bar.get_width()
            ax.text(width + 0.1, bar.get_y() + bar.get_height()/2.,
                   f'{int(count)}', ha='left', va='center', fontsize=8)
        
        # Remove grid and background
        ax.grid(False)
        ax.set_facecolor('white')
        
        # Remove top and right spines
        ax.spines['top'].set_visible(False)
        ax.spines['right'].set_visible(False)
        
        # Set y-axis to show all cluster sizes
        ax.set_yticks(cluster_size_counts.index)
        
        # Add summary statistics text with number of clusters
        total_clusters = len(df)
        mean_size = df['group_size'].mean()
        median_size = df['group_size'].median()
        max_size = df['group_size'].max()
        
        stats_text = f'Number of clusters: {total_clusters}\nμ: {mean_size:.1f}\nMedian: {median_size:.1f}\nMax: {max_size}'
        ax.text(0.98, 0.98, stats_text, transform=ax.transAxes, fontsize=8,
                verticalalignment='top', horizontalalignment='right', bbox=dict(boxstyle='round', facecolor='white', alpha=0.8, edgecolor='none'))
    
    plt.tight_layout()
    
    # Save if path provided
    if save_path:
        plt.savefig(save_path, dpi=dpi, bbox_inches='tight')
        print(f"Plot saved to: {save_path}")
    
    return fig

def plot_cluster_size_frequency_single(experiment_dfs, experiment_names=None, save_path=None,
                                        figsize=(8, 6), dpi=300, max_cluster_size=None):
    """
    Create a single horizontal bar chart comparing cluster size frequencies across all experiments.
    
    Parameters:
    -----------
    experiment_dfs : dict or list
        Dictionary with experiment names as keys and DataFrames as values,
        or list of DataFrames (will use default names)
    experiment_names : list, optional
        List of names for experiments if experiment_dfs is a list
    save_path : str, optional
        Path to save the plot (e.g., 'cluster_size_frequency_combined.png')
    figsize : tuple, default=(14, 6)
        Figure size (width, height)
    dpi : int, default=300
        Resolution for saved figure
    max_cluster_size : int, optional
        Maximum cluster size to display (useful for limiting range)
    
    Returns:
    --------
    matplotlib.figure.Figure : The created figure object
    """
    # Handle input format
    if isinstance(experiment_dfs, dict):
        exp_dict = experiment_dfs
    elif isinstance(experiment_dfs, list):
        if experiment_names is None:
            experiment_names = [f'Experiment_{i+1}' for i in range(len(experiment_dfs))]
        exp_dict = dict(zip(experiment_names, experiment_dfs))
    else:
        raise ValueError("experiment_dfs must be a dictionary or list of DataFrames")
    
    # Get all unique cluster sizes across all experiments
    all_cluster_sizes = set()
    for df in exp_dict.values():
        all_cluster_sizes.update(df['group_size'].unique())
    
    if max_cluster_size is not None:
        all_cluster_sizes = {size for size in all_cluster_sizes if size <= max_cluster_size}
    
    all_cluster_sizes = sorted(all_cluster_sizes)
    
    # Create figure
    fig, ax = plt.subplots(figsize=figsize)
    
    # Calculate bar width and positions - slightly thicker bars
    n_experiments = len(exp_dict)
    bar_width = 2 / n_experiments  # Increased from 0.3 to 0.5 for thicker bars
    y_spacing = 2.1  # Reduce spacing between cluster sizes (1.0 = normal, <1.0 = compressed)
    
    # Create bars for each experiment
    for i, (exp_name, df) in enumerate(exp_dict.items()):
        cluster_size_counts = df['group_size'].value_counts().sort_index()
        
        # Create array of counts for all cluster sizes (0 if not present)
        counts = [cluster_size_counts.get(size, 0) for size in all_cluster_sizes]
        
        # Calculate y positions for this experiment's bars with reduced spacing
        y_positions = [(y * y_spacing) + i * bar_width for y in range(len(all_cluster_sizes))]
        
        # Create horizontal bars
        bars = ax.barh(y_positions, counts, bar_width, label=exp_name, alpha=0.7)
        
        # Add value labels on right of bars (only for non-zero values)
# Add value labels on right of bars (including zeros)
        for y_pos, count in zip(y_positions, counts):
            # Moderate offset of 0.25 for reasonable distance from bars
            x_position = max(count + 0.25, 0.25)  # Ensure zeros appear at a minimum position
            ax.text(x_position, y_pos, f'{int(count)}', ha='left', va='center', fontsize=11)
    # Customize plot
    ax.set_xlabel('Number of Clusters', fontsize=14, fontweight='bold')
    ax.set_ylabel('Cluster Size', fontsize=14, fontweight='bold')
    
    # Set y-axis labels with larger font and reduced spacing
    y_tick_positions = [(y * y_spacing) + bar_width * (n_experiments - 1) / 2 for y in range(len(all_cluster_sizes))]
    ax.set_yticks(y_tick_positions)
    ax.set_yticklabels(all_cluster_sizes, fontsize=12)
    
    # Increase x-axis tick label font size
    ax.tick_params(axis='x', labelsize=12)
    
    # Reduce space between first bars and x-axis
    ax.set_ylim(-0.3, len(all_cluster_sizes) * y_spacing)
    
    # Add legend without borders with larger font
    legend = ax.legend(frameon=False, bbox_to_anchor=(0.98, 0.98), fontsize=12)
    
    # Remove grid and background
    ax.grid(False)
    ax.set_facecolor('white')
    
    # Remove top and right spines
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
    plt.tight_layout()
    
    # Save if path provided
    if save_path:
        plt.savefig(save_path, dpi=dpi, bbox_inches='tight')
        print(f"Plot saved to: {save_path}")
    
    return fig


def run_plots_cluster_size_frequency():
    """Example usage of the cluster size frequency plotting functions"""
    
    # Load your data (replace with your actual data loading)
    exact_match = pd.read_pickle(os.path.join(TMP_RQ1_DATA_DIR, "cluster-sizes-exact-match.pkl"))
    sourcerercc_match = pd.read_pickle(os.path.join(TMP_RQ1_DATA_DIR, "cluster-sizes-sourcerercc-match.pkl"))
    matching_clone = pd.read_pickle(os.path.join(TMP_RQ1_DATA_DIR, "cluster-sizes-matching-type-2.pkl"))
    type_1_2 = pd.read_pickle(os.path.join(TMP_RQ1_DATA_DIR, "cluster-sizes-type-1-2.pkl"))

    # Get the rows that will be dropped Only Defects4J
    dropped_rows = exact_match[~exact_match['bug_uid'].str.contains('defects4j', na=False)]
    print(dropped_rows)
    print(exact_match)
    exact_match = exact_match[exact_match['bug_uid'].str.contains('defects4j', case=False, na=False)]
    print(exact_match)
    
    print("================================")

    dropped_rows = sourcerercc_match[~sourcerercc_match['bug_uid'].str.contains('defects4j', na=False)]
    print(dropped_rows)
    print(sourcerercc_match)
    sourcerercc_match = sourcerercc_match[sourcerercc_match['bug_uid'].str.contains('defects4j', case=False, na=False)]
    print(sourcerercc_match)

    print("================================")

    dropped_rows = matching_clone[~matching_clone['bug_uid'].str.contains('defects4j', na=False)]
    print(dropped_rows)
    print(matching_clone)
    matching_clone = matching_clone[matching_clone['bug_uid'].str.contains('defects4j', case=False, na=False)]
    print(matching_clone)

    print("================================")

    dropped_rows = type_1_2[~type_1_2['bug_uid'].str.contains('defects4j', na=False)]
    print(dropped_rows) 
    print(type_1_2)
    type_1_2 = type_1_2[type_1_2['bug_uid'].str.contains('defects4j', case=False, na=False)]
    print(type_1_2)

    print("================================")

    experiments = {
        'Exact Match': exact_match,
        'Token-Based Matching': sourcerercc_match,
        'AST-Based Matching': matching_clone,
        'Manual Assessment': type_1_2,
    }
    
    # Create 3 separate bar charts
    fig1 = plot_cluster_size_frequency_bars(
        experiments, 
        save_path=os.path.join(TMP_PLOTS_DIR, "cluster_size_frequency_separate.png")
    )
    
    # Create combined bar chart
    fig2 = plot_cluster_size_frequency_single(
        experiments,
        save_path=os.path.join(TMP_PLOTS_DIR, "cluster_size_frequency_combined.png")
    )

def run_bug_number():
    # Load your data (replace with your actual data loading)
    last_match_only_pairs = pd.read_pickle(os.path.join(TMP_RQ1_DATA_DIR, "last-match-only-pairs.pkl"))
    tool_patches = pd.read_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)

    # Add "bug_uid" column to last_match_only_pairs. first get uid column of last_match_only_pairs
    # Then match with index column of tool_patches and add the bug_uid column to last_match_only_pairs from tool_patches
    last_match_only_pairs = last_match_only_pairs.merge(tool_patches[['bug_uid']], left_on='uid', right_index=True, how='left')
    
    # get unique bug_uids
    unique_bug_uids = last_match_only_pairs['bug_uid'].unique()
    print(f"Number of unique bugs: {len(unique_bug_uids)}")

    pairs_not_matching = last_match_only_pairs[last_match_only_pairs['expert_label'] != 'Match']
    print(f"Number of pairs not matching: {len(pairs_not_matching)}")
    unique_bug_uids_not_matching = pairs_not_matching['bug_uid'].unique()
    print(len(unique_bug_uids_not_matching))

    pairs_matching = last_match_only_pairs[last_match_only_pairs['expert_label'] == 'Match']
    print(f"Number of pairs matching: {len(pairs_matching)}")
    unique_bug_uids_matching = pairs_matching['bug_uid'].unique()
    print(len(unique_bug_uids_matching))

    # Bugs that are in unique_bug_uids_not_matching but not in unique_bug_uids_matching
    unique_bug_uids_not_matching_only = set(unique_bug_uids_not_matching) - set(unique_bug_uids_matching)
    print(f"Number of bugs not matching only: {len(unique_bug_uids_not_matching_only)}")

if __name__ == "__main__":
    # run_plots_cluster_sizes()
    run_plots_cluster_size_frequency()
    # run_bug_number()



