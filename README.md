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

Initialize submodules:
```bash
git submodule update --init --recursive
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
results.py: script to aggregate results (generate files in tmp/results/classification) and generate plots in tmp/plots
```

## Usage

To reproduce the experiments and generate results (including LLM responses if they do not already exist in `tmp/results/`):
```bash
python build.py
```

If you wish to regenerate LLM responses, ensure that [`Ollama`](https://ollama.com/) is installed and the desired models are pulled using:
```bash
ollama pull <model_name>
```

The list of required models is specified in `tmp/data/metadata/ollama/models.json`.

To classify the LLM responses and generate summary plots:
```bash
python results.py
```
This will save the classified responses in `tmp/results/classified/` and generate plots in `tmp/plots/`.

To generate results of the first research question:
```bash
python rq1.py
python rq1_plots.py
```

