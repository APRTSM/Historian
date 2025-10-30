# Replication Package for Historian

This repository provides the replication package "Historian".

## Overview
Historian is a tool to tackle this by formulating patch assessment as code clone detection and semantic similarity. It introduces a novel paradigm that formulates APCA as a multi-reference code clone detection problem against a historical knowledge base. 

<p align="center">
  <img src="https://anonymous.4open.science/api/repo/Historian-Artifact/file/rebutal/method.png" alt="Overview Figure">
</p>

- **Evidence-Based Decision Making**: Unlike black-box classifiers, Historian provides transparent verdicts by pointing to specific historical patches that match, enabling expert verification.

- **Self-Improving Architecture**: The reference set grows over time, making Historian progressively more powerful without expensive model retraining.

- **Principled Uncertainty Handling**: Unknown labels indicate insufficient historical evidence, directing human experts to cases requiring thorough evaluation.

- **Performance**: Historian demonstrates superior performance compared to existing SOTA methods (Acc/F1 W. Avg. 88.4/0.91 in Table 6 compared to 84.0/0.88 of SOTA in Table 7).

<p align="center">
  <img src="https://anonymous.4open.science/api/repo/Historian-Artifact/file/rebutal/main-results.png" alt="Performance Figure">
</p>

## Purpose and Scope
In the error-prone research environment of APR, our empirical studies show that many APR-generated patches are repetitive and exhibit human-understandable similarities (Type-1, Type-2 clones), making relabeling from scratch inefficient and unnecessary. The tool addresses the specific challenge of reducing redundant validation effort while maintaining high accuracy through evidence-based decision making.

## Requirements
- Python 3.10
- Java 1.8
- OS: Tested on Ubuntu Linux

Install Python dependencies:
```bash
pip install -r requirements.txt
```

Initialize submodules:
```bash
git submodule update --init --recursive
```

## Project Structure
```
├── benchmarks/                         # bug benchmarks used for evaluation
│   ├── bears/                          # Bears benchmark
│   ├── benchmarks.json                 # benchmarks configuration file
│   ├── bugsjar/                        # Bugs.jar benchmark
│   ├── defects4j/                      # Defects4J benchmark
│   ├── ID2commit-bugsjar/              # commit ID mappings for Bugs.jar
│   ├── introclassjava/                 # IntroClassJava benchmark
│   └── quixbugs/                       # QuixBugs benchmark
├── build.py                            # script to generate the files in tmp/results (RQ3 and RQ4)
├── classify.py                         # script to classify LLM responses
├── datasets/                           # historically validated APR patches
│   ├── aprenfl/                        # APR-ENF-L dataset
│   ├── datasets.json                   # datasets configuration file with links to datasets
│   ├── defectrepairing/                # DefectRepairing dataset
│   ├── dl4pc2/                         # DL4PC2 dataset
│   ├── drr/                            # DRR dataset
│   └── wangicse/                       # Wang ICSE dataset
├── __pycache__/                        # Python cache files
│   └── build.cpython-310.pyc           # compiled Python bytecode
├── rebutal/                            # includes materials mentioned in rebuttal response
├── requirements.txt                    # Python dependencies
├── results.json                        # refeneces to LLM responses 
├── results.py                          # script to aggregate (majority voting) results and generate plots
├── rq1_plots.py                        # script to generate plots for research question 1
├── rq1.py                              # script to generate results for research question 1
├── rq3_zeroshot_plots.py               # script to generate zero-shot classification performance plots
├── tmp/                                # results, logs, and intermediate files
│   ├── checkouts/                      # includes all generated documents
│   ├── data/                           # includes preprocessed data in each step
│   ├── logs/                           # execution logs
│   ├── methods/                        # extracted methods storage
│   ├── patches/                        # cleaned patches storage
│   ├── plots/                          # generated figures
│   └── results/                        # raw LLM responses and classification results
├── tools/                              # tools for patch analysis
│   ├── matching/                       # AST-based code clone detection tool
│   ├── ollama/                         # Ollama configurations
│   ├── SourcererCC/                    # SourcererCC code clone detection tool (text-based)
│   └── tools.json                      # list of tools and links
└── utils/                              # helper utilities for preprocessing and analysis
    ├── benchmark.py                    # API access to benchmark metadata
    ├── config.py                       # configuration (paths, keys)
    ├── dataset.py                      # interfaces for dataset access
    ├── __pycache__/                    # Python cache files
    ├── tool.py                         # scripts to wrap tools
    └── utils.py                        # utils
```

### Key Subdirectories in tmp/:
- `tmp/data/metadata/`: includes datasets, benchmarks and other metadata used in experiments
- `tmp/data/metadata/ollama/`: includes prompts and models used in experiments
- `tmp/results/classification/`: classified LLM outputs for research questions 3 and 4 (keywords EXP2 and EXP3 respectively)
- `tmp/results/rq1/`: labels for research question 1
- `tmp/results/expert/`: expert labels for research question 2 (EXP2-*.pkl files)

## RQ1
To generate results of the first research question:
```bash
python rq1.py
python rq1_plots.py
```

This will save the classified responses in `tmp/results/classified/` and generate plots in `tmp/plots/`.

## RQ2, RQ3 and RQ4
`tmp/results/` includes raw LLM responses in pickle files. To classify the LLM responses, aggregate votes and generate summary plots for 2nd, 3rd and 4th research questions:
```bash
python results.py
```

Results will be saved in `tmp/plots/`.

## RQ3
- Zero-Shot Classification Performance:
```bash
python rq3_zeroshot_plots.py
```

Results are saved in tmp/plots/rq3

## Regenerate Raw LLM Responses
Raw LLM responses are stored in `tmp/results/` as pickle files.

If you wish to generate LLM responses from scratch, instructions are provided in the following:

To reproduce the LLM responses if they do not already exist in `tmp/results/`, ensure that [Ollama](https://ollama.com/) is installed, the server is running and the desired models are pulled using:
```bash
ollama pull <model_name>
```

The list of required models is specified in `tmp/data/metadata/ollama/models.json`.

Then run the following:
```bash
python build.py
```

# Clean Patches and Methods
Cleaned patches and extracted methods are stored in `tmp/patches` and `tmp/methods` respectively. If you wish to regenerate them out of `datasets`, consider the following.

Initialize submodules:
```bash
git submodule update --init --recursive
```

Add [Defects4J](https://github.com/rjust/defects4j) in `benchmarks/defects4j` to the path and initialize it. For detailed instructions, please visit [Defects4J](https://github.com/rjust/defects4j):

Add IDs to Bugs.jar:
```bash
cp -r benchmarks/ID2commit-bugsjar benchmarks/bugsjar/ID2commit
```

Then run:
```bash
python build.py
```

Please note that this script will skip cleaning and extraction phases if the corresponding pickle files exist in `tmp/data`. These pickle files are generated (by `build.py`) after extraction and include metadata (e.g., location and ID of patches, methods, bugs, etc.)


## TMP 
results are here for table 6 Selecting best configuration
simple-results-exp2.csv
type-binary-results-exp2.csv