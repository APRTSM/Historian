import os
import csv
import shutil
import logging
import json
import time
from .utils import *
from .config import *
import pandas as pd


""" Defects4J """
# Configuring Defects4J.
def configure_defects4j():    
    # Installing dependencies
    execute_bash_command("cpanm --installdeps .", DEFECTS4J_DIR)

    # Initializing Defects4J
    execute_bash_command("./init.sh", DEFECTS4J_DIR)

    # Adding Defects4J's executables to the PATH variable
    if not os.path.normpath(f"{DEFECTS4J_DIR}/framework/bin") in os.environ["PATH"]:
        current_path_variable = os.environ["PATH"]
        os.environ["PATH"] = os.path.normpath(f"{current_path_variable}{os.pathsep}{DEFECTS4J_DIR}/framework/bin")

    # Checking installation
    execute_bash_command("defects4j info -p Lang")
    
# Output of this function should be a list of dictionaries (bugs). Include keys that you want to see in the list view.
def get_bug_list_defects4j():
    projects_dir = os.path.normpath(f"{DEFECTS4J_DIR}/framework/projects")
    bug_list = []

    for project in os.listdir(projects_dir):
        folder_path = os.path.join(projects_dir, project)
        
        if os.path.isdir(folder_path):
            for file in os.listdir(folder_path):
                if file == "active-bugs.csv":
                    file_path = os.path.join(folder_path, file) 

                    with open(file_path, mode='r', newline='', encoding='utf-8-sig') as file:
                        reader = csv.DictReader(file)
                        
                        for row in reader:
                            report_link = row["report.url"]

                            if report_link == "UNKNOWN":
                                report_link = None

                            id_in_project = row["bug.id"]
                            bug_info = {
                                "uid": f"defects4j-{project}-{id_in_project}",
                                "benchmark": "Defects4J",
                                "project": project,
                                "number": id_in_project,
                                "deprecated": False,
                                "report_link": report_link,
                                "language": "Java"
                            }

                            bug_list.append(bug_info)

                if file == "deprecated-bugs.csv":
                    file_path = os.path.join(folder_path, file) 

                    with open(file_path, mode='r', newline='', encoding='utf-8-sig') as file:
                        reader = csv.DictReader(file)
                        
                        for row in reader:
                            report_link = row["report.url"]

                            if report_link == "UNKNOWN":
                                report_link = None

                            id_in_project = row["bug.id"]
                            bug_info = {
                                "uid": f"defects4j-{project}-{id_in_project}",
                                "benchmark": "Defects4J",
                                "project": project,
                                "number": id_in_project,
                                "deprecated": True,
                                "report_link": report_link,
                                "language": "Java"
                            }

                            bug_list.append(bug_info)

    return bug_list

def checkout_bug_defects4j(bug):
    bug_uid, id, project = bug["uid"], bug["number"], bug["project"]
    output_dir = os.path.join(TMP_CHECKOUTS_DIR, f"{bug_uid}")

    # Add depricated bugs here if needed
    if bug_uid in DEPRICATED_BUGS:
        # Change to Java 1.7 and add defects4j 1 to path
        os.environ["JAVA_HOME"] = JAVA_7_HOME
        execute_bash_command(f"sudo {DEFECTS4J_DIR_15}/framework/bin/defects4j checkout -p {project} -v {id}b -w {output_dir}")
        os.environ["JAVA_HOME"] = JAVA_8_HOME

    else:
        execute_bash_command(f"defects4j checkout -p {project} -v {id}b -w {output_dir}")

    return output_dir

def checkout_fix_defects4j(bug):
    bug_uid, id, project = bug["uid"], bug["number"], bug["project"]
    output_dir = os.path.join(TMP_CHECKOUTS_DIR, f"{bug_uid}-fixed")

    # Add depricated bugs here if needed
    if bug_uid in DEPRICATED_BUGS:
        # Change to Java 1.7 and add defects4j 1 to path
        os.environ["JAVA_HOME"] = JAVA_7_HOME
        execute_bash_command(f"sudo {DEFECTS4J_DIR_15}/framework/bin/defects4j checkout -p {project} -v {id}f -w {output_dir}")
        os.environ["JAVA_HOME"] = JAVA_8_HOME

    else:
        execute_bash_command(f"defects4j checkout -p {project} -v {id}f -w {output_dir}")

    return output_dir

def get_developer_patch_defects4j(bug, buggy_project_dir, fixed_project_dir):
    bug_uid = bug["uid"]
    patch_uid = f"{bug_uid}-developer"

    if bug["project"] == "Chart":
        source_folder = "source"

    elif bug["project"] == "Gson":
        source_folder = os.path.join("gson", "src")

    else:
        source_folder = "src"

    buggy_source = os.path.join(buggy_project_dir, source_folder)
    fixed_source = os.path.join(fixed_project_dir, source_folder)

    diff, _ = execute_bash_command(f"git diff {buggy_source} {fixed_source}", dir=os.path.dirname(PROJECT_DIR), error_allowed=True)

    tmp_patch_dir = os.path.join(TMP_DEVELOPER_PATCH_DIR, f"{patch_uid}.patch")

    with open(tmp_patch_dir, 'w') as file:
        file.write(diff)

    patch = {
        "uid": patch_uid,
        "bug_uid": bug["uid"],
        "generator": "Developer",
        "location": os.path.relpath(tmp_patch_dir, PROJECT_DIR),
        "correctness": "Correct",
        "origin": bug["benchmark"]
    }
    return patch

""" Bugs.jar """
def get_bug_list_bugsjar():
    bugs = []
    
    for folder_name in os.listdir(BUGSJAR_DIR):
        folder_path = os.path.join(BUGSJAR_DIR, folder_name)
        
        if os.path.isdir(folder_path) and folder_name != ".git":
            if folder_name == "ID2commit":
                continue

            result = execute_bash_command("git branch -a | grep bugs-dot-jar_", dir=folder_path)

            filtered_output = [line for line in result.split('\n') if 'bugs-dot-jar_' in line]
            
            for line in filtered_output:
                bug_issue_name, sha = line.split("/")[-1].split("_")[1:]
                project_issue_name, issue_number = bug_issue_name.split("-")
                
                with open(os.path.join(BUGSJAR_DIR, "ID2commit", f"{folder_name}.txt")) as map_file:
                    for line in map_file.read().split("\n"):
                        if line.split(",")[1] == f"{project_issue_name}-{issue_number}_{sha}":
                            bug_number = line.split(",")[0]

                            break
                
                bug_info = {
                    "uid": f"bugsjar-{folder_name}-{sha}",
                    "benchmark": "Bugs.jar",
                    "project": folder_name,
                    "sha": sha,
                    "number": bug_number,
                    "project_issue_name": project_issue_name,
                    "issue_number": issue_number, 
                    "report_link": f"https://issues.apache.org/jira/browse/{project_issue_name}-{issue_number}",
                    "language": "Java"
                }
                bugs.append(bug_info)

    return bugs
            
def checkout_bug_bugsjar(bug):
    bug_uid, project, id, folder_name, sha = bug["uid"], bug["project_issue_name"], bug["issue_number"], bug["project"], bug["sha"]
    tmp_bugsjar_dir =os.path.join(TMP_CHECKOUTS_DIR, ".tmp_bugsjar")

    execute_bash_command(f"cp -r {BUGSJAR_DIR} {tmp_bugsjar_dir}")

    project_dir = os.path.join(tmp_bugsjar_dir, folder_name)

    execute_bash_command(f"git checkout bugs-dot-jar_{project}-{id}_{sha}", dir=project_dir)
    
    output_dir = os.path.join(TMP_CHECKOUTS_DIR, f"{bug_uid}")
    
    if os.path.exists(output_dir):
        shutil.rmtree(output_dir)

    shutil.copytree(project_dir, output_dir)

    execute_bash_command("rm .git", dir=output_dir)
    execute_bash_command("git init", dir=output_dir)
    execute_bash_command("git add .", dir=output_dir)
    execute_bash_command('git commit -m "init"', dir=output_dir)
    execute_bash_command(f"rm -rf {tmp_bugsjar_dir}")

    return output_dir
            
def get_developer_patch_bugsjar(bug, buggy_project_dir): 
    bug_uid = bug["uid"]
    patch_uid = f"{bug_uid}-developer"
    patch_dir = os.path.join(buggy_project_dir, ".bugs-dot-jar", "developer-patch.diff")
    tmp_patch_dir = os.path.join(TMP_DEVELOPER_PATCH_DIR, f"{patch_uid}.patch")
    execute_bash_command(f"cp {patch_dir} {tmp_patch_dir}")

    patch = {
        "uid": patch_uid,
        "bug_uid": bug["uid"],
        "generator": "Developer",
        "location": os.path.relpath(tmp_patch_dir, PROJECT_DIR),
        "correctness": "Correct",
        "origin": bug["benchmark"]
    }
    return patch
    
def checkout_fix_bugsjar(bug):
    bug_uid, project, id, folder_name, sha = bug["uid"], bug["project_issue_name"], bug["issue_number"], bug["project"], bug["sha"]
    tmp_bugsjar_dir =os.path.join(TMP_CHECKOUTS_DIR, ".tmp_bugsjar")

    execute_bash_command(f"cp -r {BUGSJAR_DIR} {tmp_bugsjar_dir}")

    project_dir = os.path.join(tmp_bugsjar_dir, folder_name)

    execute_bash_command(f"git checkout bugs-dot-jar_{project}-{id}_{sha}", dir=project_dir)
    
    output_dir = os.path.join(TMP_CHECKOUTS_DIR, f"{bug_uid}-fixed")
    
    if os.path.exists(output_dir):
        shutil.rmtree(output_dir)

    shutil.copytree(project_dir, output_dir)

    execute_bash_command("rm .git", dir=output_dir)
    execute_bash_command("git init", dir=output_dir)
    execute_bash_command("git add .", dir=output_dir)
    execute_bash_command('git commit -m "init"', dir=output_dir)

    execute_bash_command(f"rm -rf {tmp_bugsjar_dir}")

    patch_dir = os.path.join(output_dir, ".bugs-dot-jar", "developer-patch.diff")

    apply_patch_to_git_repo(output_dir, patch_dir)

    return output_dir


""" QuixBugs """
def get_bug_list_quixbugs_java():
    bugs = []
    correct_dir = os.path.join(QUIXBUGS_DIR, "correct_java_programs")

    for file_name in os.listdir(correct_dir):
        bug_name = None

        if not file_name.endswith(".java"):
            continue

        bug_name = file_name[:-5]

        # Continue before this
        assert bug_name

        bug_info = {
            "uid": f"quixbugs-{bug_name}-java",
            "benchmark": "QuixBugs",
            "project": bug_name,
            "language": "Java"
        }
        bugs.append(bug_info)

    return bugs

def get_bug_list_quixbugs_python():
    bugs = []
    correct_dir = os.path.join(QUIXBUGS_DIR, "correct_python_programs")

    for file_name in os.listdir(correct_dir):
        bug_name = None

        if not file_name.endswith(".py"):
            continue

        bug_name = file_name[:-3]

        # Continue before this
        assert bug_name

        bug_info = {
            "uid": f"quixbugs-{bug_name}-python",
            "benchmark": "QuixBugs",
            "project": bug_name,
            "language": "Python"
        }
        bugs.append(bug_info)

    return bugs

def get_developer_patch_quixbugs_java(bug):
    bug_name, bug_uid = bug["project"], bug["uid"]
    file_name = f"{bug_name}.java"
    patch_uid = f"{bug_uid}-developer"

    # Get buggy file dir
    buggy_file_dir = os.path.join(QUIXBUGS_DIR, "java_programs", file_name)

    # Fix correct file, save it in tmp and get the dir
    correct_file_dir = os.path.join(QUIXBUGS_DIR, "correct_java_programs", file_name)
    
    with open(correct_file_dir, 'r') as input_file:
        correct_file_content = input_file.read()

    correct_file_content = correct_file_content.replace("correct_", "").replace("java_programs;", "java_programs;")
    correct_file_tmp_dir = os.path.join(TMP_CHECKOUTS_DIR, "correct_" + file_name)

    with open(correct_file_tmp_dir, 'w') as output_file:
        output_file.write(correct_file_content)

    # Get diff
    diff, _ = execute_bash_command(f"git diff {buggy_file_dir} {correct_file_tmp_dir}", dir=os.path.dirname(PROJECT_DIR), error_allowed=True)

    # Remove tmp correct file
    os.remove(correct_file_tmp_dir)

    # Save patch
    tmp_patch_dir = os.path.join(TMP_DEVELOPER_PATCH_DIR, f"{patch_uid}.patch")

    with open(tmp_patch_dir, 'w') as file:
        file.write(diff)

    # Generate patch
    patch = {
        "uid": patch_uid,
        "bug_uid": bug["uid"],
        "generator": "Developer",
        "location": os.path.relpath(tmp_patch_dir,PROJECT_DIR),
        "correctness": "Correct",
        "origin": bug["benchmark"]
    }

    return patch

def get_developer_patch_quixbugs_python(bug):
    bug_name, bug_uid = bug["project"], bug["uid"]
    file_name = f"{bug_name}.py"
    patch_uid = f"{bug_uid}-developer"

    # Get buggy file dir
    buggy_file_dir = os.path.join(QUIXBUGS_DIR, "python_programs", file_name)

    # Fix correct file, save it in tmp and get the dir
    correct_file_dir = os.path.join(QUIXBUGS_DIR, "correct_python_programs", file_name)

    # Get diff
    diff, _ = execute_bash_command(f"git diff {buggy_file_dir} {correct_file_dir}", dir=os.path.dirname(PROJECT_DIR), error_allowed=True)

    # Save patch
    tmp_patch_dir = os.path.join(TMP_DEVELOPER_PATCH_DIR, f"{patch_uid}.patch")

    with open(tmp_patch_dir, 'w') as file:
        file.write(diff)

    # Generate patch
    patch = {
        "uid": patch_uid,
        "bug_uid": bug["uid"],
        "generator": "Developer",
        "location": os.path.relpath(tmp_patch_dir, PROJECT_DIR),
        "correctness": "Correct",
        "origin": bug["benchmark"]
    }

    return patch

def checkout_bug_quixbugs(bug):
    if bug["language"] == "Java":
        extension = ".java"
        bug_folder = "java_programs"

    else: 
        extension = ".py"
        bug_folder = "python_programs"

    bug_uid, file_name = bug["uid"], bug["project"]
    file_dir = os.path.join(QUIXBUGS_DIR, bug_folder, f"{file_name}{extension}")
    checkout_file_dir = os.path.join(TMP_CHECKOUTS_DIR, f"{bug_uid}{extension}")
    shutil.copyfile(file_dir, checkout_file_dir)

    return checkout_file_dir


""" Bears """
def get_bug_list_bears():
    bugs = []
    id_branch_map_file = os.path.join(BEARS_DIR, "scripts", "data", "bug_id_and_branch_2019.json")

    with open(id_branch_map_file) as file:
        id_branch_map = json.load(file)

    for mapping in id_branch_map:
        bug_branch = mapping["bugBranch"]
        project = '-'.join(mapping["bugBranch"].split('-')[1:-2])
        developer = mapping["bugBranch"].split('-')[0]
        _, bug_number = mapping["bugId"].split('-')

        # Issues exist for some bug but we will need to checkout first.
        bug = {
            "uid": f"bears-{bug_number}",
            "benchmark": "Bears",
            "number": bug_number,
            "project": project,
            "developer": developer,
            "branch": bug_branch,
            "branch_url": f"https://github.com/bears-bugs/bears-benchmark/tree/{bug_branch}",
            "language": "Java"
        }
        bugs.append(bug)

    return bugs

def checkout_bug_bears(bug):
    bug_uid = bug["uid"]
    checkout_dir = os.path.join(TMP_CHECKOUTS_DIR, bug_uid)

    if os.path.exists(checkout_dir):
        return checkout_dir
    
    new_bug_list_dir = os.path.join(BEARS_DIR, "scripts", "data", "bug_id_and_branch.json")
    old_bug_list_dir = os.path.join(BEARS_DIR, "scripts", "data", "bug_id_and_branch_2019.json")
    execute_bash_command(f"cp {old_bug_list_dir} {new_bug_list_dir}", dir=BEARS_DIR)

    checkout_bug_file = os.path.join(BEARS_DIR, "scripts", "checkout_bug.py")

    with open(checkout_bug_file, 'r') as file:
        content = file.read()

    old_content = """BUGGY_COMMIT = subprocess.check_output(cmd, shell=True).decode("utf-8")"""
    new_content = """BUGGY_COMMIT = subprocess.check_output(cmd, shell=True).decode("utf-8") \nif not BUGGY_COMMIT:
    cmd = "cd %s; git log --format=format:%%H --grep='Bug commit from';" % BEARS_PATH
    BUGGY_COMMIT = subprocess.check_output(cmd, shell=True).decode("utf-8")
    """

    content = content.replace(old_content, new_content)

    with open(checkout_bug_file, 'w') as file:
        file.write(content)

    bug_number = bug["number"]
    command = f"python scripts/checkout_bug.py --bugId Bears-{bug_number} --workspace {TMP_CHECKOUTS_DIR}"

    execute_bash_command(command, dir=BEARS_DIR)

    original_checkout_dir = os.path.join(TMP_CHECKOUTS_DIR, f"Bears-{bug_number}")

    os.rename(original_checkout_dir, checkout_dir)

    return checkout_dir

def checkout_fix_bears(bug):
    bug_uid = bug["uid"]
    checkout_dir = os.path.join(TMP_CHECKOUTS_DIR, f"{bug_uid}-fixed")

    if os.path.exists(checkout_dir):
        return checkout_dir
    
    new_bug_list_dir = os.path.join(BEARS_DIR, "scripts", "data", "bug_id_and_branch.json")
    old_bug_list_dir = os.path.join(BEARS_DIR, "scripts", "data", "bug_id_and_branch_2019.json")
    execute_bash_command(f"cp {old_bug_list_dir} {new_bug_list_dir}", dir=BEARS_DIR)
    
    bug_number = bug["number"]
    command = f"python scripts/checkout_bug.py --bugId Bears-{bug_number} --workspace {TMP_CHECKOUTS_DIR}"

    execute_bash_command(command, dir=BEARS_DIR)

    original_checkout_dir = os.path.join(TMP_CHECKOUTS_DIR, f"Bears-{bug_number}")
    os.rename(original_checkout_dir, checkout_dir)

    bug_branch = bug["branch"]

    # Use checkout command to go to latest commit in this branch.
    execute_bash_command(f"git checkout {bug_branch}", dir=checkout_dir)

    return checkout_dir

def get_developer_patch_bears(bug, buggy_project_dir, fixed_project_dir):
    bug_uid = bug["uid"]
    patch_uid = f"{bug_uid}-developer"

    diff = get_diff(buggy_project_dir, fixed_project_dir, types=[".java"], ignore_dirs=["target"])
    tmp_patch_dir = os.path.join(TMP_DEVELOPER_PATCH_DIR, f"{patch_uid}.patch")

    with open(tmp_patch_dir, 'w') as file:
        file.write(diff)

    patch = {
        "uid": patch_uid,
        "bug_uid": bug["uid"],
        "generator": "Developer",
        "location": os.path.relpath(tmp_patch_dir, PROJECT_DIR),
        "correctness": "Correct",
        "origin": bug["benchmark"]
    }
    return patch


""" IntroClassJava """
def get_bug_list_introclassjava():
    bugs = []

    with open(os.path.join(INTROCLASSJAVA_DIR, "dataset", "introclass.json")) as file:
        data = json.load(file)

    projects = {}

    for key in data:
        projects[key.split('/')[0]] = 0

    # benchmarkBugId
    for _, value in data.items():
        project, user, version = value["projectName"], value["projectUser"], value["projectUserVersion"]

        bug = {
                "uid": f"introclassjava-{project}-{user}-{version}",
                "benchmark": "IntroClassJava",
                "project": project,
                "developer": user,
                "number": version
            }

        bugs.append(bug)

    return bugs


""" General """
def configure_benchmarks():
    logging.info("Configuring benchmarks ...")
    
    configure_defects4j()

def get_bugs():
    bugs = []
    bugs += get_bug_list_bugsjar()
    bugs += get_bug_list_defects4j()
    bugs += get_bug_list_bears()
    bugs += get_bug_list_quixbugs_java()
    # bugs += get_bug_list_quixbugs_python()
    bugs += get_bug_list_introclassjava()

    return bugs

def checkout_bug(bug: pd.DataFrame) -> str:
    if bug["benchmark"] == "Defects4J":
        repo_dir = checkout_bug_defects4j(bug)

    elif bug["benchmark"] == "Bugs.jar":
        repo_dir = checkout_bug_bugsjar(bug)

    elif bug["benchmark"] == "Bears":
        repo_dir = checkout_bug_bears(bug)

    elif bug["benchmark"] == "QuixBugs":
        repo_dir = checkout_bug_quixbugs(bug)

    else:
        raise ValueError("Unexpected benchmark.")

    return repo_dir

def get_developer_patch(bug): 
    logging.info(f"Fetching developer patch of the bug: {bug}")

    bug_uid = bug["uid"]
    patch_uid = f"{bug_uid}-developer"
    location = os.path.join(TMP_DEVELOPER_PATCH_DIR, f"{patch_uid}.patch")

    if os.path.exists(location):
        logging.info(f"Developer patch has already been fetched for the bug: {bug}")


        patch = {
            "uid": patch_uid,
            "bug_uid": bug["uid"],
            "generator": "Developer",
            "location": os.path.relpath(location, PROJECT_DIR),
            "correctness": "Correct",
            "origin": bug["benchmark"]
        }      

        if not read_patch(location).strip():
            raise ValueError

        return patch
            
    if bug["benchmark"] == "Defects4J":
        buggy_project_dir = checkout_bug_defects4j(bug)
        fixed_project_dir = checkout_fix_defects4j(bug)
        patch = get_developer_patch_defects4j(bug, buggy_project_dir, fixed_project_dir)
        shutil.rmtree(buggy_project_dir)
        shutil.rmtree(fixed_project_dir)

    elif bug["benchmark"] == "Bugs.jar":
        buggy_project_dir = checkout_bug_bugsjar(bug)
        patch = get_developer_patch_bugsjar(bug, buggy_project_dir)
        time.sleep(2)
        
        shutil.rmtree(buggy_project_dir)

    elif bug["benchmark"] == "Bears":
        buggy_project_dir = checkout_bug_bears(bug)
        fixed_project_dir = checkout_fix_bears(bug)
        patch = get_developer_patch_bears(bug, buggy_project_dir, fixed_project_dir)
        shutil.rmtree(buggy_project_dir)
        shutil.rmtree(fixed_project_dir)

    elif bug["benchmark"] == "QuixBugs":
        if bug["language"] == "Java":
            patch = get_developer_patch_quixbugs_java(bug)

        elif bug["language"] == "Python":
            patch = get_developer_patch_quixbugs_python(bug)

    if not read_patch(location).strip():
        raise ValueError

    return patch

def get_developer_patches(bugs):
    logging.info("Fetching developer patches ...")

    patches = []

    for bug in bugs:

        if bug["benchmark"] == "IntroClassJava":
            continue

        patch = get_developer_patch(bug)

        patches.append(patch)

    logging.info("Developer patches are fetched.")

    return patches

SKIP = False

# Fixes patch and stores it: returns relpath of the fixed patch
def fix_patch(patch: pd.Series, bugs: pd.DataFrame) -> str:
    global SKIP

    if "patch1-Math-35-Arja.patch" in patch["location"]:
        SKIP = False

    patch_uid = patch.name
    formatted_patch_dir = os.path.join(TMP_FORMATTED_PATCH_DIR, f"{patch_uid}.patch")

    logging.info(f"Fixing the patch: {patch_uid}")

    # Check if patch exists
    if os.path.exists(formatted_patch_dir):
        logging.info(f"Formatted patch already exists.")

        return os.path.relpath(formatted_patch_dir, PROJECT_DIR)

    if SKIP:
        logging.info(f"Skipping the patch. Patch: {patch.name}")

        return None

    patch_abs_dir = os.path.join(PROJECT_DIR, patch["location"])

    bug = bugs.loc[patch["bug_uid"]].to_dict()
    bug["uid"] = patch["bug_uid"]

    one_file = False

    if bug["benchmark"] == "Defects4J":
        checkout_abs_dir = checkout_bug_defects4j(bug)
    
    elif bug["benchmark"] == "Bugs.jar":
        checkout_abs_dir = checkout_bug_bugsjar(bug)

    elif bug["benchmark"] == "Bears":
        checkout_abs_dir = checkout_bug_bears(bug)

    elif bug["benchmark"] == "QuixBugs":
        one_file = True
        checkout_abs_dir = checkout_bug_quixbugs(bug)

    else:
        raise ValueError("Unexpected benchmark.")
    
    if one_file:
        diff = fix_file_patch(patch_abs_dir, checkout_abs_dir)

        os.remove(checkout_abs_dir) 

    else:
        diff = fix_repo_patch(patch_abs_dir, checkout_abs_dir)

        shutil.rmtree(checkout_abs_dir, onerror=rmtree)

    # Output
    if diff:
        logging.info(f"Fixed the patch: {patch_uid}") 

        with open(formatted_patch_dir, 'w') as file:
            file.write(diff)

        return os.path.relpath(formatted_patch_dir, PROJECT_DIR)
    
    logging.info(f"Could not fix the patch: {patch_uid}") 

    return None 

# Returns method
def get_method(patch: pd.Series, bugs: pd.DataFrame = None):
    patch_uid = patch.name
    logging.info(f"Getting method of the patch: {patch.location}")
    out_put_dir = os.path.join(TMP_METHODS_DIR, patch.name)
    if os.path.exists(out_put_dir):
        logging.info(f"Methods already exist for the patch: {patch_uid}")
        all_files = os.listdir(out_put_dir)
        # Filter and sort the files for target and source files
        target_files = sorted([f for f in all_files if f.startswith('target-')], key=lambda x: int(x.split('-')[1].split('.')[0]))
        source_files = sorted([f for f in all_files if f.startswith('source-')], key=lambda x: int(x.split('-')[1].split('.')[0]))
        # Append the absolute paths to the respective lists
        source_method_dirs = [os.path.abspath(os.path.join(out_put_dir, f)) for f in source_files]
        target_method_dirs = [os.path.abspath(os.path.join(out_put_dir, f)) for f in target_files]
        if not source_method_dirs or not target_method_dirs:
            raise ValueError
        
        # Convert to relative paths before returning
        source_method_dirs = [os.path.relpath(path, PROJECT_DIR) for path in source_method_dirs]
        target_method_dirs = [os.path.relpath(path, PROJECT_DIR) for path in target_method_dirs]
        return source_method_dirs, target_method_dirs
    os.makedirs(out_put_dir)
    bug = get_dictionary(bugs.loc[patch["bug_uid"]])
    repo_dir = checkout_bug(bug)
    one_file = False
    if bug["benchmark"] == "QuixBugs":
        one_file = True
    try:
        if one_file:
            source_method_dirs, target_method_dirs = get_java_modified_methods_git_repo(out_put_dir, TMP_CHECKOUTS_DIR, os.path.join(PROJECT_DIR, patch["location"]))
        else:
            source_method_dirs, target_method_dirs = get_java_modified_methods_git_repo(out_put_dir, repo_dir, os.path.join(PROJECT_DIR, patch["location"]))
    except (javalang.parser.JavaSyntaxError, UnicodeDecodeError, unidiff.errors.UnidiffParseError):
        logging.info(f"Could not parse the patch while getting the method: {patch_uid}")
        shutil.rmtree(out_put_dir)
        shutil.rmtree(repo_dir, onerror=rmtree)
        return None
    
    if one_file:
        os.remove(repo_dir)
    else:
        shutil.rmtree(repo_dir, onerror=rmtree)
    
    if not source_method_dirs or not target_method_dirs:
        raise ValueError
    
    # Convert to relative paths before returning
    source_method_dirs = [os.path.relpath(path, PROJECT_DIR) for path in source_method_dirs]
    target_method_dirs = [os.path.relpath(path, PROJECT_DIR) for path in target_method_dirs]
    
    logging.info(f"Successfully fetched methods of the patch: {patch_uid}")
    return source_method_dirs, target_method_dirs

def get_file(patch: pd.Series, bugs: pd.DataFrame = None):
    """
    Extract changed files for a single patch.
    
    Args:
        patch: Series containing patch information
        bugs: DataFrame containing bug information
    
    Returns:
        tuple: (source_file_dirs, target_file_dirs) or None if extraction fails
    """
    patch_uid = patch.name
    logging.info(f"Getting files of the patch: {patch.location}")
    
    output_dir = os.path.join(TMP_FILES_DIR, patch.name)
    
    # Check if files already exist
    if os.path.exists(output_dir):
        logging.info(f"Files already exist for the patch: {patch_uid}")
        all_files = os.listdir(output_dir)
        
        # Filter and sort the files for target and source files
        target_files = sorted([f for f in all_files if f.startswith('target-')], 
                             key=lambda x: int(x.split('-')[1]))
        source_files = sorted([f for f in all_files if f.startswith('source-')], 
                             key=lambda x: int(x.split('-')[1]))
        
        # Append the absolute paths to the respective lists
        source_file_dirs = [os.path.abspath(os.path.join(output_dir, f)) for f in source_files]
        target_file_dirs = [os.path.abspath(os.path.join(output_dir, f)) for f in target_files]
        
        if not source_file_dirs or not target_file_dirs:
            logging.warning(f"No valid files found for patch: {patch_uid}")
            return None, None
        
        # Convert to relative paths before returning
        source_file_dirs = [os.path.relpath(path, PROJECT_DIR) for path in source_file_dirs]
        target_file_dirs = [os.path.relpath(path, PROJECT_DIR) for path in target_file_dirs]
        
        return source_file_dirs, target_file_dirs
    
    # Create output directory
    os.makedirs(output_dir)
    
    # Get bug information
    bug = get_dictionary(bugs.loc[patch["bug_uid"]])
    repo_dir = checkout_bug(bug)
    
    one_file = False
    if bug["benchmark"] == "QuixBugs":
        one_file = True
    
    try:
        if one_file:
            source_file_dirs, target_file_dirs = get_java_modified_files_git_repo(
                output_dir, TMP_CHECKOUTS_DIR, os.path.join(PROJECT_DIR, patch["location"])
            )
        else:
            source_file_dirs, target_file_dirs = get_java_modified_files_git_repo(
                output_dir, repo_dir, os.path.join(PROJECT_DIR, patch["location"])
            )
            
    except (UnicodeDecodeError, unidiff.errors.UnidiffParseError, Exception) as e:
        logging.error(f"Could not parse the patch while getting files: {patch_uid}. Error: {str(e)}")
        shutil.rmtree(output_dir, ignore_errors=True)
        if one_file and os.path.exists(repo_dir):
            os.remove(repo_dir)
        elif os.path.exists(repo_dir):
            shutil.rmtree(repo_dir, onerror=rmtree)
        return None, None
    
    # Clean up repository
    if one_file and os.path.exists(repo_dir):
        os.remove(repo_dir)
    elif os.path.exists(repo_dir):
        shutil.rmtree(repo_dir, onerror=rmtree)
    
    if not source_file_dirs or not target_file_dirs:
        logging.warning(f"No files extracted for patch: {patch_uid}")
        shutil.rmtree(output_dir, ignore_errors=True)
        return None, None
    
    # Convert to relative paths before returning
    source_file_dirs = [os.path.relpath(path, PROJECT_DIR) for path in source_file_dirs]
    target_file_dirs = [os.path.relpath(path, PROJECT_DIR) for path in target_file_dirs]
    
    logging.info(f"Successfully fetched files of the patch: {patch_uid}")
    return source_file_dirs, target_file_dirs



# Returns diff
def get_raw_patch(patch: pd.Series) -> str:
    return read_patch(patch["location"])

def get_headerless_patch(patch: pd.Series) -> str:
    patch_dir = os.path.join(PROJECT_DIR, patch["location"])

    if not os.path.exists(patch_dir):
        raise ValueError

    with open(patch_dir, 'r') as file:
        lines = file.readlines()

    # Remove lines start with "+++ " or "--- " or "diff --git " or "index"
    headerless_lines = [line for line in lines if not (line.startswith("+++ ") or line.startswith("--- ") or line.startswith("diff --git ") or line.startswith("index "))]
    headerless_patch = ''.join(headerless_lines)

    return headerless_patch

def get_single_hunk_method(patch: pd.Series):
    _, target_method_dirs = get_method(patch)

    if len(target_method_dirs) != 1:
        raise ValueError(f"The patch is not single hunk. {target_method_dirs}")

    return read_patch(target_method_dirs[0])

def are_single_hunks(patch: pd.Series, developer_patches: pd.DataFrame) -> bool:
    _, target_method_dirs = get_method(patch)
    _, developer_target_method_dirs = get_method(developer_patches.loc[f"{patch['bug_uid']}-developer"])
    
    if len(target_method_dirs) != 1:
        return False
    
    if len(developer_target_method_dirs) != 1:
        return False
    
    return True

def is_single_hunk(patch: pd.Series):
    _, target_method_dirs = get_method(patch)

    if len(target_method_dirs) != 1:
        return False
    
    return True

def get_single_hunks(patches: pd.DataFrame, developer_patches) -> pd.DataFrame:
    if os.path.exists(TMP_SINGLE_HUNK_TOOL_PATHCES_PKL):
        logging.info("Loading single hunk tool patches from file ...")
        single_hunk_tool_patches = pd.read_pickle(TMP_SINGLE_HUNK_TOOL_PATHCES_PKL)

        dropped_rows_tool = single_hunk_tool_patches[~single_hunk_tool_patches['bug_uid'].str.contains('defects4j', na=False)]
        single_hunk_tool_patches = single_hunk_tool_patches[single_hunk_tool_patches['bug_uid'].str.contains('defects4j', na=False)]
        logging.info(f"Dropped Tool Patches Rows (Non-Defects4J): {len(dropped_rows_tool)}")

        return single_hunk_tool_patches

    single_hunk_tool_patches = patches[patches.apply(is_single_hunk, axis=1)]
    # single_hunk_tool_patches = patches[patches.apply(lambda patch: are_single_hunks(patch, developer_patches), axis=1)]

    # # are all single hunks have only one  source_method source_method is a list? Yes
    # print(len(single_hunk_tool_patches))
    # answer = single_hunk_tool_patches[single_hunk_tool_patches['source_methods'].apply(lambda x: isinstance(x, list) and len(x) == 1)]
    # print(f"Single Hunk Tool Patches with only one source_method: {len(answer)}")

    # Save to file
    single_hunk_tool_patches.to_pickle(TMP_SINGLE_HUNK_TOOL_PATHCES_PKL)
    logging.info(f"Single hunk tool patches saved to {TMP_SINGLE_HUNK_TOOL_PATHCES_PKL}")

    dropped_rows_tool = single_hunk_tool_patches[~single_hunk_tool_patches['bug_uid'].str.contains('defects4j', na=False)]
    single_hunk_tool_patches = single_hunk_tool_patches[single_hunk_tool_patches['bug_uid'].str.contains('defects4j', na=False)]
    logging.info(f"Dropped Tool Patches Rows (Non-Defects4J): {len(dropped_rows_tool)}")

    return single_hunk_tool_patches

""" Patch Dataset Preprocessing"""
def remove_developer_identical_patches(tool_patches=None, developer_patches=None):
    logging.info("Removing Developer Identical-1 ...")
    tool_patches_copy = tool_patches.copy()

    # tool_patches['content'] = tool_patches['location'].apply(read_patch)
    # tool_patches['content'] = tool_patches['target_methods'].apply(lambda x: read_file(x[0]) if isinstance(x, list) and len(x) > 0 else None)
    tool_patches_copy['content'] = tool_patches_copy.apply(get_single_hunk_method, axis=1)

    # get bugs with fixes
    # unique_bugs = tool_patches["bug_uid"].unique()
    # logging.info(f"Unique bugs: {len(unique_bugs)}")

    # Make the bugs a dataframe
    # unique_bugs = pd.DataFrame(unique_bugs, columns=["bug_uid"])

    # unique_bugs["total_patches"] = unique_bugs["bug_uid"].apply(lambda x: len(tool_patches[tool_patches["bug_uid"] == x]))

    # logging.info(unique_bugs)

    # Create a copy of tool_patches to work with
    logging.info(f"Current Representatives: {tool_patches_copy}")

    # Remove developer identicals
    logging.info("Removing developer identicals ...")
    logging.info(f"Developer patches: {len(developer_patches)}")
    logging.info(f"Current Representatives: {tool_patches_copy}")
    # developer_patches["content"] = developer_patches["location"].apply(read_patch)
    # developer_patches['content'] = developer_patches['target_methods'].apply(lambda x: read_file(x[0]) if isinstance(x, list) and len(x) > 0 else None)
    # developer_patches['content'] = developer_patches.apply(get_single_hunk_method, axis=1)

    # Track patches dropped due to being identical to developer patches
    # tool_patches_copy["is_identical_to_dev"] = tool_patches_copy.apply(lambda row: row["content"] == get_single_hunk_method(developer_patches[developer_patches["bug_uid"] == row["bug_uid"]].iloc[0]), axis=1)
    # if get_single_hunk_method raises error, then it is not identical


    def is_identical(row):
        dev_rows = developer_patches[developer_patches["bug_uid"] == row["bug_uid"]]
        if len(dev_rows) == 0:
            return False
        try:
            return row["content"] == get_single_hunk_method(dev_rows.iloc[0])
        except Exception:
            return False

    tool_patches_copy["is_identical_to_dev"] = tool_patches_copy.apply(
        is_identical,
        axis=1
    )
    
    tool_patches_copy = tool_patches_copy[~tool_patches_copy["is_identical_to_dev"]]

    tool_patches_copy = tool_patches_copy.drop(columns=['content'])
    
    return tool_patches_copy

""" Reporting """
def report_dataset(cleaned_developer_patches, cleaned_tool_patches, bugs):
    # Reports single hunk, and identical seperately
    logging.info("-------------------------------------------------------------------------------------------------------------------------")
    logging.info("-------------------------------------------------------------------------------------------------------------------------")
    logging.info("--- Reporting dataset ---")
    # Make copies to avoid modifying input dataframes
    developer_patches = cleaned_developer_patches.copy()
    tool_patches = cleaned_tool_patches.copy()
    
    logging.info("Reporting dataset ... Tool-devided, Developer-Provided, and Correctness, being single hunk")
    logging.info(f"No of bugs: {len(bugs)}, No of developer patches: {len(developer_patches)}, No of tool patches: {len(tool_patches)}, No of correct tool patches: {len(tool_patches[tool_patches['correctness'] == 'Correct'])}")

    # ===== Summary Table Report =====
    combined_patches = pd.concat([tool_patches, developer_patches])
    combined_patches.reset_index(drop=True, inplace=True)
    merged_data = combined_patches.merge(
        bugs,
        left_on="bug_uid",
        right_index=True,
        how="left"
    )

    summary_table = (
        merged_data.groupby(['generator_id', 'benchmark', 'correctness'])
            .size()
            .unstack(fill_value=0)
            .reset_index()
    )

    summary_table['C/O'] = (
        summary_table.get('Correct', 0).astype(str) + '/' + summary_table.get('Overfitting', 0).astype(str)
    )

    final_table = summary_table.pivot(index='generator_id', columns='benchmark', values='C/O')
    final_table = final_table.fillna('0/0')

    logging.info("\nSummary Table (Correct/Overfitting by Generator x Benchmark):")
    logging.info(final_table)

    # ===== Developer-Identical Patches Report =====
    logging.info("\n--- Developer-Identical Patches Report ---")
    
    # Read patch content for comparison
    developer_patches['content'] = developer_patches['location'].apply(read_patch)
    tool_patches['content'] = tool_patches['location'].apply(read_patch)
    
    # Find tool patches identical to developer patches
    dev_identical_patches = []
    non_dev_identical_patches = []
    
    for _, tool_patch in tool_patches.iterrows():
        match = developer_patches[
            (developer_patches['bug_uid'] == tool_patch['bug_uid']) &
            (developer_patches['content'] == tool_patch['content'])
        ]
        if not match.empty:
            dev_identical_patches.append(tool_patch)
        else:
            non_dev_identical_patches.append(tool_patch)
    
    dev_identical_df = pd.DataFrame(dev_identical_patches) if dev_identical_patches else pd.DataFrame()
    non_dev_identical_df = pd.DataFrame(non_dev_identical_patches) if non_dev_identical_patches else pd.DataFrame()
    
    logging.info(f"Total tool patches: {len(tool_patches)}")
    logging.info(f"Developer-identical patches: {len(dev_identical_df)}")
    logging.info(f"Non-developer-identical patches: {len(non_dev_identical_df)}")
    
    if len(dev_identical_df) > 0:
        # Breakdown by generator
        logging.info("\nDeveloper-identical patches by generator:")
        dev_identical_by_gen = dev_identical_df.groupby('generator_id').size()
        for gen, count in dev_identical_by_gen.items():
            logging.info(f"  {gen}: {count}")
        
        # Check correctness of developer-identical patches
        if 'correctness' in dev_identical_df.columns:
            correctness_count = dev_identical_df['correctness'].value_counts()
            logging.info("\nCorrectness of developer-identical patches:")
            for correctness, count in correctness_count.items():
                logging.info(f"  {correctness}: {count}")
            
            # Flag if any overfitting patches are identical to developer patches
            overfitting_identical = dev_identical_df[dev_identical_df['correctness'] == 'Overfitting']
            if len(overfitting_identical) > 0:
                logging.warning(f"WARNING: {len(overfitting_identical)} overfitting patches are identical to developer patches!")
                for _, patch in overfitting_identical.iterrows():
                    logging.warning(f"  Bug: {patch['bug_uid']}, Generator: {patch['generator_id']}")

    # ===== Single Hunk Reporting =====
    logging.info("\n--- Single Hunk Patch Reporting ---")

    # Add single hunk info columns using both checks
    developer_patches['is_single_hunk'] = developer_patches.apply(is_single_hunk, axis=1)
    developer_patches['are_single_hunks'] = developer_patches.apply(
        lambda patch: are_single_hunks(patch, developer_patches), axis=1
    )

    tool_patches['is_single_hunk'] = tool_patches.apply(is_single_hunk, axis=1)
    tool_patches['are_single_hunks'] = tool_patches.apply(
        lambda patch: are_single_hunks(patch, developer_patches), axis=1
    )

    # Developer single hunk patches (with both methods)
    dev_single_hunk_is = developer_patches[developer_patches['is_single_hunk']]
    dev_single_hunk_are = developer_patches[developer_patches['are_single_hunks']]
    logging.info(f"Developer patches: {len(developer_patches)} total")
    logging.info(f"  - {len(dev_single_hunk_is)} are single-hunk (is_single_hunk)")
    logging.info(f"  - {len(dev_single_hunk_are)} are single-hunk (are_single_hunks)")

    # Tool-generated single hunk patches (all)
    tool_single_hunk_is = tool_patches[tool_patches['is_single_hunk']]
    tool_single_hunk_is_correct = tool_single_hunk_is[tool_single_hunk_is['correctness'] == 'Correct']
    tool_single_hunk_are = tool_patches[tool_patches['are_single_hunks']]
    tool_single_hunk_are_correct = tool_single_hunk_are[tool_single_hunk_are['correctness'] == 'Correct']
    logging.info(f"Tool patches: {len(tool_patches)} total")
    logging.info(f"  - {len(tool_single_hunk_is)} are single-hunk (is_single_hunk)")
    logging.info(f"  - {len(tool_single_hunk_is_correct)} are single-hunk (is_single_hunk) and correct")
    logging.info(f"  - {len(tool_single_hunk_are)} are single-hunk (are_single_hunks)")
    logging.info(f"  - {len(tool_single_hunk_are_correct)} are single-hunk (are_single_hunks) and correct")

    # Single hunk analysis for non-developer-identical patches
    if len(non_dev_identical_df) > 0:
        non_dev_identical_df['is_single_hunk'] = non_dev_identical_df.apply(is_single_hunk, axis=1)
        non_dev_identical_df['are_single_hunks'] = non_dev_identical_df.apply(
            lambda patch: are_single_hunks(patch, developer_patches), axis=1
        )
        
        non_dev_single_hunk_is = non_dev_identical_df[non_dev_identical_df['is_single_hunk']]
        non_dev_single_hunk_are = non_dev_identical_df[non_dev_identical_df['are_single_hunks']]
        
        logging.info(f"\nNon-developer-identical tool patches: {len(non_dev_identical_df)} total")
        logging.info(f"  - {len(non_dev_single_hunk_is)} are single-hunk (is_single_hunk)")
        logging.info(f"  - {len(non_dev_single_hunk_are)} are single-hunk (are_single_hunks)")

    # Correct tool patches - single hunk
    correct_patches = tool_patches[tool_patches["correctness"] == "Correct"]
    correct_single_hunk_is = correct_patches[correct_patches['is_single_hunk']]
    correct_single_hunk_are = correct_patches[correct_patches['are_single_hunks']]
    
    # Calculate non-developer-identical correct patches that are single hunk
    correct_non_dev_identical_single_hunk_is = 0
    correct_non_dev_identical_single_hunk_are = 0
    
    for _, patch in correct_patches.iterrows():
        # Check if patch is developer-identical
        match = developer_patches[
            (developer_patches['bug_uid'] == patch['bug_uid']) &
            (developer_patches['content'] == patch['content'])
        ]
        # If not developer-identical, check if it's single hunk
        if match.empty:
            if patch['is_single_hunk']:
                correct_non_dev_identical_single_hunk_is += 1
            if patch['are_single_hunks']:
                correct_non_dev_identical_single_hunk_are += 1
    
    logging.info(f"\nCorrect tool patches: {len(correct_patches)} total")
    logging.info(f"  - {len(correct_single_hunk_is)} are single-hunk (is_single_hunk)")
    logging.info(f"  - {len(correct_single_hunk_are)} are single-hunk (are_single_hunks)")
    logging.info(f"  - {correct_non_dev_identical_single_hunk_is} non-developer-identical are single-hunk (is_single_hunk)")
    logging.info(f"  - {correct_non_dev_identical_single_hunk_are} non-developer-identical are single-hunk (are_single_hunks)")

    # Overfitting tool patches - single hunk
    overfitting_patches = tool_patches[tool_patches["correctness"] == "Overfitting"]
    overfitting_single_hunk_is = overfitting_patches[overfitting_patches['is_single_hunk']]
    overfitting_single_hunk_are = overfitting_patches[overfitting_patches['are_single_hunks']]

    # Calculate non-developer-identical overfitting patches that are single hunk
    overfitting_non_dev_identical_single_hunk_is = 0
    overfitting_non_dev_identical_single_hunk_are = 0

    for _, patch in overfitting_patches.iterrows():
        # Check if patch is developer-identical
        match = developer_patches[
            (developer_patches['bug_uid'] == patch['bug_uid']) &
            (developer_patches['content'] == patch['content'])
        ]
        # If not developer-identical, check if it's single hunk
        if match.empty:
            if patch['is_single_hunk']:
                overfitting_non_dev_identical_single_hunk_is += 1
            if patch['are_single_hunks']:
                overfitting_non_dev_identical_single_hunk_are += 1

    logging.info(f"Overfitting tool patches: {len(overfitting_patches)} total")
    logging.info(f"  - {len(overfitting_single_hunk_is)} are single-hunk (is_single_hunk)")
    logging.info(f"  - {len(overfitting_single_hunk_are)} are single-hunk (are_single_hunks)")
    logging.info(f"  - {overfitting_non_dev_identical_single_hunk_is} non-developer-identical are single-hunk (is_single_hunk)")
    logging.info(f"  - {overfitting_non_dev_identical_single_hunk_are} non-developer-identical are single-hunk (are_single_hunks)")

    # ===== Combined Summary Table =====
    logging.info("\n--- Combined Summary Table ---")
    
    combined_summary = []
    
    # Add developer patches
    dev_row = {
        'Generator': 'Developer',
        'Total': len(developer_patches),
        'Dev-Identical': 0,  # Developers are always dev-identical
        'Non-Dev-Identical': 0,  # N/A for developers
        'is_single_hunk': len(developer_patches[developer_patches['is_single_hunk']]),
        'are_single_hunks': len(developer_patches[developer_patches['are_single_hunks']]),
        'Correctness': 'N/A'
    }
    combined_summary.append(dev_row)
    
    # Add tool patches by generator and correctness
    for generator in tool_patches['generator_id'].unique():
        gen_tool_patches = tool_patches[tool_patches['generator_id'] == generator]
        
        # Find dev-identical and non-dev-identical counts
        gen_dev_identical = 0
        gen_non_dev_identical = 0
        for _, patch in gen_tool_patches.iterrows():
            match = developer_patches[
                (developer_patches['bug_uid'] == patch['bug_uid']) &
                (developer_patches['content'] == patch['content'])
            ]
            if not match.empty:
                gen_dev_identical += 1
            else:
                gen_non_dev_identical += 1
        
        for correctness in ['Correct', 'Overfitting']:
            subset = gen_tool_patches[gen_tool_patches['correctness'] == correctness]
            if len(subset) > 0:
                # Count dev-identical in this subset
                subset_dev_identical = 0
                subset_non_dev_identical = 0
                for _, patch in subset.iterrows():
                    match = developer_patches[
                        (developer_patches['bug_uid'] == patch['bug_uid']) &
                        (developer_patches['content'] == patch['content'])
                    ]
                    if not match.empty:
                        subset_dev_identical += 1
                    else:
                        subset_non_dev_identical += 1
                
                row = {
                    'Generator': generator,
                    'Total': len(subset),
                    'Dev-Identical': subset_dev_identical,
                    'Non-Dev-Identical': subset_non_dev_identical,
                    'is_single_hunk': len(subset[subset['is_single_hunk']]),
                    'are_single_hunks': len(subset[subset['are_single_hunks']]),
                    'Correctness': correctness
                }
                combined_summary.append(row)
    
    if combined_summary:
        summary_df = pd.DataFrame(combined_summary)
        logging.info("\n")
        logging.info(summary_df.to_string(index=False))

    # Drop temporary columns (not from originals since we made copies)
    developer_patches.drop(['content', 'is_single_hunk', 'are_single_hunks'], axis=1, inplace=True, errors='ignore')
    tool_patches.drop(['content', 'is_single_hunk', 'are_single_hunks'], axis=1, inplace=True, errors='ignore')
    if len(non_dev_identical_df) > 0:
        non_dev_identical_df.drop(['content', 'is_single_hunk', 'are_single_hunks'], axis=1, inplace=True, errors='ignore')

    logging.info("Reporting dataset completed.")
    logging.info("-------------------------------------------------------------------------------------------------------------------------")
    logging.info("-------------------------------------------------------------------------------------------------------------------------")

