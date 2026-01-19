import matplotlib.pyplot as plt
import numpy as np
import matplotlib.patches as mpatches
import os

# Create output directory
os.makedirs('rq4', exist_ok=True)

# Set up the figure with academic styling
plt.rcParams['font.family'] = 'serif'
plt.rcParams['font.size'] = 14
plt.rcParams['axes.linewidth'] = 0.8
plt.rcParams['hatch.linewidth'] = 0.5

# Data from your results
# Baseline = TMP_DEDUPLICATED_TOOL_PATHCES_PKL (500 correct, 13140 overfitting)
# DLFix+ARJA-e (2020) is baseline

tools = ['DLFix+ARJA-e\n(2020)', 'Recoder\n(2021)', 'CIRCLE\n(2022)', 'TransplantFix\n(2023)', 'ITER\n(2024)']

# Total patches (DLFix: 40 correct, 0 overfit + ARJA-e: 36 correct, 62 overfit)
correct_total = [40+36, 77, 66, 72, 74]  # 76
overfitting_total = [0+62, 5, 0, 123, 0]  # 62

# Breakdown: Baseline only, Both, Added only
# DLFix: Baseline=35, Added=0, Both=0
# ARJA-e: Baseline=0, Added=0, Both=0 (no baseline labels found)
correct_baseline = [35+0, 9, 5, 7, 3]  # 35
correct_both = [0+0, 27, 30, 12, 28]  # 0
correct_added = [0+0, 2, 6, 1, 11]  # 0

# Overfitting
# DLFix: n=0
# ARJA-e: Baseline=0, Added=0, Both=0
overfitting_baseline = [0+0, 3, 0, 10, 0]  # 0
overfitting_both = [0+0, 0, 0, 1, 0]  # 0
overfitting_added = [0+0, 0, 0, 0, 0]  # 0

# Calculate percentages
def calc_pct(val, total):
    return 100 * val / total if total > 0 else 0

correct_baseline_pct = [calc_pct(v, t) for v, t in zip(correct_baseline, correct_total)]
correct_both_pct = [calc_pct(v, t) for v, t in zip(correct_both, correct_total)]
correct_added_pct = [calc_pct(v, t) for v, t in zip(correct_added, correct_total)]

overfitting_baseline_pct = [calc_pct(v, t) for v, t in zip(overfitting_baseline, overfitting_total)]
overfitting_both_pct = [calc_pct(v, t) for v, t in zip(overfitting_both, overfitting_total)]
overfitting_added_pct = [calc_pct(v, t) for v, t in zip(overfitting_added, overfitting_total)]

# Colors
baby_green = '#90EE90'
baby_red = '#FFB6C1'

# Create figure - smaller size as requested
fig, ax = plt.subplots(figsize=(10, 6))

x = np.arange(len(tools))
width = 0.30
gap = 0.04

# Stacked bars for CORRECT patches
# Bottom: Baseline only (backslash hatch)
bars1_base = ax.bar(x - width/2 - gap/2, correct_baseline_pct, width,
                    color=baby_green, edgecolor='black', linewidth=0.5,
                    hatch='\\\\\\\\')
# Middle: Both (cross-hatch)
bars1_both = ax.bar(x - width/2 - gap/2, correct_both_pct, width,
                    bottom=correct_baseline_pct,
                    color=baby_green, edgecolor='black', linewidth=0.5,
                    hatch='xxxx')
# Top: Added only (forward-slash hatch)
bars1_added = ax.bar(x - width/2 - gap/2, correct_added_pct, width,
                     bottom=[b + m for b, m in zip(correct_baseline_pct, correct_both_pct)],
                     color=baby_green, edgecolor='black', linewidth=0.5,
                     hatch='////')

# Stacked bars for OVERFITTING patches
bars2_base = ax.bar(x + width/2 + gap/2, overfitting_baseline_pct, width,
                    color=baby_red, edgecolor='black', linewidth=0.5,
                    hatch='\\\\\\\\')
bars2_both = ax.bar(x + width/2 + gap/2, overfitting_both_pct, width,
                    bottom=overfitting_baseline_pct,
                    color=baby_red, edgecolor='black', linewidth=0.5,
                    hatch='xxxx')
bars2_added = ax.bar(x + width/2 + gap/2, overfitting_added_pct, width,
                     bottom=[b + m for b, m in zip(overfitting_baseline_pct, overfitting_both_pct)],
                     color=baby_red, edgecolor='black', linewidth=0.5,
                     hatch='////')

# Customize the plot
ax.set_ylabel('Percentage of Patches that are Clones (%)', fontsize=12)
ax.set_xlabel('APR Tool (Year)', fontsize=12)
ax.set_xticks(x)
ax.set_xticklabels(tools, fontsize=10)
ax.set_ylim(0, 100)
ax.set_yticks(np.arange(0, 101, 10))
ax.tick_params(axis='y', labelsize=10)

# Remove top and right spines
ax.spines['top'].set_visible(False)
ax.spines['right'].set_visible(False)

# Create custom legend
green_patch = mpatches.Patch(facecolor=baby_green, edgecolor='black', label='Correct Patches')
red_patch = mpatches.Patch(facecolor=baby_red, edgecolor='black', label='Overfitting Patches')
baseline_patch = mpatches.Patch(facecolor='white', edgecolor='black', hatch='\\\\\\\\', label='Baseline Only')
both_patch = mpatches.Patch(facecolor='white', edgecolor='black', hatch='xxxx', label='Both')
added_patch = mpatches.Patch(facecolor='white', edgecolor='black', hatch='////', label='Added Tools Only')

ax.legend(handles=[green_patch, red_patch, baseline_patch, both_patch, added_patch],
          loc='upper left', frameon=True, edgecolor='black', fancybox=False,
          fontsize=9, title='Values: Base/Both/Added/Total', title_fontsize=8)

# Add value labels on bars
for i in range(len(tools)):
    # Calculate individual percentages
    c_base_pct = correct_baseline_pct[i]
    c_both_pct = correct_both_pct[i]
    c_added_pct = correct_added_pct[i]
    c_total_pct = c_base_pct + c_both_pct + c_added_pct
    
    o_base_pct = overfitting_baseline_pct[i]
    o_both_pct = overfitting_both_pct[i]
    o_added_pct = overfitting_added_pct[i]
    o_total_pct = o_base_pct + o_both_pct + o_added_pct
    
    # Matched counts
    c_matched = correct_baseline[i] + correct_both[i] + correct_added[i]
    o_matched = overfitting_baseline[i] + overfitting_both[i] + overfitting_added[i]
    
    # Correct patches label - 3 percentages and 4 numbers
    if c_total_pct > 0:
        label = f'{c_base_pct:.1f}%/{c_both_pct:.1f}%/{c_added_pct:.1f}%\n{correct_baseline[i]}/{correct_both[i]}/{correct_added[i]}/{c_matched}'
        ax.annotate(label,
                    xy=(x[i] - width/2 - gap/2, c_total_pct),
                    xytext=(0, 3), textcoords="offset points",
                    ha='center', va='bottom', fontsize=8)
    else:
        # Baseline - just show n
        label = f'n={correct_total[i]}'
        ax.annotate(label,
                    xy=(x[i] - width/2 - gap/2, 2),
                    xytext=(0, 0), textcoords="offset points",
                    ha='center', va='bottom', fontsize=8)
    
    # Overfitting patches label - 3 percentages and 4 numbers
    if overfitting_total[i] > 0:
        if o_total_pct > 0:
            label = f'{o_base_pct:.1f}%/{o_both_pct:.1f}%/{o_added_pct:.1f}%\n{overfitting_baseline[i]}/{overfitting_both[i]}/{overfitting_added[i]}/{o_matched}'
            ax.annotate(label,
                        xy=(x[i] + width/2 + gap/2, o_total_pct),
                        xytext=(0, 3), textcoords="offset points",
                        ha='center', va='bottom', fontsize=8)
        else:
            label = f'n={overfitting_total[i]}'
            ax.annotate(label,
                        xy=(x[i] + width/2 + gap/2, 2),
                        xytext=(0, 0), textcoords="offset points",
                        ha='center', va='bottom', fontsize=8)

# Add baseline info as text annotation
ax.text(0.98, 0.98, 'Baseline: 500 correct, 13,140 overfitting patches',
        transform=ax.transAxes, fontsize=9, verticalalignment='top',
        horizontalalignment='right', style='italic',
        bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.5))

# Tight layout
plt.tight_layout()

# Save figure
plt.savefig('rq4/temporal_redundancy.pdf', format='pdf', dpi=300, bbox_inches='tight')
plt.savefig('rq4/temporal_redundancy.png', format='png', dpi=300, bbox_inches='tight')

print("Figure saved successfully!")
print("Saved: rq4/temporal_redundancy.pdf")
print("Saved: rq4/temporal_redundancy.png")

# Print data summary
print("\nData summary:")
print(f"{'Tool':<20} {'Correct':<8} {'Base':<6} {'Both':<6} {'Added':<6} {'Total%':<8}")
for i, tool in enumerate(tools):
    tool_clean = tool.replace('\n', ' ')
    c_total_pct = correct_baseline_pct[i] + correct_both_pct[i] + correct_added_pct[i]
    print(f"{tool_clean:<20} {correct_total[i]:<8} {correct_baseline[i]:<6} {correct_both[i]:<6} {correct_added[i]:<6} {c_total_pct:<8.1f}")