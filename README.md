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

To reproduce the experiments and generate results:
```bash
python build.py
```

To generate plots and summary figures:
```bash
python results.py
```
