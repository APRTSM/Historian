import os
import logging
import time
from tqdm import tqdm
from datetime import datetime

FORMATTED_DATE_TIME = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

JAVA_7_HOME = "/usr/lib/jvm/java-7-oracle"
JAVA_8_HOME = "/usr/lib/jvm/java-8-openjdk-amd64"

""" Directories """
PROJECT_DIR = os.getcwd()

# RESEARCH QUESTIONS
RQ4_DIR = os.path.join(PROJECT_DIR, "rq4")
RQ4_DATA_DIR = os.path.join(RQ4_DIR, "tool_patches")
RQ4_FIRST_CLEANED_DATA_DIR = os.path.join(RQ4_DIR, "first_cleaned_tool_patches")
RQ4_SECOND_CLEANED_DATA_DIR = os.path.join(RQ4_DIR, "second_cleaned_tool_patches")
RQ4_META_DATA_DIR = os.path.join(RQ4_DIR, "patches_metadata")
TMP_CLEANED_NEW_PATHCES_PKL = os.path.join(RQ4_DIR, "cleaned_new_patches.pkl")
TMP_METHOD_NEW_PATHCES_PKL = os.path.join(RQ4_DIR, "method_new_patches.pkl")
TMP_GENERATOR_NORMALIZED_NEW_PATHCES_PKL = os.path.join(RQ4_DIR, "generator_normalized_new_patches.pkl")
TMP_SINGLE_HUNK_NEW_PATHCES_PKL = os.path.join(RQ4_DIR, "single_hunk_new_patches.pkl")
TMP_DEDUPLICATED_NEW_PATHCES_PKL = os.path.join(RQ4_DIR, "deduplicated_new_patches.pkl")

RQ5_DIR = os.path.join(PROJECT_DIR, "rq5")
TMP_CLEANED_LLM4PC_PATCHES_PKL = os.path.join(RQ5_DIR, "cleaned_llm4pc_patches.pkl")
TMP_LLM4PC_METHODS_PKL = os.path.join(RQ5_DIR, "llm4pc_methods.pkl")
TMP_LLM4PC_NORMALIZED_NAMES_PKL = os.path.join(RQ5_DIR, "llm4pc_normalized_names.pkl")
TMP_LLM4PC_SINGLE_METHODS_PKL = os.path.join(RQ5_DIR, "llm4pc_single_methods.pkl")
TMP_LLM4PC_FILES_PKL = os.path.join(RQ5_DIR, "llm4pc_files.pkl")
LLM4PC_TOOL_PATCHES_DIR = os.path.join(RQ5_DIR, "tool-patches.pkl")
TMP_TYPE_BINARY_RESULTS_CSV_EXP5 = os.path.join(RQ5_DIR, "type-binary-results-exp5.csv")

# DATASETS
DATASETS_DIR = os.path.join(PROJECT_DIR, "datasets")
DL4PC2_DIR = os.path.join(DATASETS_DIR, "dl4pc2")
APRE_NFL_DIR = os.path.join(DATASETS_DIR, "aprenfl")
APRE_PFL_DIR = os.path.join(DATASETS_DIR, "aprepfl")
DEFECTREPAIRING_DIR = os.path.join(DATASETS_DIR, "defectrepairing")
DRR_DIR = os.path.join(DATASETS_DIR, "drr")
WANGICSE_DIR = os.path.join(DATASETS_DIR, "wangicse")
HISTORIAN_DIR = os.path.join(DATASETS_DIR, "historian")
CSMALL_DIR = os.path.join(DATASETS_DIR, "csmall")
LLM4PC_DIR = os.path.join(DATASETS_DIR, "llm4pc")

# BENCHMARKS
BENCHMARKS_DIR = os.path.join(PROJECT_DIR, "benchmarks")
DEFECTS4J_DIR = os.path.join(BENCHMARKS_DIR, "defects4j")
DEFECTS4J_DIR_15 = os.path.join(BENCHMARKS_DIR, "defects4j15")
BUGSJAR_DIR = os.path.join(BENCHMARKS_DIR, "bugsjar")
QUIXBUGS_DIR = os.path.join(BENCHMARKS_DIR, "quixbugs")
BEARS_DIR = os.path.join(BENCHMARKS_DIR, "bears")
INTROCLASSJAVA_DIR = os.path.join(BENCHMARKS_DIR, "introclassjava")

# TOOLS
TOOLS_DIR = os.path.join(PROJECT_DIR, "tools")
OLLAMA_DIR = os.path.join(TOOLS_DIR, "ollama")
OLLAMA_PROMPTS_JSON =  os.path.join(OLLAMA_DIR, "prompts.json")
OLLAMA_MODELS_JSON = os.path.join(OLLAMA_DIR, "models.json")
OLLAMA_TEMPERATURES_JSON = os.path.join(OLLAMA_DIR, "temperatures.json")
SOURCERERCC_DIR = os.path.join(TOOLS_DIR, "SourcererCC")
MATCHING_DIR = os.path.join(TOOLS_DIR, "matching")

# SETTINGS
SETTINGS_FILE = os.path.join(PROJECT_DIR, "settings.xml")
BENCHMARKS_JSON = os.path.join(BENCHMARKS_DIR, "benchmarks.json")
DATASETS_JSON = os.path.join(DATASETS_DIR, "datasets.json")
TOOLS_JSON = os.path.join(TOOLS_DIR, "tools.json")

INPUT_DIR = os.path.join(PROJECT_DIR, "input")

# TMP
TMP_DIR = os.path.join(PROJECT_DIR, "tmp")
TMP_CHECKOUTS_DIR = os.path.join(TMP_DIR, "checkouts")
TMP_METHODS_DIR = os.path.join(TMP_DIR, "methods")
TMP_FILES_DIR = os.path.join(TMP_DIR, "files")
TMP_PATCHES_DIR = os.path.join(TMP_DIR, "patches")
TMP_DATA_DIR = os.path.join(TMP_DIR, "data")
TMP_RESULTS_DIR = os.path.join(TMP_DIR, "results")
TMP_PLOTS_DIR = os.path.join(TMP_DIR, "plots")

TMP_RQ1_DATA_DIR = os.path.join(TMP_DATA_DIR, "rq1")


## Initial Data
TMP_RQ1_RESULTS_DIR = os.path.join(TMP_RESULTS_DIR, "rq1")
TMP_RQ2_RESULTS_DIR = os.path.join(TMP_RESULTS_DIR, "rq2")

TMP_FORMATTED_PATCH_DIR = os.path.join(TMP_PATCHES_DIR, "cleaned")
TMP_DEVELOPER_PATCH_DIR = os.path.join(TMP_PATCHES_DIR, "raw")

TMP_META_DATA = os.path.join(TMP_DATA_DIR, "metadata")
TMP_SETTINGS_FILE = os.path.join(TMP_META_DATA, "settings.xml") 
TMP_BENCHMARKS_JSON = os.path.join(TMP_META_DATA, "benchmarks.json")
TMP_DATASETS_JSON = os.path.join(TMP_META_DATA, "datasets.json")
TMP_TOOLS_JSON = os.path.join(TMP_META_DATA, "tools.json")

TMP_BUGS_PKL = os.path.join(TMP_DATA_DIR, "bugs.pkl")
TMP_DEVELOPER_PATHCES_PKL = os.path.join(TMP_DATA_DIR, "developer-patches.pkl")
TMP_TOOL_PATHCES_PKL = os.path.join(TMP_DATA_DIR, "tool-patches.pkl")
TMP_CLEANED_DEVELOPER_PATHCES_PKL = os.path.join(TMP_DATA_DIR, "cleaned-developer-patches.pkl")
TMP_CLEANED_TOOL_PATHCES_PKL = os.path.join(TMP_DATA_DIR, "cleaned-tool-patches.pkl")
TMP_METHOD_DEVELOPER_PATHCES_PKL = os.path.join(TMP_DATA_DIR, "method-developer-patches.pkl")
TMP_METHOD_TOOL_PATHCES_PKL = os.path.join(TMP_DATA_DIR, "method-tool-patches.pkl")
TMP_GENERATOR_NORMALIZED_DEVELOPER_PATHCES_PKL = os.path.join(TMP_DATA_DIR, "generator-normalized-developer-patches.pkl")
TMP_GENERATOR_NORMALIZED_TOOL_PATHCES_PKL = os.path.join(TMP_DATA_DIR, "generator-normalized-tool-patches.pkl")
TMP_SINGLE_HUNK_TOOL_PATHCES_PKL = os.path.join(TMP_DATA_DIR, "single-hunk-tool-patches.pkl")
TMP_SINGLE_HUNK_DEVELOPER_PATHCES_PKL = os.path.join(TMP_DATA_DIR, "single-hunk-developer-patches.pkl")
TMP_DEDUPLICATED_TOOL_PATHCES_PKL = os.path.join(TMP_DATA_DIR, "deduplicated-tool-patches.pkl")
TMP_SECOND_DEDUPLICATED_DEVELOPER_PATHCES_PKL = os.path.join(TMP_DATA_DIR, "second-deduplicated-developer-patches.pkl")
TMP_SECOND_DEDUPLICATED_TOOL_PATHCES_PKL = os.path.join(TMP_DATA_DIR, "second-deduplicated-tool-patches.pkl")

## Patch Processing
TMP_PROCESSORS_JSON = os.path.join(TMP_META_DATA, "processors.json")

## Tool Settings 
TMP_OLLAMA_DIR = os.path.join(TMP_META_DATA, "ollama")
TMP_OLLAMA_PROMPTS_JSON =  os.path.join(TMP_OLLAMA_DIR, "prompts.json")
TMP_OLLAMA_MODELS_JSON = os.path.join(TMP_OLLAMA_DIR, "models.json")
TMP_OLLAMA_TEMPERATURES_JSON = os.path.join(TMP_OLLAMA_DIR, "temperatures.json")

# Processed Data
TMP_TEST_RESULTS_DIR = os.path.join(TMP_RESULTS_DIR, "test")
TMP_CLASSIFICATION_RESULTS_DIR = os.path.join(TMP_RESULTS_DIR, "classification")
TMP_EXPERT_LABEL_DIR = os.path.join(TMP_RESULTS_DIR, "expert")
TMP_EXPERT_CORRECT_LABEL_PKL = os.path.join(TMP_EXPERT_LABEL_DIR, "correct.pkl")
TMP_EXPERT_CORRECT_LABEL_PKL_EXP2 = os.path.join(TMP_EXPERT_LABEL_DIR, "EXP2-labeled-tbar.pkl")

# Plots
TMP_RESULTS_JSON_FILE = os.path.join(TMP_PLOTS_DIR, "results.json")
TMP_BOOLEAN_RESULTS_JSON_FILE = os.path.join(TMP_PLOTS_DIR, "boolean-results.json")

TMP_COHENS_KAPPA_CSV = os.path.join(TMP_PLOTS_DIR, "cohens-kappa-exp1.csv")
TMP_SIMPLE_RESULTS_CSV = os.path.join(TMP_PLOTS_DIR, "simple-results-exp1.csv")
TMP_TYPE_RESULTS_CSV = os.path.join(TMP_PLOTS_DIR, "type-results-exp1.csv")

TMP_SIMPLE_RESULTS_CSV_EXP2 = os.path.join(TMP_PLOTS_DIR, "simple-results-exp2.csv")
TMP_TYPE_BINARY_RESULTS_CSV_EXP2 = os.path.join(TMP_PLOTS_DIR, "type-binary-results-exp2.csv")
TMP_TYPE_RESULTS_CSV_EXP2 = os.path.join(TMP_PLOTS_DIR, "type-results-exp2.csv")

TMP_TYPE_BINARY_EXPER_LABEL_RESULTS_CSV_EXP2 = os.path.join(TMP_PLOTS_DIR, "type-binary-expert-label-results-exp2.csv")
TMP_TYPE_BINARY_VOTED_EXPER_LABEL_RESULTS_EXP2 = os.path.join(TMP_PLOTS_DIR, "type-binary-expert-voted-label-results-exp2.txt")

TMP_SIMPLE_CLONE_RESULTS_CSV_EXP2 = os.path.join(TMP_PLOTS_DIR, "simple-clone-results-exp2.csv")

TMP_SIMPLE_RESULTS_CSV_EXP3 = os.path.join(TMP_PLOTS_DIR, f"simple-results-exp3.csv")
TMP_SIMPLE_RESULTS_CSV_EXP4 = os.path.join(TMP_PLOTS_DIR, f"simple-results-exp4.csv")
TMP_TYPE_BINARY_RESULTS_CSV_EXP3 = os.path.join(TMP_PLOTS_DIR, f"type-binary-results-exp3.csv")
TMP_TYPE_BINARY_RESULTS_CSV_EXP4 = os.path.join(TMP_PLOTS_DIR, f"type-binary-results-exp4.csv")

TMP_RQ2_PLOTS_DIR = os.path.join(TMP_PLOTS_DIR, "rq2")
TMP_RQ3_PLOTS_DIR = os.path.join(TMP_PLOTS_DIR, "rq3")

LOG_DIR = os.path.join(TMP_DIR, "logs")
LOG_FILE = os.path.join(LOG_DIR, f"{FORMATTED_DATE_TIME}.log")

""" Naming Conventions """
SEPERATOR = '_'
PATCH_FILE_SUFFIX = ".patch"


""" Other Configurations """
logging.basicConfig(
    filename=LOG_FILE, 
    filemode='w',
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    level=logging.INFO,
    datefmt='%Y-%m-%d %H:%M:%S'
)
tqdm.pandas(desc="Unknown Process.")


""" DEFECTS4J Settings """
DEPRICATED_BUGS = [
    "defects4j-Closure-93",
    "defects4j-Closure-63",
]

DEPRICATED_BUGS_2 = [
    "defects4j-Lang-2",
    "defects4j-Time-21",
    "defects4j-Cli-6",
    "defects4j-Collections-1",
    "defects4j-Collections-2",
    "defects4j-Collections-3",
    "defects4j-Collections-4",
    "defects4j-Collections-5",
    "defects4j-Collections-6",
    "defects4j-Collections-7",
    "defects4j-Collections-8",
    "defects4j-Collections-9",
    "defects4j-Collections-10",
    "defects4j-Collections-11",
    "defects4j-Collections-12",
    "defects4j-Collections-13",
    "defects4j-Collections-14",
    "defects4j-Collections-15",
    "defects4j-Collections-16",
    "defects4j-Collections-17",
    "defects4j-Collections-18",
    "defects4j-Collections-19",
    "defects4j-Collections-20",
    "defects4j-Collections-21",
    "defects4j-Collections-22",
    "defects4j-Collections-23",
    "defects4j-Collections-24",
]

DEFECTS4J_ERROR_BUGS = [
    "defects4j-Math-12",
    "defects4j-Closure-107",
]

if __name__=="__main__":
    pass
