# Replication Package for Historian

This repository provides the replication package "Historian".

## Requirements

- Python 3.10
- Java 1.8
- OS: Tested on Ubuntu Linux

Install Python dependencies:
```bash
pip install -r requirements.txt
```

## Structure

```
benchmarks/: bug benchmarks used for evaluation
datasets/: historically validated APR patches
tools/: tools for patch analysis
utils/: helper utilities for preprocessing and analysis
tmp/: results, logs, and intermediate files
tmp/data: includes preprocessed data in each step
tmp/data/metadata: includes datasets, benchmarks and other metadata used in experiments
tmp/data/metadata/ollama: includes prompts and models used in experiments
tmp/results/classification: classified LLM outputs for research questions 3 and 4 (the keywords EXP2 and EXP3 respectively)
tmp/results/rq1: labels for research question 1
tmp/results/expert: expert labels for research question 2 (EXP2-*.pkl)
tmp/plots: generated figures
tmp/logs: execution logs
tmp/config.py: configuration (paths, keys)
tmp/benchmarks.py: API access to benchmark metadata
tmp/datasets.py: interfaces for dataset access
build.py: script to generate the files in tmp/results
results.py: script to aggregate (majority voting) results (generate files in tmp/results/classification) and generate plots in tmp/plots
```

## RQ1

To generate results of the first research question:
```bash
python rq1.py
python rq1_plots.py
```

This will save the classified responses in `tmp/results/classified/` and generate plots in `tmp/plots/`.

## RQ2, RQ3 and RQ4

`tmp/results/` includes raw LLM responses in pickle files. To classify the LLM responses, aggrigate votes and generate summary plots for 2nd, 3rd and 4th research questions:
```bash
python results.py
```

Results will be saved in `tmp/plots/`.

## Regenerate Raw LLM Responses

Raw LLM responses are stored in `tmp/results/` as pickle files.

If you wish to generate LLM responses from scratch instructions are provided in the following:

To reproduce the LLM responses if they do not already exist in `tmp/results/`, ensure that [`Ollama`](https://ollama.com/) is installed, server is running and the desired models are pulled using:
```bash
ollama pull <model_name>
```
The list of required models is specified in `tmp/data/metadata/ollama/models.json`.

Then run the following:
```bash
python build.py
```

# Clean Patches and Methods

Cleaned patches and extracted methods are stored in `tmp/patches` and `tmp/methods` respectively. If you wish to regenerate them out of `datsets` consider the following. 

Initialize submodules:
```bash
git submodule update --init --recursive
```

Add [`Defects4J`](https://github.com/rjust/defects4j) in `benchmarks/defects4j` to the path and initialize it, for detailed instructions please visit [`Defects4J`](https://github.com/rjust/defects4j):
Add IDs to Bugs.jar,
```bash
cp -r benchmarks/ID2commit-bugsjar benchmarks/bugsjar/ID2commit
```
Then run:
```bash
python build.py
```
Please note that this script will skip cleaning and extraction phases if the corresponding pickle files exist in `tmp/data`. These pickle files are generated (by `build.py`) after extraction and include metadata (e.g. location and ID of patches, methods, bugs and etc.)