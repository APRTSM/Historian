import matplotlib.pyplot as plt
import numpy as np
import matplotlib.patches as mpatches
from utils.config import *

# Set up the figure with academic styling
plt.rcParams['font.family'] = 'serif'
plt.rcParams['font.size'] = 11
plt.rcParams['axes.linewidth'] = 0.8
plt.rcParams['hatch.linewidth'] = 0.5  # Thinner hatch lines for denser appearance

# Data (removed baseline)
tools = ['DifFix\n(2020)', 'Recoder\n(2021)', 'Circle\n(2022)', 'TransplantFx\n(2023)', 'Iter\n(2024)']

# Total clone percentages (reduced values)
correct_clone_pct = [15.2, 39.6, 45.3, 12.5, 61.5]
overfitting_clone_pct = [0, 60, 0, 16.4, 0]

# Reference set sizes (total patches for each tool)
correct_total = [40, 76, 66, 113, 77]
overfitting_total = [0, 5, 0, 161, 0]

# Three categories for correct patches:
correct_baseline_only = [15.2, 37.8, 38.5, 11.6, 51.6]
correct_both = [0, 2.1, 7.5, 3.9, 15.5]
correct_tools_only = [0, 4.9, 12.3, 0.9, 6.4]

# Three categories for overfitting patches:
overfitting_baseline_only = [0, 40, 0, 12.2, 0]
overfitting_both = [0, 0, 0, 5.6, 0]
overfitting_tools_only = [0, 0, 0, 3.9, 0]

# Colors - baby/pastel red and green
baby_green = '#90EE90'  # Light green
baby_red = '#FFB6C1'    # Light pink/red

# Create figure
fig, ax = plt.subplots(figsize=(10, 6))

x = np.arange(len(tools))
width = 0.25  # Thinner bars
gap = 0.03    # Small gap between paired bars

# Create stacked bars for correct patches (left bar, shifted left by gap/2)
# Bottom: baseline only (y=-x hatch: \\)
bars1_base = ax.bar(x - width/2 - gap/2, correct_baseline_only, width, 
                    color=baby_green, edgecolor='black', linewidth=0.5,
                    hatch='\\\\\\\\')
# Middle: both (diamond pattern: both hatches)
bars1_both = ax.bar(x - width/2 - gap/2, correct_both, width,
                    bottom=correct_baseline_only,
                    color=baby_green, edgecolor='black', linewidth=0.5,
                    hatch='xxxx')
# Top: tools only (y=x hatch: //)
bars1_tools = ax.bar(x - width/2 - gap/2, correct_tools_only, width,
                     bottom=[b + m for b, m in zip(correct_baseline_only, correct_both)],
                     color=baby_green, edgecolor='black', linewidth=0.5,
                     hatch='////')

# Create stacked bars for overfitting patches (right bar, shifted right by gap/2)
# Bottom: baseline only (y=-x hatch: \\)
bars2_base = ax.bar(x + width/2 + gap/2, overfitting_baseline_only, width,
                    color=baby_red, edgecolor='black', linewidth=0.5,
                    hatch='\\\\\\\\')
# Middle: both (diamond pattern: both hatches)
bars2_both = ax.bar(x + width/2 + gap/2, overfitting_both, width,
                    bottom=overfitting_baseline_only,
                    color=baby_red, edgecolor='black', linewidth=0.5,
                    hatch='xxxx')
# Top: tools only (y=x hatch: //)
bars2_tools = ax.bar(x + width/2 + gap/2, overfitting_tools_only, width,
                     bottom=[b + m for b, m in zip(overfitting_baseline_only, overfitting_both)],
                     color=baby_red, edgecolor='black', linewidth=0.5,
                     hatch='////')

# Customize the plot
ax.set_ylabel('Percentage of Patches that are Clones (%)', fontsize=12)
ax.set_xlabel('APR Tool (Year)', fontsize=12)
ax.set_xticks(x)
ax.set_xticklabels(tools)
ax.set_ylim(0, 100)
ax.set_yticks(np.arange(0, 101, 10))

# Remove top and right spines
ax.spines['top'].set_visible(False)
ax.spines['right'].set_visible(False)

# Create custom legend
solid_green = mpatches.Patch(facecolor=baby_green, edgecolor='black', label='Correct Patches')
solid_red = mpatches.Patch(facecolor=baby_red, edgecolor='black', label='Overfitting Patches')
baseline_patch = mpatches.Patch(facecolor='white', edgecolor='black', hatch='\\\\\\\\', label='Baseline Only')
both_patch = mpatches.Patch(facecolor='white', edgecolor='black', hatch='xxxx', label='Both')
tools_patch = mpatches.Patch(facecolor='white', edgecolor='black', hatch='////', label='Added Tools Only')

ax.legend(handles=[solid_green, solid_red, baseline_patch, both_patch, tools_patch], 
          loc='upper left', frameon=True, edgecolor='black', fancybox=False,
          fontsize=9)

# Add value labels on bars (total percentages and reference set sizes)
for i in range(len(tools)):
    total1 = correct_clone_pct[i]
    total2 = overfitting_clone_pct[i]
    
    # Percentage labels
    ax.annotate(f'{total1:.1f}%',
                xy=(x[i] - width/2 - gap/2, total1),
                xytext=(0, 3), textcoords="offset points",
                ha='center', va='bottom', fontsize=9)
    ax.annotate(f'{total2:.1f}%',
                xy=(x[i] + width/2 + gap/2, total2),
                xytext=(0, 3), textcoords="offset points",
                ha='center', va='bottom', fontsize=9)
    
    # Reference set size labels (n=X) above percentage
    ax.annotate(f'n={correct_total[i]}',
                xy=(x[i] - width/2 - gap/2, total1),
                xytext=(0, 14), textcoords="offset points",
                ha='center', va='bottom', fontsize=8, style='italic')
    ax.annotate(f'n={overfitting_total[i]}',
                xy=(x[i] + width/2 + gap/2, total2),
                xytext=(0, 14), textcoords="offset points",
                ha='center', va='bottom', fontsize=8, style='italic')

# Tight layout
plt.tight_layout()

# Save figure
plt.savefig(os.path.join(RQ4_META_DATA_DIR, 'rq5_temporal_redundancy.pdf'), format='pdf', dpi=300, bbox_inches='tight')
plt.savefig(os.path.join(RQ4_META_DATA_DIR, 'rq5_temporal_redundancy.png'), format='png', dpi=300, bbox_inches='tight')


print("Figure generated and saved at:"
      f" {os.path.join(RQ4_META_DATA_DIR, 'rq5_temporal_redundancy.pdf')} and "
      f"{os.path.join(RQ4_META_DATA_DIR, 'rq5_temporal_redundancy.png')}")
print("Figure saved successfully!")