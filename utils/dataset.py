import os
import logging
from .utils import *
from .config import *
import json


def get_dl4pc_exp2_dataset(bugs):
    logging.info("Extracting DL4PC patches from second experiment ...")
    patches = []

    for root, _, files in os.walk(DL4PC2_DIR):
        for file in files:
            # Clear values from previous patch to prevent errors
            bug_uid = tool = correctness = patch_number = None

            # Start identifying the patch
            if not file.endswith(".txt"):
                continue

            patch = {}
            correctness_info, tool, benchmark_bug, patch_number= os.path.join(root, file).split("/")[-4:]
            patch_number = patch_number.replace(".txt", "")
            benchmark = benchmark_bug.split("-")[0]
            
            location = os.path.relpath(os.path.join(root, file), start=PROJECT_DIR)

            # Find the corresponding bug_info
            if benchmark == "Defects4J":
                bug_project, bug_number = benchmark_bug.split("-")[1:]
                bug_info = {"benchmark": benchmark, "project": bug_project, "number": bug_number}


            elif benchmark == "Bugs.jar":
                bug_project, bug_number = benchmark_bug.split("-")[1:]

                bug_project_folder_name_map = {
                    "math": "commons-math",
                    "OAK": "jackrabbit-oak",
                    "log4j2": "logging-log4j2"
                }

                if bug_project in bug_project_folder_name_map:
                    bug_project_folder_name = bug_project_folder_name_map[bug_project]

                else:
                    bug_project_folder_name = bug_project

                bug_info = {"benchmark": benchmark, "project": bug_project_folder_name, "number": bug_number}

            elif benchmark == "Bears":
                bug_number = benchmark_bug.split("-")[1]
                bug_info = {"benchmark": benchmark, "number": bug_number}

            elif benchmark == "QuixBugs":
                bug_name = benchmark_bug.split("-")[1]
                bug_info = {"benchmark": benchmark, "language": "Java", "project": bug_name}

            else:
                logging.warning(f"Unknown benchmark {benchmark} in DL4PC second experiment patch extraction.")
                continue

            # Match the info with an existing bug
            bug = get_record(bugs, bug_info)

            if not bug:
                raise Exception(f"Bug not found for APRE NFL patch: {bug_info}")

    
            bug_uid = bug["uid"]
            
            # Set correctness
            if correctness_info == "correct-patches":
                correctness = "Correct"

            else:
                correctness = "Overfitting"

            # Continue before this statement if not valid
            assert bug_uid and tool and correctness and patch_number

            patch = {
                "uid": f"dl4pc2-{bug_uid}-{tool}-{patch_number}",
                "bug_uid": bug_uid,
                "generator": tool,
                "location": os.path.relpath(location, PROJECT_DIR),
                "correctness": correctness,
                "origin": "DL4PC-E2"
            }

            patches.append(patch) 

    no_correct_patches = len(get_objects_by_feature(patches, "correctness", "Correct"))
    no_overfitting_patches = len(get_objects_by_feature(patches, "correctness", "Overfitting"))

    logging.info(f"{no_correct_patches} Correct and {no_overfitting_patches} Overfitting patches extracted from DL4PC, second experiment.")

    return patches

def get_apre_nfl_dataset(bugs):
    logging.info("Extracting APRE NFL patches ...")

    patches = []
    nfl_dir = os.path.join(APRE_NFL_DIR)
    skipped = []  # To track missing ones

    for root, dirs, files in os.walk(nfl_dir):
        for file in files:
            tool, project_id_correctness = root.split('/')[-2:]
            project_id, correctness = project_id_correctness.split('_')

            if correctness == 'C':
                correctness = "Correct"
            else:
                correctness = "Overfitting"

            project, id = project_id.split('-')

            bug_info = {"benchmark": "Defects4J", "project": project, "number": id}
            bug = get_record(bugs, bug_info)

            if not bug:
                raise Exception(f"Bug not found for APRE NFL patch: {bug_info}")

            bug_uid = bug["uid"]

            # Remove `.txt` from name (if any)
            patch_name = file[:-4] if file.endswith(".txt") else file

            patch = {
                "uid": f"aprenfl-{bug_uid}-{tool}-{patch_name}",
                "bug_uid": bug_uid,
                "generator": tool,
                "location": os.path.relpath(os.path.join(root, file), PROJECT_DIR),
                "correctness": correctness,
                "origin": "APRE-NFL"
            }
            patches.append(patch)

    no_correct_patches = len(get_objects_by_feature(patches, "correctness", "Correct"))
    no_overfitting_patches = len(get_objects_by_feature(patches, "correctness", "Overfitting"))

    logging.info(f"{no_correct_patches} Correct and {no_overfitting_patches} Overfitting patches extracted from APRE study (NFL).")
    logging.info(f"{len(skipped)} patches skipped (no matching bug).")

    # Log details of missing ones
    for s in skipped:
        logging.warning(f"Skipped: {s['project']}-{s['id']} ({s['file']}) in {s['root']}")

    return patches

def get_apre_pfl_dataset(bugs):
    logging.info("Extracting APRE PFL patches ...")

    patches = []
    pfl_dir = os.path.join(APRE_PFL_DIR)
    skipped = []  # To track missing ones

    for root, dirs, files in os.walk(pfl_dir):
        for file in files:
            tool, project_id_correctness = root.split('/')[-2:]
            project_id, correctness = project_id_correctness.split('_')

            if correctness == 'C':
                correctness = "Correct"
            else:
                correctness = "Overfitting"

            project, id = project_id.split('-')

            bug_info = {"benchmark": "Defects4J", "project": project, "number": id}
            bug = get_record(bugs, bug_info)

            if not bug:
                skipped.append({"project": project, "id": id, "file": file, "root": root})
                continue

            bug_uid = bug["uid"]

            # Remove `.txt` from name (if any)
            patch_name = file[:-4] if file.endswith(".txt") else file

            patch = {
                "uid": f"aprenfl-{bug_uid}-{tool}-{patch_name}",
                "bug_uid": bug_uid,
                "generator": tool,
                "location": os.path.relpath(os.path.join(root, file), PROJECT_DIR),
                "correctness": correctness,
                "origin": "APRE-NFL"
            }
            patches.append(patch)

    no_correct_patches = len(get_objects_by_feature(patches, "correctness", "Correct"))
    no_overfitting_patches = len(get_objects_by_feature(patches, "correctness", "Overfitting"))

    logging.info(f"{no_correct_patches} Correct and {no_overfitting_patches} Overfitting patches extracted from APRE study (NFL).")
    logging.info(f"{len(skipped)} patches skipped (no matching bug).")

    # Log details of missing ones
    for s in skipped:
        logging.warning(f"Skipped: {s['project']}-{s['id']} ({s['file']}) in {s['root']}")

    return patches



""" DefectRepairing """
def get_defectrepairing_dataset(bugs):
    logging.info("Extracting DefectRepairing patches ...")

    patches = []
    info_dir = os.path.join(DEFECTREPAIRING_DIR, "INFO")
    
    for root, _, files in os.walk(DEFECTREPAIRING_DIR):
        for patch_file in files:
            bug_uid = tool = patch_number = patch_file_path = correctness = None

            if patch_file.endswith(".json") or  patch_file.endswith(".md"):
                continue

            info_file_path = os.path.join(info_dir, f"{patch_file}.json")

            with open(info_file_path, 'r') as f:
                info = json.load(f)

            if info["correctness"] == "Unknown":
                continue

            bug_info = {"benchmark": "Defects4J", "project": info["project"], "number": info["bug_id"]}
            bug = get_record(bugs, bug_info)

            if not bug:
                continue

            tool = info["tool"]
            correctness = info["correctness"]

            if correctness == "Incorrect":
                correctness = "Overfitting"

            bug_uid = bug["uid"]
            patch_number = info["ID"]
            patch_file_path = os.path.join(root, patch_file)

            # Continue before this
            assert bug_uid and tool and patch_number and patch_file_path and correctness 

            patch = {
                "uid": f"defectrepairing-{bug_uid}-{tool}-{patch_number}",
                "bug_uid": bug_uid,
                "generator": tool,
                "location": os.path.relpath(patch_file_path, PROJECT_DIR),
                "correctness": correctness,
                "origin": "DefectRepairing"
            }

            patches.append(patch) 

    no_correct_patches = len(get_objects_by_feature(patches, "correctness", "Correct"))
    no_overfitting_patches = len(get_objects_by_feature(patches, "correctness", "Overfitting"))

    logging.info(f"{no_correct_patches} Correct and {no_overfitting_patches} Overfitting patches extracted from DefectRepairing study.")


    return patches


""" DRR """
def get_drr_dataset(bugs):
    logging.info("Extracting DRR patches ...")

    patches = []
    
    for root, _, files in os.walk(DRR_DIR):
        for patch_file in files:    
            if not patch_file.endswith(".patch"):
                continue
            
            if "Dunassessed" in root:
                continue

            patch_name, project, number, tool = patch_file.replace(".patch", "").replace("-plausible", "").split('-') 
            bug_info = {"benchmark": "Defects4J", "project": project, "number": number}
            bug = get_record(bugs, bug_info)

            if not bug:
                logging.warning(f"Skipping patch with no bug record: {os.path.join(root, patch_file)}")
                continue

            bug_uid = bug["uid"]

            if "Dcorrect" in root:
                correctness = "Correct"

            else:
                correctness = "Overfitting"

            patch = {
                "uid": f"drr-{bug_uid}-{tool}-{patch_name}",
                "bug_uid": bug_uid,
                "generator": tool,
                "location": os.path.relpath(os.path.join(root, patch_file), PROJECT_DIR),
                "correctness": correctness,
                "origin": "DRR"
            }
            patches.append(patch)

    no_correct_patches = len(get_objects_by_feature(patches, "correctness", "Correct"))
    no_overfitting_patches = len(get_objects_by_feature(patches, "correctness", "Overfitting"))

    logging.info(f"{no_correct_patches} Correct and {no_overfitting_patches} Overfitting patches extracted from DRR study.")

    return patches


""" WangICSE """
def get_wangicse_dataset(bugs):
    logging.info("Extracting WangICSE patches ...")

    patches = []
    
    for root, _, files in os.walk(WANGICSE_DIR):
        for patch_file in files:    
            if not patch_file.endswith(".patch"):
                continue
            
            if "Error" in root:
                logging.warning(f"Skipping patch in error folder: {os.path.join(root, patch_file)}")
                continue

            patch_name, project, number, tool = patch_file.replace(".patch", "").replace("-plausible", "").replace("-plusible", "").split('-') 
            bug_info = {"benchmark": "Defects4J", "project": project, "number": number}
            bug = get_record(bugs, bug_info)

            if not bug:
                logging.warning(f"Skipping patch with no bug record: {os.path.join(root, patch_file)}")
                continue

            bug_uid = bug["uid"]

            if "Doverfitting" in root:
                correctness = "Overfitting"

            else:
                correctness = "Correct"

            patch = {
                "uid": f"wangicse-{bug_uid}-{tool}-{patch_name}",
                "bug_uid": bug_uid,
                "generator": tool,
                "location": os.path.relpath(os.path.join(root, patch_file), PROJECT_DIR),
                "correctness": correctness,
                "origin": "WangICSE"
            }
            patches.append(patch)

    no_correct_patches = len(get_objects_by_feature(patches, "correctness", "Correct"))
    no_overfitting_patches = len(get_objects_by_feature(patches, "correctness", "Overfitting"))

    logging.info(f"{no_correct_patches} Correct and {no_overfitting_patches} Overfitting patches extracted from Wang's ICSE study.")

    return patches


""" Historian """
def get_historian_dataset(bugs):
    logging.info("Extracting Historian patches ...")

    patches = []
    
    for filename in os.listdir(HISTORIAN_DIR):
        file_path = os.path.join(HISTORIAN_DIR, filename)

        uid = filename.replace(".patch", "")
        _, _, bug_project, bug_number, tool, _ = filename.replace(".patch", "").split('-')

        if type(bugs) == dict:
            bug_info = {"benchmark": "Defects4J", "project": bug_project, "number": bug_number}
            bug = get_record(bugs, bug_info)
        
        else:
            bug = bugs.loc[f"defects4j-{bug_project}-{bug_number}"].copy()
            bug['uid'] = bug.name

        bug_uid = bug["uid"]

        patch = {
            "uid": uid,
            "bug_uid": bug_uid,
            "generator": tool,
            "location": os.path.relpath(file_path, PROJECT_DIR),
            "correctness": "Unknown",
            "origin": "Historian"
        }
        patches.append(patch)

    return patches

""" LLM4PatchCorrect """
def get_llm4pc_dataset(bugs):
    logging.info("Extracting LLM4PC patches ...")

    patches = []

    df = pd.read_csv(os.path.join(LLM4PC_DIR, "all_data_v1.csv"))

    # Developer patches are not considered seperately
    df = df[df["tool"] != "defects4j-developer"]

    defectrepairing_patches = get_defectrepairing_dataset(bugs)

    for index, row in df.iterrows():
        bug_info = {"benchmark": "Defects4J", "project": row["project"], "number": str(row["bug_id"])}

        bug = get_record(bugs, bug_info)

        if not bug:
            # Depricated bugs in Defects4J v2.0
            # if bug_info["project"] == "Closure" and bug_info["number"] in ["63", "93"]:
            #     logging.info(f"Skipping deprecated bug in LLM4PC: {bug_info}")
            #     continue

            raise Exception(f"Bug not found in LLM4PC: {bug_info}")

        bug_uid = bug["uid"]
        tool = row["tool"]

        patch_name = row["filename"].split("-")[0]

        if row["label"]:
            correctness = "Overfitting"

        else:
            correctness = "Correct"

        location = os.path.join(CSMALL_DIR, correctness.lower(), tool, row["project"], row["filename"])

        if not os.path.exists(location):
            search_location = WANGICSE_DIR
            file_location = find_file_relative(search_location, row["filename"])
            origin_location = WANGICSE_DIR


            if not file_location:
                search_location = DRR_DIR
                file_location = find_file_relative(search_location, row["filename"])
                origin_location = DRR_DIR

            if not file_location:
                file_name_modified = row["filename"].replace("-plausible", "")
                file_location = find_file_relative(search_location, file_name_modified)
                origin_location = DRR_DIR

            if not file_location:
                file_name_modified = row["filename"].replace("jGenProg-plausible", "JGenProg2017")
                file_location = find_file_relative(search_location, file_name_modified)
                origin_location = DRR_DIR

            if not file_location:
                record = {
                    "bug_uid": bug_uid,
                    "generator": row["filename"].replace("-plausible", "").replace(".patch", "").split("-")[-1],
                    "correctness": correctness,
                }

                defectreparing_record = get_record(defectrepairing_patches, record)

                if defectreparing_record:
                    file_location = defectreparing_record["location"]
                    origin_location = PROJECT_DIR
                

            if not file_location: # Search
                logging.error(f"Patch not found in LLM4PC: {location}")

                if "patch1-Math-73-jGenProg.patch" in location or "patch1-Math-50-jGenProg.patch" in location:
                    logging.info("Known missing patch in LLM4PC, skipping...")
                    continue

                raise Exception(f"Patch not found in LLM4PC: {location}")

            location = os.path.join(origin_location, file_location)

            if not os.path.exists(location):
                logging.error(f"Location not found in LLM4PC: {location}")

                raise Exception(f"Location not found in LLM4PC: {location}")

        assert bug_uid and tool and patch_name and correctness and location

        patch = {
            "uid": f"llm4pc-{bug_uid}-{tool}-{patch_name}",
            "bug_uid": bug_uid,
            "generator": tool,
            "location": os.path.relpath(location, PROJECT_DIR),
            "correctness": correctness,
            "origin": "LLM4PC"
        }

        patches.append(patch)

    return patches



""" General """
# Get all patches (from datasets)
def get_patches(bugs):
    patches = []
    patches += get_dl4pc_exp2_dataset(bugs)
    print(len(patches))
    patches += get_apre_nfl_dataset(bugs)
    print(len(patches))
    patches += get_defectrepairing_dataset(bugs)
    print(len(patches))
    patches += get_drr_dataset(bugs)
    print(len(patches))
    # patches += get_wangicse_dataset(bugs)
    # print(len(patches))
    patches += get_llm4pc_dataset(bugs)
    print(len(patches))

    return patches


