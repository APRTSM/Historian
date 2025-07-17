import subprocess
import json
import sys
import argparse
from formatter import run_formatter

def get_varmap_type(fixed_code, similar_code, line_number, end_line_number):
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
    """ Run get_varmap_type for all lines """
    code_1 = run_formatter(code_1)
    code_2 = run_formatter(code_2)
    code_1_lines = code_1.splitlines()
    max_lines = len(code_1_lines) + 1
    match_list = []
    
    for line_number in range(3, max_lines):
        type = get_varmap_type(code_1, code_2, line_number, line_number)

        if type == -1:
            return -1
        
        match_list.append(type)
        # print(f"Line {line_number}: {type}, Line: {code_1_lines[line_number - 2].strip()}")
    
    return min(match_list)

def main():
    parser = argparse.ArgumentParser(description='Compare two code snippets for clone detection')
    parser.add_argument('--file1', '-f1', help='Path to first code file')
    parser.add_argument('--file2', '-f2', help='Path to second code file')
    parser.add_argument('--code1', '-c1', help='First code snippet as string')
    parser.add_argument('--code2', '-c2', help='Second code snippet as string')
    
    args = parser.parse_args()
    
    # Option 1: Read from files
    if args.file1 and args.file2:
        try:
            with open(args.file1, 'r') as f1:
                code_1 = f1.read()
            with open(args.file2, 'r') as f2:
                code_2 = f2.read()
        except FileNotFoundError as e:
            print(f"Error: {e}")
            sys.exit(1)
    
    # Option 2: Direct code input
    elif args.code1 and args.code2:
        code_1 = args.code1
        code_2 = args.code2
    
    # Option 3: Interactive input
    elif len(sys.argv) == 1:
        print("Enter first code snippet (end with 'END' on a new line):")
        code_1_lines = []
        while True:
            line = input()
            if line.strip() == 'END':
                break
            code_1_lines.append(line)
        code_1 = '\n'.join(code_1_lines)
        
        print("Enter second code snippet (end with 'END' on a new line):")
        code_2_lines = []
        while True:
            line = input()
            if line.strip() == 'END':
                break
            code_2_lines.append(line)
        code_2 = '\n'.join(code_2_lines)
    
    else:
        print("Usage examples:")
        print("1. Using files: python script.py -f1 code1.java -f2 code2.java")
        print("2. Using direct input: python script.py -c1 'code here' -c2 'code here'")
        print("3. Interactive mode: python script.py")
        sys.exit(1)
    
    # Run the comparison
    clone_type = get_clone_type(code_1, code_2)
    print(clone_type)

if __name__ == "__main__":
    main()