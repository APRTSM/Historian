import json
import os
from .utils import execute_bash_command
from .config import *


""" EvoSuite """
def generate_test_evosuite(repo_dir, modified_class):
    execute_bash_command("mvn compile", dir=repo_dir)
    execute_bash_command(f"$EVOSUITE -class {modified_class} -projectCP target/classes", dir=repo_dir)    
    class_path = '/'.join(modified_class.split(".")[:-1])
    command = f"""
    mvn dependency:copy-dependencies;
    export CLASSPATH=target/classes:~/evosuite/v1.2.0/evosuite-standalone-runtime-1.2.0.jar:evosuite-tests:target/dependency/*;
    javac evosuite-tests/{class_path}/*.java;
    java org.junit.runner.JUnitCore {modified_class}_ESTest;
    """
    results, _ = execute_bash_command(command, dir=repo_dir)
    
    return results

def run_tests_on_candidate_evosuite(fixed_repo_dir, buggy_repo_dir, modified_class):
    execute_bash_command(f"cp -r evosuite-tests {buggy_repo_dir}", dir=fixed_repo_dir)
    execute_bash_command("mvn compile", dir=buggy_repo_dir)
    class_path = '/'.join(modified_class.split(".")[:-1])
    command = f"""
    mvn dependency:copy-dependencies;
    export CLASSPATH=target/classes:~/evosuite/v1.2.0/evosuite-standalone-runtime-1.2.0.jar:evosuite-tests:target/dependency/*;
    java org.junit.runner.JUnitCore {modified_class}_ESTest;
    """
    results, _ = execute_bash_command(command, dir=buggy_repo_dir, error_allowed=True)

    return results


""" Ollama """
def ollama_get_settings():
    # Output is formatted as the json file. Refer to `prompts.json`
    with open(OLLAMA_PROMPTS_JSON, 'r') as file:
        prompts = json.load(file)   

    with open(OLLAMA_MODELS_JSON, 'r') as file:
        models = json.load(file)   

    with open(OLLAMA_TEMPERATURES_JSON, 'r') as file:
        temperatures = json.load(file)  

    return prompts, models, temperatures



""" SourcererCC """
def sourcerercc_are_clones(code_1, code_2):
    src_1_dir = os.path.join(SOURCERERCC_DIR, "tokenizer-input", "src-1", "src.java")
    src_2_dir = os.path.join(SOURCERERCC_DIR, "tokenizer-input", "src-2", "src.java")

    with open(src_1_dir, "w") as f:
        f.write(code_1)

    with open(src_2_dir, "w") as f:
        f.write(code_2)
        
    execute_bash_command("./run.sh", dir=SOURCERERCC_DIR)

    file_path = os.path.join(SOURCERERCC_DIR, "results.pairs")

    if os.path.getsize(file_path) == 0:
        return False
    
    else:
        return True
    

""" Matching """
def matching_are_clones(code_1, code_2):
    # Implement the matching logic here
    output, _ = execute_bash_command(f'python wrapper.py -c1 "{code_1}" -c2 "{code_2}"', dir=MATCHING_DIR, error_allowed=True)

    return int(output)