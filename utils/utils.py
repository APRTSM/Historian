import re
import subprocess
import os
import javalang
import shutil
import json
import logging
from unidiff import PatchSet
import pandas as pd
from dataclasses import dataclass
import unidiff
import collections
from .config import *
from utils import *
from itertools import combinations


""" Bash """
def execute_bash_command(command, dir=None, error_allowed=False):
    if dir:
        result = subprocess.run(command, cwd=dir, shell=True, capture_output=True)

    else:
        result = subprocess.run(command, shell=True, capture_output=True)

    stdout = result.stdout.decode("latin-1")
    stderr = result.stderr.decode("latin-1")

    if not error_allowed:
        assert result.returncode == 0, stderr

        return stdout


    return stdout, stderr

def rmtree(func, dir, info):
    command = f"rm -rf {dir}"

    execute_bash_command(command)


""" Data List """
# Get object corresponding to the existing features
def get_record(records, record_info):
    records_copy = records.copy()

    for key, value in record_info.items():
        for record in records:
            if not key in record or record[key] != value:
                try:
                    records_copy.remove(record) 

                except:
                    pass
    
    if not len(records_copy) == 1:
        logging.info(f"Could not find a record for the given info: {record_info}, List of Matching Records: {records_copy}")

        return None
    
    # assert len(records_copy) == 1, f"Invalid record information. {record_info}"

    return records_copy[0]

def get_all(data_dir, file_name):
    with open(os.path.join(data_dir, file_name)) as file:
        return json.load(file)

def commit(data_dir, file_name, dataset):
    with open(os.path.join(data_dir, file_name), "w") as file:                
        file.seek(0)
        file.write(json.dumps(dataset))

def add_object(file_name, item):
    items = get_all(file_name)
    items.append(item)
    
    commit(file_name, items)

def get_objects_by_feature(dataset, feature, value):
    items = []

    for item in dataset:
        if item[feature] == value:
            items.append(item)

    return items

def get_object_by_unique_feature(dataset, feature, value):
    for item in dataset:
        if item[feature] == value:
            return item

def get_object_by_uid(dataset, uid):
    for item in dataset:
        if item["uid"] == uid:
            return item

def get_foreign_key_pairs(dataset, foreign_key_field_name):
    pairs = {}

    for item in dataset:
        try:
            pairs[item[foreign_key_field_name]].append(item)

        except:
            pairs[item[foreign_key_field_name]] = [item]

    return pairs

def get_objects_by_relation(dataset, feature, relation_feature, value):
    items = []

    for item in dataset:
        for relation in item[feature]:
            if relation[relation_feature] == value:
                items.append(item)
            
    return items

def remove_by_uid(dataset, uid):
    for index, item in enumerate(dataset):
        if item["uid"] == uid:
            dataset.pop(index)

            return dataset

def edit_object(dataset, edited_item):
    item = get_object_by_uid(dataset, edited_item["uid"])
    dataset[dataset.index(item)] = edited_item

    return dataset

def get_dictionary(series: pd.Series) -> dict:
    object = series.to_dict()
    object["uid"] = series.name

    return object


""" Files """
def delete_file_type(dir, types):
    for dirpath, _, filenames in os.walk(dir):
        for filename in filenames:
            file_path = os.path.join(dirpath, filename)
            remove = True

            for type in types:

                if filename.endswith(type):
                    remove = False

            if remove:
                os.remove(file_path)

def delete_dirs(root_dir, dirs):
    for dirpath, _, filenames in os.walk(root_dir):
        for filename in filenames:
            file_path = os.path.join(dirpath, filename)
            remove = False

            for dir in dirs:

                if f"/{dir}/" in file_path:
                    remove = True

            if remove:
                os.remove(file_path)

def remove_empty_lines(input_string):
    return "\n".join(line for line in input_string.splitlines() if line.strip())


""" Patch Files """
def get_diff(source_dir, target_dir, types=[], ignore_dirs=[]):
    tmp_source_dir = os.path.join(TMP_CHECKOUTS_DIR, ".diff_source")
    tmp_target_dir = os.path.join(TMP_CHECKOUTS_DIR, ".diff_target")

    shutil.copytree(source_dir, tmp_source_dir)
    shutil.copytree(target_dir, tmp_target_dir)

    if types:
        delete_file_type(tmp_source_dir, types)
        delete_file_type(tmp_target_dir, types)

    if ignore_dirs:
        delete_dirs(tmp_source_dir, ignore_dirs)
        delete_dirs(tmp_target_dir, ignore_dirs)
        
    command = f"git diff --no-index {tmp_source_dir} {tmp_target_dir}"
    diff, _ = execute_bash_command(command, error_allowed=True)
    
    shutil.rmtree(tmp_source_dir)
    shutil.rmtree(tmp_target_dir)

    return diff

def reset_applied_patch_git_repo(repo_dir):
    execute_bash_command(f"git reset --hard HEAD", repo_dir)

def apply_patch_to_git_repo(repo_dir, patch_dir):
    execute_bash_command(f"git apply --whitespace=fix --ignore-space-change --ignore-whitespace {patch_dir}", repo_dir)

def format_file_patch(location: str, file_location: str = None) -> str:
    with open(location, 'r') as file:
        lines = file.readlines()
    
    modified_lines = []
    last_src_path = None  # To hold the last valid src path
    diff_pattern = re.compile(r'^diff --git a/.+ b/.+')
    index_pattern = re.compile(r'^index \w+\.\.\w+ \d+')

    for line in lines:
        if diff_pattern.match(line) or index_pattern.match(line):
            continue

        if line.startswith("+++ "):
            line = line[:4] + f"b{last_src_path}" + '\n'

        elif line.startswith("--- "):
            if file_location:
                file_name = os.path.basename(file_location)

            else:
                file_name = os.path.basename(line.split('\t')[0].split(' ')[1].strip())

            path_part = f"/{file_name}"  # Remove the timestamp and other parts
            last_src_path = path_part  # Update last known good src path
            line = line[:4] + f"a{last_src_path}" + '\n'

        # Append the modified or original line
        modified_lines.append(line)

    # Write the modified content to a new file in the output directory
    return ''.join(modified_lines)

def format_patch(location: str, source_file: str = "", prefix=None) -> str:
    if not source_file:
        diff = read_patch(location).replace("PATCH_DIFF_ORIG=", "")

        if "--- a/" in diff:
            return diff

        if "--- /" in diff:
            diff = diff.replace("--- /", "--- a/")
            diff = diff.replace("+++ /", "+++ b/")

            return diff

    with open(location, 'r') as file:
        lines = file.readlines()
    
    modified_lines = []
    last_src_path = None  # To hold the last valid src path
    diff_pattern = re.compile(r'^diff --git a/.+ b/.+')
    index_pattern = re.compile(r'^index \w+\.\.\w+ \d+')

    index = 0

    if prefix:
        lines[index] = prefix + lines[index]

    while index < len(lines):
        line = lines[index]
        line = line.replace("PATCH_DIFF_ORIG=", "")

        if diff_pattern.match(line) or index_pattern.match(line):
            index += 1
            
            continue

        if line.startswith("+++ "):
            try:
                line = line[:4] + f"b{last_src_path}" + '\n'

            except:
                line = "--- " + modified_lines.pop()
                index -= 1

        if line.startswith("--- "):
            if source_file.endswith(".java"):
                last_src_path = f"/{source_file}"
                line = line[:4] + f"a{last_src_path}" + '\n'

            else:
                # Find the index of "src" or "source" in the path
                source_index = line.find(f"/{source_file}/")

                if source_index != -1:
                    # Extract the path starting from "src" or "source"
                    path_part = line[source_index:].split('\t')[0].strip()  # Remove the timestamp and other parts
                    
                    if path_part.endswith("java") and not path_part.endswith(".java"):
                        path_part = path_part[:-4] + ".java"

                    last_src_path = path_part  # Update last known good src path
                    line = line[:4] + f"a{last_src_path}" + '\n'
                else:
                    path_part = f"/{source_file}/" + line.split('\t')[0].split(' ')[1].strip()  # Remove the timestamp and other parts
                    last_src_path = path_part  # Update last known good src path
                    line = line[:4] + f"a{last_src_path}" + '\n'
        
        # Append the modified or original line
        modified_lines.append(line)
        index += 1


    # Write the modified content to a new file in the output directory
    return ''.join(modified_lines)

def get_patch_changes(patch_location: str, repo_location: str):
    logging.info(f"Patch Location{patch_location}, Repo Location{repo_location}")

    # Read the patch file
    with open(patch_location, 'r') as patch_file:
        patch_content = patch_file.readlines()
    
    # Extract filenames from lines starting with "+++"
    filenames = []
    for line in patch_content:
        if line.startswith("+++"):
            logging.info(line)
            match = re.search(r'(\b\w+\.java\b)', line)
            if match:
                logging.info(match.group(1))
                filenames.append(match.group(1))
    
    logging.info(f"File Names{filenames}")
    # Remove duplicates
    filenames = list(set(filenames))
    
    # Search the repository for the extracted filenames
    result = []
    for filename in filenames:
        matches = []
        
        for root, _, files in os.walk(repo_location):
            if filename in files:
                matches.append(os.path.relpath(os.path.join(root, filename), repo_location))
        
        result += matches
    
    return result

def fix_file_patch(patch_location: str, file_location: str) -> str:
    repo_dir = os.path.dirname(file_location)
    formatted_patch_dir = os.path.join(TMP_DIR, "tmp-patch.patch")
    diff = format_file_patch(patch_location, file_location)

    with open(formatted_patch_dir, 'w') as file:
        file.write(diff)

    command = f"git apply --whitespace=fix --ignore-space-change --ignore-whitespace {formatted_patch_dir}"
    _, stderr = execute_bash_command(command, repo_dir, error_allowed=True) 
    os.remove(formatted_patch_dir) 

    if stderr.strip() == "":
        return diff

    else:
        return None
    
def fix_repo_patch(patch_location: str, repo_location: str) -> str:
    diff = read_patch(patch_location)
    
    if "+++ " not in diff and "--- " not in diff:
        logging.info(f"Not a valid patch file. Patch location: {patch_location}, Repo location: {repo_location}")

        return None

    formatted_patch_dir = os.path.join(TMP_DIR, "tmp-patch.patch")
    changes = [
        "",
        "source",
        "src",
        "src/main/java",
        "src/java",
        "gson/src",
        "modules",
        "core/src",
        "suite",
        "dubbo-cluster/src",
        "hessian-lite/src",
        "dubbo-config/dubbo-config-spring/src",
        "dubbo-config/dubbo-config-api/src",
        "dubbo-remoting/dubbo-remoting-api/src",
        "service-registry/src",
        "foundations/foundation-config/src",
        "oak-core/src",
        "integration/hibernate-base/src",
        "runtime/src",
        "navigation-formats/src",
        "debezium-connector-postgres/src",
        "debezium-connector-mysql/src",
        "dhis-2/dhis-api/src",
        "address-model-lib/src",
        "address-controller/src",
        "pinot-core/src",
        "molgenis-semantic-mapper/src",
        "molgenis-data-csv/src",
        "molgenis-data-security/src",
        "molgenis-data-postgresql/src",
        "molgenis-data-import/src",
        "zipkin2/src",
        "byte-buddy-dep/src",
        "spring-cloud-gcp-storage/src",
        "spring-cloud-gcp-data-spanner/src",
        "activiti-cloud-app-service/src",
        "dubbo-rpc/dubbo-rpc-api/src",
        "code/spi-support/src",
        "cas-client-core/src",
        "amazon-kinesis-client/src",
        "axon-server-connector/src",
        "common/src",
        "apollo-adminservice/src",
        "openhtmltopdf-core/src",
        "yaml/src",
        "BaragonAgentService/src",
        "api/src",
        "omod-1.9/src",
        "pippo-session-parent/pippo-session/src",
        "wffweb/src",
        "jgrapht-core/src",
        "seeds-core/src",
        "ci-droid-tasks-consumer-services/src",
        "cxx-checks/src",
        "vertx-web-client/src",
        "server",
        "src/main/java/org/apache/commons/math",
        "dubbo-cluster/src/main/java/com/alibaba/dubbo/rpc/cluster",
        "oak-auth-external/src/main/java/org/apache/jackrabbit/oak/spi/security/authentication/external",
        "oak-mk/src/main/java/org/apache/jackrabbit/mk/model"
    ]

    extra_changes = get_patch_changes(patch_location, repo_location)

    if extra_changes == []:
        logging.info(f"No source file found for the patch. {patch_location}")

        return None
    
    changes += extra_changes

    if "bears-152" in patch_location or "bears-168" in patch_location or "bears-173" in patch_location or "bears-211" in patch_location:
        return None
    
    logging.info(f"Fixing the patch. {patch_location}")

    for source_folder in changes: 
        diff = format_patch(patch_location, source_folder)

        logging.info(f"Source Folder: {source_folder}, Difference: {diff}")

        with open(formatted_patch_dir, 'w') as file:
            file.write(diff)

        command = f"git apply --whitespace=fix --ignore-space-change --ignore-whitespace {formatted_patch_dir}"

        _, stderr = execute_bash_command(command, repo_location, error_allowed=True)

        logging.info(f"Source Folder: {source_folder}, Error: {stderr}")

        os.remove(formatted_patch_dir)

        if "patch fragment without header" in stderr:
            diff = format_patch(patch_location, source_folder, prefix="--- ")

            with open(formatted_patch_dir, 'w') as file:
                file.write(diff)

            logging.info(f"Difference: {diff}")
            command = f"git apply --whitespace=fix --ignore-space-change --ignore-whitespace {formatted_patch_dir}"
            logging.info(f"command: {command}")
            logging.info(f"repo_location: {repo_location}")

            _, stderr = execute_bash_command(command, repo_location, error_allowed=True)

            logging.info(f"Tried to fix the patch fragment without header. Got error: {stderr}")

            os.remove(formatted_patch_dir)

            if "patch fragment without header" in stderr:

                diff = remove_empty_lines(diff[4:])

                with open(formatted_patch_dir, 'w') as file:
                    file.write(diff)

                command = f"git apply --whitespace=fix --ignore-space-change --ignore-whitespace {formatted_patch_dir}"
                _, stderr = execute_bash_command(command, repo_location, error_allowed=True)

                logging.info(f"Tried to fix the patch fragment without header. But could not fix. Got error: {stderr}")

                os.remove(formatted_patch_dir)

        if "corrupt patch at" in stderr:
            diff = diff + "\n\n\n\n"

            with open(formatted_patch_dir, 'w') as file:
                file.write(diff)

            command = f"git apply --whitespace=fix --ignore-space-change --ignore-whitespace {formatted_patch_dir}"
            _, stderr = execute_bash_command(command, repo_location, error_allowed=True)

            os.remove(formatted_patch_dir)

        if "No such file or directory" in stderr:
            continue
    
        elif stderr.strip() == "" or "warning" in stderr:
            return diff
        
        elif "patch does not apply" in stderr:
            logging.info("The patch does not apply.")

            return None
        
        elif "already exists in working directory" in stderr:
            logging.info("The patch adds a file that already exists.")

            return None

        elif "git diff header lacks filename information" in stderr:
            logging.info("The patch header lacks filename information.")

            return None
        
        elif "bad git-diff" in stderr:
            logging.info("The patch has bad git-diff format.")

            return None    
        
        elif "corrupt patch at" in stderr:
            logging.info("Could not fix corrupted patch.")

            return None
        
        elif "patch fragment without header" in stderr:
            logging.info("Tried to fix the patch fragment without header. But could not fix. Got error. Patch is corrupted.")

            return None
        
        else:
            raise Exception(f"Unexpected stderr content while fixing the patch. Error: {stderr}")
        
    logging.info(f"Unexpected source file type for the patch. {patch_location}, could not fix the patch.")
        
    return None  

def format_and_apply_patch_to_file(file_dir, patch_dir):
    repo_dir = os.path.dirname(file_dir)
    formatted_patch_dir = os.path.join(TMP_DIR, "tmp-patch.patch")
    diff = format_file_patch(patch_dir, file_dir)

    with open(formatted_patch_dir, 'w') as file:
        file.write(diff)

    command = f"git apply --whitespace=fix --ignore-space-change --ignore-whitespace {formatted_patch_dir}"
    _, stderr = execute_bash_command(command, repo_dir, error_allowed=True) 
    os.remove(formatted_patch_dir) 

    if stderr.strip() == "":
        logging.info("The file patch applied correctly.")

        return True

    else:
        logging.info("The file patch does not apply.")

        return False

def format_and_apply_patch_to_git_repo(repo_dir, patch_dir):
    _, stderr = execute_bash_command(f"git apply --whitespace=fix --ignore-space-change --ignore-whitespace {patch_dir}", repo_dir, error_allowed=True)
    formatted_patch_dir = os.path.join(TMP_DIR, "tmp-patch.patch")

    if stderr.strip() == "" or "warning" in stderr:
        logging.info("The patch applied without a change.")

        return True
    
    if "patch does not apply" in stderr:
        logging.info(f"The patch does not apply. It is not formatted.")

        return False

    for source_folder in ["source", "src", "src/main/java", "src/java"]: 
        diff = format_patch(patch_dir, source_folder)

        with open(formatted_patch_dir, 'w') as file:
            file.write(diff)

        _, stderr = execute_bash_command(f"git apply --whitespace=fix --ignore-space-change --ignore-whitespace {formatted_patch_dir}", repo_dir, error_allowed=True)
        
        os.remove(formatted_patch_dir)

        if stderr.strip() == "" or "warning" in stderr:
            logging.info(f"The patch is formatted. Source Folder: {source_folder}")

            return True
        
        if "patch does not apply" in stderr:
            logging.info(f"The patch does not apply. It is formatted. Source Folder: {source_folder}")

            return False
        

    return False

def get_modified_files(patch_dir):
    modified_files = []
    with open(patch_dir) as file:
        patch = file.read()

    patch = PatchSet(patch)

    for patched_file in patch:
        modified_files.append(patched_file.path)

    return modified_files

def get_modified_classes_java(repo_dir, modified_files):
    modified_classes = []
    
    for modified_file in modified_files:
        file_dir = os.path.join(repo_dir, modified_file)

        with open(file_dir) as file:
            code_content = file.read()

        class_name = file_dir.split("/")[-1][:-5]
        pattern = r'package(.*?);'
        match = re.search(pattern, code_content, re.DOTALL)
        modified_classes.append(match.group(1).strip() + '.' + class_name)

    return modified_classes

def get_modified_files_git_repo(output_dir, checkout_dir, patch_dir):
    with open(patch_dir) as f:
        patch = PatchSet(f.read())

    patch_name = os.path.basename(patch_dir)[:-6]
    for i, patchedFile in enumerate(patch):  # different files
        source_start = []  # collect all star lines and find methods in class
        target_start = []
        for hunk in patchedFile:
            bias = -1
            target_start_2nd = -1
            source_start_2nd = -1
            curHunkCnt = [0,0]
            for j, x in enumerate(hunk):
                if x.line_type == '-':
                    source_start.append(x.source_line_no-1)
                    curHunkCnt[0] += 1
                elif x.line_type == '+':
                    target_start.append(x.target_line_no-1)
                    curHunkCnt[1] += 1
                elif sum(curHunkCnt) == 0:
                    if x.target_line_no is not None:
                        target_start_2nd = x.target_line_no-1
                    if x.source_line_no is not None:
                        source_start_2nd = x.source_line_no - 1
            if target_start.__len__() == 0 or curHunkCnt[1] == 0:
                target_start.append(target_start_2nd)
            elif source_start.__len__() == 0 or curHunkCnt[0] == 0:
                source_start.append(source_start_2nd)


        export_dir = os.path.join(output_dir, patch_name)
        
        if not os.path.exists(export_dir):
            os.makedirs(export_dir)

        original_file_dir =  os.path.join(output_dir, patch_name, f"source_file_{i}.java")
        changed_file_dir = os.path.join(output_dir, patch_name, f"target_file_{i}.java")

        shutil.copy(os.path.join(checkout_dir, patchedFile.source_file[2:]), original_file_dir)
        shutil.copy(os.path.join(checkout_dir, patchedFile.source_file[2:]), changed_file_dir)

        return original_file_dir, changed_file_dir
    
def read_patch(location):
    with open(location) as file:
        diff = file.read()

    return diff

def read_file(location):
    with open(location) as file:
        content = file.read()

    return content

keys = []
@dataclass
class FormattedPatch():
    modules: list[str]
    default_formats = {
        'module': '%s',
        'file_diffs': '%s',
        'hunk': 'Hunk %d',
        'line_numbers': '@@  %s  @@',
        '-line': '-%s',
        '+line': '+%s',
    }
    formats = default_formats.copy()
    
    @classmethod
    def turn_off(self, keys):
        logging.info(keys)
        for key in keys:
            FormattedPatch.formats[key] = ''
            
    @classmethod     
    def reset(self, keys = None):
        if keys == None:
            keys = FormattedPatch.default_formats.keys()
        for key in keys:
            FormattedPatch.formats[key] = FormattedPatch.default_formats[key]
    
    def __init__(self, location):
        self.patchset = unidiff.PatchSet.from_filename(location)   
        
        self.modules = []
        for file in self.patchset:
            file_diff = repr(file)
            module_name_marker_idx = file_diff.find('.')
            module_name_start = file_diff.rfind('/', 0, module_name_marker_idx) + 1
            module_name_end = file_diff.find('\t', module_name_marker_idx)
            self.modules.append(file_diff[module_name_start: module_name_end])
        
        self.print_config = collections.defaultdict(lambda: True)
    def __str__(self):
        patch = ''
        for module, patched_file in zip(self.modules, self.patchset):
            if self.formats['module']:
                patch += self.formats['module'] % module + '\n'
                
            line_numbers = []
            hunks = []
            for hunk in patched_file:
                lines = str(hunk).splitlines()
                line_numbers.append(lines[0].strip('@@').strip())
                hunks.append('\n'.join(lines[1:]))

            for i, hunk in enumerate(hunks):
                hunk_to_print = []
                
                for line in hunk.splitlines():
                    format_diff_line = None
                    
                    if line.startswith('-'):
                        if not self.formats['-line']:
                            continue
                        else:
                            format_diff_line = '-'
                    elif line.startswith('+'):
                        if not self.formats['+line']:
                            continue
                        else:
                            format_diff_line = '+'
                    else:
                        hunk_to_print.append(line)
                        
                    if format_diff_line:
                        format = self.formats['+line'] if format_diff_line == '+' else self.formats['-line']
                        line = ' ' + line[1:]
                        indent = 0
                        for c in line:
                            if c == ' ':
                                indent += 1
                            elif c == '\t':
                                indent += 4
                            if not c.isspace():
                                break

                        format_character_count = len(format) - 2
                        line = line.strip()
                        line = line.rjust(len(line) + indent - format_character_count)
                        line = format % line
                        hunk_to_print.append(line)
                        
                if self.formats['hunk']:    
                    patch += self.formats['hunk'] % (i + 1 ) + '\n'
                if self.formats['line_numbers']:
                    patch += self.formats['line_numbers'] % line_numbers[i] + '\n'
                patch += '\n'.join(hunk_to_print) + '\n'
        return patch

def get_patch_edit_numbers(location):
    file_counts = []
    hunk_counts = []
    source_line_counts = []
    target_line_counts = []

    try:
        patchset = unidiff.PatchSet.from_filename(location)

    except unidiff.errors.UnidiffParseError as e:
        if "Unexpected new file found" in str(e):
            file_sum = 0
            hunk_sum = 0
            source_sum = 0
            target_sum = 0
            
        else:
            raise e

    else:
        file_counts.append(len(patchset))
        hunk_count = sum(len(file) for file in patchset) # Sum the hunks for all files
        hunk_counts.append(hunk_count)
        hunks = [hunk for file in patchset for hunk in file] # Flatten hunks from all files
        source_line_counts.append(sum(hunk.source_length for hunk in hunks))
        target_line_counts.append(sum(hunk.target_length for hunk in hunks))

        file_sum = sum(file_counts)
        hunk_sum = sum(hunk_counts)
        source_sum = sum(source_line_counts)
        target_sum = sum(target_line_counts)

    counts = {
        'file': file_sum,
        'hunk': hunk_sum,
        'source': source_sum,
        'target': target_sum
    }

    return counts


""" Java Code """
def get_java_modified_methods_git_repo(output_dir, checkout_dir, patch_dir): # Later devide this function to functions    
    def annotate_unsupport_code(code):
        for i, line in enumerate(code):
            if line.startswith("package ") or line.startswith("import "):
                code[i] = '//' + code[i]
        return code

    def get_ast(functions):
        func = annotate_unsupport_code(functions)
        tokens = javalang.tokenizer.tokenize("".join(func))
        parser = javalang.parser.Parser(tokens)
        tree = parser.parse_member_declaration()
        return tree
    
    replaceString = re.compile("[\"].*?[\"]")

    def get_end_line(start_line, file_back, upper_limit):
        file = file_back.copy()
        left_bracket = 0
        right_bracket = 0
        for i in range(start_line, upper_limit):
            anno_index = file[i].find('//')
            file[i] = file[i][:anno_index] if anno_index != -1 else file[i]
            file[i] = replaceString.sub("",file[i]) ##
            left_bracket += file[i].count('{')
            right_bracket += file[i].count('}')
            if right_bracket == left_bracket and right_bracket:
                return i
        if right_bracket == left_bracket and right_bracket == 0:
            return start_line
        else:
            return -1

    def get_function_positions(tree, class_file):
        position = []  # start from 0
        methods = []
        for x in tree.body:
            if isinstance(x, javalang.tree.ClassDeclaration):
                for y in x.body:
                    if isinstance(y,javalang.tree.ClassDeclaration):
                        methods.extend(y.methods)
                    elif isinstance(y,javalang.tree.MethodDeclaration) or isinstance(y, javalang.tree.ConstructorDeclaration):
                        methods.append(y)
            elif isinstance(x, javalang.tree.MethodDeclaration) or isinstance(x, javalang.tree.ConstructorDeclaration):
                methods.append(x)
        if methods.__len__() == 0:
            methods.extend(tree.methods)
        for i, method in enumerate(methods):
            start_line = method.position.line - 1
            if i + 1< methods.__len__():
                upper_limit = methods[i+1].position.line - 1
            else:
                upper_limit = class_file.__len__() -1
            end_line = get_end_line(start_line, class_file,upper_limit)
            if end_line == -1:
                continue
            position.append((start_line, end_line))
        position = list(set(position))
        position.sort()
        return position

    with open(patch_dir) as f:
        patch = PatchSet(f.read())

    source_methods = []
    target_methods = []
    buggy_methods = []
    patched_methods = []
    patch_name = os.path.basename(patch_dir)[:-6]
    for i, patchedFile in enumerate(patch):  # different files
        source_start = []  # collect all star lines and find methods in class
        target_start = []
        for hunk in patchedFile:
            bias = -1
            target_start_2nd = -1
            source_start_2nd = -1
            curHunkCnt = [0,0]
            for j, x in enumerate(hunk):
                if x.line_type == '-':
                    source_start.append(x.source_line_no-1)
                    curHunkCnt[0] += 1
                elif x.line_type == '+':
                    target_start.append(x.target_line_no-1)
                    curHunkCnt[1] += 1
                elif sum(curHunkCnt) == 0:
                    if x.target_line_no is not None:
                        target_start_2nd = x.target_line_no-1
                    if x.source_line_no is not None:
                        source_start_2nd = x.source_line_no - 1
            if target_start.__len__() == 0 or curHunkCnt[1] == 0:
                target_start.append(target_start_2nd)
            elif source_start.__len__() == 0 or curHunkCnt[0] == 0:
                source_start.append(source_start_2nd)

        original_file = os.path.join(checkout_dir, patchedFile.source_file[2:])

        with open(original_file) as file:
            buggy_class = file.readlines()

        apply_patch_to_git_repo(checkout_dir, patch_dir)

        changed_file = os.path.join(checkout_dir, patchedFile.source_file[2:])

        with open(changed_file) as file:
            patched_class = file.readlines()

        reset_applied_patch_git_repo(checkout_dir)

        buggy_tree = get_ast(buggy_class)
        patched_tree = get_ast(patched_class)
        buggy_funtions_position = get_function_positions(buggy_tree, buggy_class)
        patched_funtions_position = get_function_positions(patched_tree, patched_class)
        buggy_methods_pos = set()
        patched_methods_pos = set()
        for start in source_start:
            for pos in buggy_funtions_position:
                if pos[0] <= start and start <= pos[1]:
                    buggy_methods_pos.add(pos)
                    break
        for start in target_start:
            for pos in patched_funtions_position:
                if pos[0] <= start and start <= pos[1]:
                    patched_methods_pos.add(pos)
        for x in buggy_methods_pos:
            buggy_methods.append("".join(buggy_class[x[0]:x[1]+1]))
        for x in patched_methods_pos:
            patched_methods.append("".join(patched_class[x[0]:x[1]+1]))

        buggy_method_dir = os.path.join(output_dir, f"source-{i}.java")
        patched_method_dir = os.path.join(output_dir, f"target-{i}.java")
        
        with open(buggy_method_dir, 'w') as file:
            file.write("".join(buggy_methods))

        with open(patched_method_dir, 'w') as file:
            file.write("".join(patched_methods))

        source_methods.append(buggy_method_dir)
        target_methods.append(patched_method_dir)

    return source_methods, target_methods    

def _normalize_code(code):
    """
    Normalizes code to compare the core content by:
    1. Removing patch headers and multihunk headers
    2. Normalizing quotes (removing duplicate quotes)
    3. Removing all whitespace, newlines and extra characters
    4. Removing diff markers (+, -, @)
    """
    # Step 1: Remove patch headers and multihunk headers
    lines = code.split('\n')
    content_lines = []
    
    for line in lines:
        # Skip empty lines
        if not line.strip():
            continue
        
        # Skip diff metadata lines (starting with ---, +++, @@)
        if any(line.strip().startswith(prefix) for prefix in ['---', '+++', '@@']):
            continue
        
        # Remove diff markers at the beginning of lines
        # if line.strip().startswith(('+', '-')):
        #     line = line[1:].strip()
            
        # Add non-empty lines
        if line.strip():
            content_lines.append(line.strip())
    
    # Step 2: Join into a single string
    content = ' '.join(content_lines)
    
    # Step 3: Remove duplicate quotes (""A becomes "A)
    import re
    content = re.sub(r'"{2,}', '"', content)
    
    # Step 4: Normalize all quotes to single quotes
    content = content.replace('"', "'")
    
    # Step 5: Remove all whitespace
    content = re.sub(r'\s+', '', content)
    
    return content

def are_codes_identical(code1, code2):
    """
    Compares two code snippets after normalization.
    Returns True if they are structurally identical.
    """
    norm_code1 = _normalize_code(code1)
    norm_code2 = _normalize_code(code2)
    
    return norm_code1 == norm_code2

""" Result Processing """
def get_response_result(response):
    yes_pattern = re.compile(r'\byes\b', re.IGNORECASE)
    no_pattern = re.compile(r'\bno\b', re.IGNORECASE)
    type_patterns = {}
    for type in ['1','2','3','4']:
        type_patterns[type] = re.compile(rf'\b(type {type}|type-{type}|t{type})\b', re.IGNORECASE)
    clone_pattern = re.compile(r'\b(are clones|are code clones)\b',  re.IGNORECASE)
    not_clone_pattern = re.compile(r"\b(are not clones|aren't clones|are not code clones|aren't code clones)\b",  re.IGNORECASE)
    
    features = {}
    features['yes'] = bool(yes_pattern.search(response))
    features['no'] = bool(no_pattern.search(response))
    features['clone'] = bool(clone_pattern.search(response))
    features['not_clone'] = bool(not_clone_pattern.search(response))
    features['1'] = bool(type_patterns['1'].search(response))
    features['2'] = bool(type_patterns['2'].search(response))
    features['3'] = bool(type_patterns['3'].search(response))
    features['4'] = bool(type_patterns['4'].search(response))
    
    feature_list = []
    if features['yes']:
        feature_list.append('yes')
    elif features['no']:
        feature_list.append('no')
    
    for type in ['1','2','3','4']:
        if features[type]:
            feature_list.append('t'+type)
    
    return feature_list

 
""" Patch Dataset Preprocessing"""
def remove_developer_identical_patches(tool_patches=None, developer_patches=None):
    logging.info("Removing Developer Identical-1 ...")

    # tool_patches['content'] = tool_patches['location'].apply(read_patch)
    tool_patches['content'] = tool_patches['target_methods'].apply(lambda x: read_file(x[0]) if isinstance(x, list) and len(x) > 0 else None)

    # get bugs with fixes
    unique_bugs = tool_patches["bug_uid"].unique()
    logging.info(f"Unique bugs: {len(unique_bugs)}")

    # Make the bugs a dataframe
    unique_bugs = pd.DataFrame(unique_bugs, columns=["bug_uid"])

    unique_bugs["total_patches"] = unique_bugs["bug_uid"].apply(lambda x: len(tool_patches[tool_patches["bug_uid"] == x]))

    logging.info(unique_bugs)

    # Create a copy of tool_patches to work with
    tool_patches_copy = tool_patches.copy()
    logging.info(f"Current Representatives: {tool_patches_copy}")

    # Remove developer identicals
    logging.info("Removing developer identicals ...")
    logging.info(f"Developer patches: {len(developer_patches)}")
    logging.info(f"Current Representatives: {tool_patches_copy}")
    # developer_patches["content"] = developer_patches["location"].apply(read_patch)
    developer_patches['content'] = developer_patches['target_methods'].apply(lambda x: read_file(x[0]) if isinstance(x, list) and len(x) > 0 else None)


    # Track patches dropped due to being identical to developer patches
    tool_patches_copy["is_identical_to_dev"] = tool_patches_copy.apply(lambda row: row["content"] == developer_patches[developer_patches["bug_uid"] == row["bug_uid"]]["content"].values[0], axis=1)
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
        merged_data.groupby(['generator', 'benchmark', 'correctness'])
            .size()
            .unstack(fill_value=0)
            .reset_index()
    )

    summary_table['C/O'] = (
        summary_table.get('Correct', 0).astype(str) + '/' + summary_table.get('Overfitting', 0).astype(str)
    )

    final_table = summary_table.pivot(index='generator', columns='benchmark', values='C/O')
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



""" Patches """
def are_single_hunks(patch: pd.Series, developer_patches: pd.DataFrame) -> bool:
    if get_patch_edit_numbers(patch["location"])["hunk"] != 1:
        return False
    
    if get_patch_edit_numbers(developer_patches.loc[f"{patch['bug_uid']}-developer"]["location"])["hunk"] != 1:
        return False
    
    return True

def is_single_hunk(patch: pd.Series):
    if get_patch_edit_numbers(patch["location"])["hunk"] != 1:
        return False
    
    return True

def get_single_hunks(patches: pd.DataFrame, developer_patches: pd.DataFrame) -> pd.DataFrame:
    if os.path.exists(TMP_SINGLE_HUNK_TOOL_PATHCES_PKL):
        logging.info("Loading single hunk tool patches from file ...")

        return pd.read_pickle(TMP_SINGLE_HUNK_TOOL_PATHCES_PKL)

    # single_hunk_tool_patches = cleaned_tool_patches[cleaned_tool_patches.apply(lambda patch: are_single_hunks(patch, cleaned_developer_patches), axis=1)] # Also get single hunk dev patches.
    single_hunk_tool_patches = patches[patches.apply(is_single_hunk, axis=1)]

    # # are all single hunks have only one  source_method source_method is a list? Yes
    # print(len(single_hunk_tool_patches))
    # answer = single_hunk_tool_patches[single_hunk_tool_patches['source_methods'].apply(lambda x: isinstance(x, list) and len(x) == 1)]
    # print(f"Single Hunk Tool Patches with only one source_method: {len(answer)}")

    # Save to file
    single_hunk_tool_patches.to_pickle(TMP_SINGLE_HUNK_TOOL_PATHCES_PKL)
    logging.info(f"Single hunk tool patches saved to {TMP_SINGLE_HUNK_TOOL_PATHCES_PKL}")

    return single_hunk_tool_patches

def get_pairs(patches): # Aligns with exp1 sahand-exp1-2 CloneHelper
    logging.info("Generating pairwise bug-based DataFrame ...")
    # correct_patches = patches[patches["correctness"] == "Correct"].copy()
    unique_bug_uids = patches['bug_uid'].nunique()
    logging.info("Unique bug UIDs in the cleaned tool patches with correctness 'Correct':")
    logging.info(unique_bug_uids)
    pairwise_rows = []
    for bug_uid, group in patches.groupby("bug_uid"):
        uids = group.index.tolist()  
        for uid1, uid2 in combinations(uids, 2):
            pairwise_rows.append({
                "uid": uid1,
                "groundtruth_index": uid2,
                "expert_label": "-"
            })

    pairwise_df = pd.DataFrame(pairwise_rows)

    logging.info("Pairwise DataFrame:")
    pairwise_df.to_pickle(os.path.join(TMP_PLOTS_DIR, "pairwise-bug-correct-deduplicated.pkl"))

    logging.info(pairwise_df)

    return pairwise_df






if __name__ == '__main__':
    pass