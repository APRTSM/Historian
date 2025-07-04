""" Plots """
import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
import numpy as np
from pathlib import Path
from utils.config import TMP_RQ1_DATA_DIR, TMP_PLOTS_DIR
import os


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


if __name__ == "__main__":
    run_plots_cluster_sizes()