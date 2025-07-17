import subprocess
import json
from formatter import run_formatter

def _get_varmap_type(fixed_code, similar_code, line_number, end_line_number):
    """
    Call Java program to get varMapType for two code snippets
    """
    cmd = [
        "java", 
        "-cp", "target/classes:target/dependency/*",
        "edu.lu.uni.serval.bug.fixer.SimpleVarMapTest",
        fixed_code,
        similar_code,
        str(line_number),
        str(end_line_number)
    ]
    
    result = subprocess.run(cmd, capture_output=True, text=True)
    
    if result.returncode != 0:
        return -1
    
    map = {
        "Final results SPECIALCHARACTER: Error: null": -1,
        "Final results SPECIALCHARACTER: No matches found": -1,
        "varMapType:ori": 1,
        "varMapType:0": 0,
        "varMapType:1": 0,
    }

    for key, type in map.items():
        if key in result.stdout:
            return type

    return -1

def get_clone_type(code_1, code_2):
    """ Run _get_varmap_type for all lines """
    code_1 = run_formatter(code_1)
    code_2 = run_formatter(code_2)

    code_1_lines = code_1.splitlines()
    max_lines = len(code_1_lines) + 1
    match_list = []

    for line_number in range(3, max_lines):
        type = _get_varmap_type(code_1, code_2, line_number, line_number)
        
        if type == -1:
            return -1

        match_list.append(type)
        # print(f"Line {line_number}: {type}, Line: {code_1_lines[line_number - 2].strip()}")

    return min(match_list)

# Example usage
if __name__ == "__main__":
    code_1 = """public void method() {
            String str = "test";
            List list = new ArrayList();

            // s
            

            String item = "test";String itemm = "test"; List.add(item);
            String item2 = "test";
        }
    """
    
    code_2 = """public void method2() {
            String str = "test";

            List myList = new ArrayList();
            String itemm = "test";
            String item = "test";
            List.add(itemm);
            String item = "test";
        }
    """

    type = get_clone_type(code_1, code_2)

    print(type)

