 README.md (Markdown)

# Replication Package for Historian

This repository provides the replication package "Historian".

## Requirements

- Python 3.10
- Java 1.8
- OS: Tested on Ubuntu Linux

Install Python dependencies:

```bash
pip install -r requirements.txt

Initialize submodules:

git submodule update --init --recursive
```

## Structure

    benchmarks/: bug benchmarks used for evaluation

    datasets/: historically validated APR patches

    tools/: tools for patch analysis

    utils/: helper utilities for preprocessing and analysis

    tmp/: results, logs, and intermediate files

        tmp/results/: experiment outputs

        tmp/plots/: generated figures

        tmp/logs/: execution logs

    tmp/config.py: configuration (paths, keys)

    tmp/benchmarks.py: API access to benchmark metadata

    tmp/datasets.py: interfaces for dataset access

    build.py: script to generate all results

    results.py: script to aggregate results and generate plots


## Usage

To reproduce the experiments and generate results (including LLM responses if they do not already exist in `tmp/results/`):
```bash
python build.py

```
If you wish to regenerate LLM responses, ensure that [`Ollama`](https://ollama.com/) is installed and the desired models are pulled using:
```bash
ollama pull <model_name>
```
The list of required models is specified in tools/ollama/models.json.

To classify the LLM responses and generate summary plots:
```bash
python results.py
```
This will save the classified responses in `tmp/results/classified/` and generate plots in `tmp/plots/`.

Experiments are implemented as functions or classes in `results.py`, and the function calls in `build.py` are commented out. To run a specific experiment, please uncomment the corresponding function call.


## TMP

Add `ID2commit` to bugsjar