import matplotlib.pyplot as plt
import numpy as np
import os

# Create output directory
os.makedirs('rq5', exist_ok=True)

# Set up the figure with academic styling
plt.rcParams['font.family'] = 'serif'
plt.rcParams['font.size'] = 18
plt.rcParams['axes.linewidth'] = 0.8

# Data
tools = ['DLFix+ARJA-e\n(2020)', 'Recoder\n(2021)', 'CIRCLE\n(2022)', 'TransplantFix\n(2023)', 'ITER\n(2024)']

# Total patches
correct_total = [40+36, 77, 66, 72, 74]  # 76, 77, 66, 72, 74
overfitting_total = [0+62, 5, 0, 123, 0]  # 62, 5, 0, 123, 0

# Matched patches
correct_matched = [0, 29, 36, 13, 39]  # baseline=0
overfitting_matched = [0, 0, 0, 7, 0]  # baseline=0

# Calculate percentages
correct_clone_pct = [100 * m / t if t > 0 else 0 for m, t in zip(correct_matched, correct_total)]
overfitting_clone_pct = [100 * m / t if t > 0 else 0 for m, t in zip(overfitting_matched, overfitting_total)]

# Colors
baby_green = '#90EE90'
baby_red = '#FFB6C1'

# Create figure
fig, ax = plt.subplots(figsize=(10, 6))

x = np.arange(len(tools))
width = 0.25
gap = 0.03

# Create bars
bars1 = ax.bar(x - width/2 - gap/2, correct_clone_pct, width, 
               color=baby_green, edgecolor='black', linewidth=0.5)

bars2 = ax.bar(x + width/2 + gap/2, overfitting_clone_pct, width,
               color=baby_red, edgecolor='black', linewidth=0.5)

# Customize the plot
ax.set_ylabel('Percentage of Patches that are Clones (%)', fontsize=14)
ax.set_xlabel('APR Tool (Year)', fontsize=16)
ax.set_xticks(x)
ax.set_xticklabels(tools, fontsize=14)
ax.set_ylim(0, 100)
ax.set_yticks(np.arange(0, 101, 10))
ax.tick_params(axis='y', labelsize=14)

# Remove top and right spines
ax.spines['top'].set_visible(False)
ax.spines['right'].set_visible(False)

# Legend
ax.legend([bars1, bars2], ['Correct Patches', 'Overfitting Patches'], 
          loc='upper left', frameon=True, edgecolor='black', fancybox=False,
          fontsize=12)

# Add value labels on bars
def add_labels(x_pos, pct, m, n):
    # If percentage is 0 (like baseline), just show m/n
    # If percentage > 0, show pct% and m/n
    if pct > 0:
        label = f'{pct:.1f}%\n{m}/{n}'
        y_pos = pct
    else:
        label = f'{m}/{n}'
        y_pos = 0  # Place at the bottom
        
    ax.annotate(label,
                xy=(x_pos, y_pos),
                xytext=(0, 3), textcoords="offset points",
                ha='center', va='bottom', fontsize=11)

for i in range(len(tools)):
    # Correct Patches
    add_labels(x[i] - width/2 - gap/2, 
               correct_clone_pct[i], 
               correct_matched[i], 
               correct_total[i])
    
    # Overfitting Patches
    add_labels(x[i] + width/2 + gap/2, 
               overfitting_clone_pct[i], 
               overfitting_matched[i], 
               overfitting_total[i])

# Tight layout
plt.tight_layout()

# Save figure
plt.savefig('rq5/temporal_redundancy.pdf', format='pdf', dpi=300, bbox_inches='tight')
plt.savefig('rq5/temporal_redundancy.png', format='png', dpi=300, bbox_inches='tight')

print("Figure saved successfully!")
print("Path: rq4/temporal_redundancy.pdf")
print("Path: rq4/temporal_redundancy.png")