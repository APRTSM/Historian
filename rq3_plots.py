import re

def calculate_coverage(latex_string):
    lines = latex_string.strip().split('\n')
    output_lines = []
    
    for line in lines:
        # Skip non-data lines (headers, hlines, etc.)
        if not re.search(r'^\w+\s*&\s*\d+:\d+', line):
            # Replace header labels
            line = line.replace('\\#U', 'Cov')
            line = line.replace('With Unknown', 'With Coverage')
            output_lines.append(line)
            continue
        
        # Extract C:O ratio
        co_match = re.search(r'(\d+):(\d+)', line)
        if not co_match:
            output_lines.append(line)
            continue
            
        correct = int(co_match.group(1))
        overfitting = int(co_match.group(2))
        total = correct + overfitting
        
        # Split the line by & to get columns
        parts = line.split('&')
        
        # Column indices for Unknown values: 6 (Qwen) and 11 (Gemini)
        for idx in [6, 11]:
            if idx < len(parts):
                part = parts[idx].strip()
                # Match a standalone number
                unknown_match = re.match(r'^(\d+)\s*(\\\\)?$', part)
                if unknown_match:
                    unknown = int(unknown_match.group(1))
                    coverage = ((total - unknown) / total) * 100 if total > 0 else 0
                    # Preserve the ending
                    ending = ' ' + unknown_match.group(2) if unknown_match.group(2) else ''
                    parts[idx] = f' {coverage:.1f}\{ending}'
        
        # Reconstruct the line
        output_lines.append(' & '.join(parts))
    
    return '\n'.join(output_lines)


# Example usage
latex_table = r"""
\begin{table}[htbp]
\centering
\tiny
\setlength{\tabcolsep}{1pt}
\renewcommand{\arraystretch}{0.9}
\caption{\toolname\ with a Commercial LLM}
\label{tab:gemini-comparison}
\resizebox{0.85\linewidth}{!}{
\begin{tabular}{lr|cc|ccc|cc|ccc}
\multirow{3}{}{\textbf{Tool}} & \multirow{3}{*}{\textbf{C:O}} & \multicolumn{5}{c|}{$\toolname^{\textbf{Qwen}}$} & \multicolumn{5}{c}{$\toolname^{\textbf{Gemini}}$}\\
\hline
 &  & \multicolumn{2}{c|}{Punished} & \multicolumn{3}{c|}{With Unknown} & \multicolumn{2}{c|}{Punished} & \multicolumn{3}{c}{With Unknown}\\
\hline
 & & \textbf{Acc} & \textbf{F1} & \textbf{Acc} & \textbf{F1} & \textbf{\#U} & \textbf{Acc} & \textbf{F1} & \textbf{Acc} & \textbf{F1} & \textbf{\#U}\\
\hline
ACS        & 29:8   & 52.8 & .49 & \cellcolor{blue!15}\textbf{61.3} & \cellcolor{blue!15}\textbf{.57} & 5 & 52.8 & .45 & 59.4 & .52 & 4 \\
Arja       & 8:49   & 86 & .92 & 89.1 & .94 & 2 & 91.2 & .95 & \cellcolor{blue!15}\textbf{92.9} & \cellcolor{blue!15}\textbf{.96} & 1 \\
AVATAR     & 17:37  & 72.2 & .81 & \cellcolor{blue!15}\textbf{90.7} & \cellcolor{blue!15}\textbf{.94} & 11 & 83.3 & .89 & 90 & \cellcolor{blue!15}\textbf{.94} & 1 \\
CapGen     & 9:41   & 79.2 & .88 & 86.4 & .92 & 4 & 85.4 & .91 & \cellcolor{blue!15}\textbf{100} & \cellcolor{blue!15}\textbf{1} & 7 \\
Cardumen   & 0:9    & \cellcolor{blue!15}\textbf{100} & \cellcolor{blue!15}\textbf{1} & \cellcolor{blue!15}\textbf{100} & \cellcolor{blue!15}\textbf{1}& 0 & \cellcolor{blue!15}\textbf{100} & \cellcolor{blue!15}\textbf{1} & \cellcolor{blue!15}\textbf{100} & \cellcolor{blue!15}\textbf{1} & 0 \\
DynaMoth   & 1:21   & 95.5 & .98 & \cellcolor{blue!15}\textbf{100} & \cellcolor{blue!15}\textbf{1} & 1 & 95.5 & .98 & 95.5 & .98 & 0 \\
FixMiner   & 6:19   & 80 & .87 & \cellcolor{blue!15}\textbf{90.9} & \cellcolor{blue!15}\textbf{.94} & 3 & 80 & .87 & \cellcolor{blue!15}\textbf{90.9} & \cellcolor{blue!15}\textbf{.94} & 3 \\
GenProg    & 1:24   & 92 & .96 & 95.8 & .98 & 1 & \cellcolor{blue!15}\textbf{100} & \cellcolor{blue!15}\textbf{1} & \cellcolor{blue!15}\textbf{100} & \cellcolor{blue!15}\textbf{1} & 0 \\
HDRepair   & 4:4    & 87.5 & .89 & \cellcolor{blue!15}\textbf{100} & \cellcolor{blue!15}\textbf{1} & 1 & 87.5 & .89 & \cellcolor{blue!15}\textbf{100} & \cellcolor{blue!15}\textbf{1} & 1 \\
Jaid       & 32:40  & 62.5 & .73 & 67.3 & .77 & 4 & 67.9 & .78 & \cellcolor{blue!15}\textbf{76} & \cellcolor{blue!15}\textbf{.84} & 6 \\
jGenProg   & 6:33   & 73 & .84 & 79.4 & .89 & 3 & 86.5 & .93 & \cellcolor{blue!15}\textbf{94.1} & \cellcolor{blue!15}\textbf{.97} & 3 \\
jKali      & 4:31   & 80 & .89 & 84.8 & \cellcolor{blue!15}\textbf{.92} & 2 & 82.9 & .90 & \cellcolor{blue!15}\textbf{85.3} & \cellcolor{blue!15}\textbf{.92} & 1 \\
jMutRepair & 2:14   & 81.3 & .89 & 81.3 & .89 & 0 & \cellcolor{blue!15}\textbf{93.8} & \cellcolor{blue!15}\textbf{.97} & \cellcolor{blue!15}\textbf{93.8} & \cellcolor{blue!15}\textbf{.97} & 0 \\
Kali       & 2:36   & \cellcolor{blue!15}\textbf{94.7} & .97 & \cellcolor{blue!15}\textbf{94.7} & .97 & 0 & \cellcolor{blue!15}\textbf{97.4} & \cellcolor{blue!15}\textbf{.99} & \cellcolor{blue!15}\textbf{97.4} & \cellcolor{blue!15}\textbf{.99} & 0 \\
kPAR       & 2:32   & 82.4 & .90 & 90.3 & .95 & 3 & 85.3 & .92 & \cellcolor{blue!15}\textbf{93.5} & \cellcolor{blue!15}\textbf{.97} & 3 \\
Nopol      & 6:89   & 88 & .94 & 95.7 & .98 & 4 & \cellcolor{blue!15}\textbf{100} & \cellcolor{blue!15}\textbf{1} & \cellcolor{blue!15}\textbf{100} & \cellcolor{blue!15}\textbf{1} & 0 \\
RSRepair   & 2:31   & 87.9 & .94 & 87.9 & .94 & 0 & \cellcolor{blue!15}\textbf{97} & \cellcolor{blue!15}\textbf{.98} & \cellcolor{blue!15}\textbf{97} & \cellcolor{blue!15}\textbf{.98} & 0 \\
SequenceR  & 10:45  & 72.7 & .81 & \cellcolor{blue!15}\textbf{78} & \cellcolor{blue!15}\textbf{.85} & 3 & 65.9 & .75 & 72.5 & .81 & 4 \\
SimFix     & 16:42  & 65.5 & .77 & 73.1 & .83 & 6 & 81 & .88 & \cellcolor{blue!15}\textbf{83.9} & \cellcolor{blue!15}\textbf{.90} & 2 \\
SketchFix  & 5:7    & 66.7 & .71 & 72.7 & \cellcolor{blue!15}\textbf{.77} & 1 & \cellcolor{blue!15}\textbf{75} & \cellcolor{blue!15}\textbf{.77} & \cellcolor{blue!15}\textbf{75} & \cellcolor{blue!15}\textbf{.77} & 0 \\
SOFix      & 10:1   & 45.5 & .25 & \cellcolor{blue!15}\textbf{62.5} & \cellcolor{blue!15}\textbf{.40} & 3 & 36.4 & .22 & 50 & .33 & 3 \\
TBar       & 7:33   & 82.5 & .89 & \cellcolor{blue!15}\textbf{94.3} & \cellcolor{blue!15}\textbf{.97} & 5 & 85 & .91 & 87.2 & .92 & 2 \\
\hline
\textbf{Avg.} & - & 78.5 & .83 & 85.3 & .88 & - & 83.2 & .86 & \cellcolor{blue!15}\textbf{87.9} & \cellcolor{blue!15}\textbf{.90} & - \\
\textbf{W. Avg.} & - & 77.8 & .84 & 84.7 & .89 & - & 83.8 & .88 & \cellcolor{blue!15}\textbf{88.4} & \cellcolor{blue!15}\textbf{.91} & - \\
\textbf{Total} & 179:646 & - & - & - & - & 62 (8\%) & - & - & - & - & 41 (5\%) \\
\hline
\end{tabular}}
\parbox{0.98\linewidth}{\scriptsize \textbf{C:O:} Correct:Overfitting, \textbf{Acc:} Accuracy (\%), \textbf{U:} Unknown, \textbf{W:} Weighted}
\end{table}
"""

result = calculate_coverage(latex_table)
print(result)