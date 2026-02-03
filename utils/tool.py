import json
import os
from .utils import execute_bash_command
from .config import *
import javalang
import re



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
    code_1_dir = os.path.join(TMP_DIR, "code_1.java")
    code_2_dir = os.path.join(TMP_DIR, "code_2.java")

    with open(code_1_dir, "w") as f:
        f.write(code_1)
    with open(code_2_dir, "w") as f:
        f.write(code_2)

    command = f'python wrapper.py -f1 "{code_1_dir}" -f2 "{code_2_dir}"'
    output, _ = execute_bash_command(command, dir=MATCHING_DIR, error_allowed=True)

    # Remove the files
    os.remove(code_1_dir)
    os.remove(code_2_dir)

    try:
        output = int(output)
        
    except ValueError:
        output = -1

    return output



""" Implementation """



def get_parse_tree(code: str) -> str:
    try:
        tree = javalang.parse.parse(code)
        return _tree_to_string(tree)
    except javalang.parser.JavaSyntaxError:
        pass
    
    try:
        wrapped = f"class _Wrapper {{ void _method() {{ {code} }} }}"
        tree = javalang.parse.parse(wrapped)
        for _, node in tree.filter(javalang.tree.MethodDeclaration):
            if node.name == "_method" and node.body:
                return _tree_to_string(node.body)
        return _tree_to_string(tree)
    except javalang.parser.JavaSyntaxError:
        pass
    
    try:
        wrapped = f"class _Wrapper {{ {code} }}"
        tree = javalang.parse.parse(wrapped)
        for _, node in tree.filter(javalang.tree.ClassDeclaration):
            if node.name == "_Wrapper" and node.body:
                return _tree_to_string(node.body)
        return _tree_to_string(tree)
    except javalang.parser.JavaSyntaxError as e:
        raise ValueError(f"Could not parse Java code: {e}")


def _tree_to_string(node, indent: int = 0) -> str:
    if node is None:
        return "None"
    
    if isinstance(node, (str, int, float, bool)):
        return repr(node)
    
    if isinstance(node, (list, tuple)):
        if not node:
            return "[]"
        items = [_tree_to_string(item, indent + 1) for item in node]
        return "[\n" + ",\n".join(items) + "\n" + "  " * indent + "]"
    
    if isinstance(node, set):
        if not node:
            return "{}"
        items = sorted([_tree_to_string(item, indent + 1) for item in node])
        return "{" + ", ".join(items) + "}"
    
    if hasattr(node, '__class__') and hasattr(node, 'attrs'):
        node_type = node.__class__.__name__
        prefix = "  " * indent
        
        lines = [f"{prefix}{node_type}("]
        
        for attr_name in node.attrs:
            attr_value = getattr(node, attr_name, None)
            if attr_value is not None:
                attr_str = _tree_to_string(attr_value, indent + 1)
                lines.append(f"{prefix}  {attr_name}={attr_str},")
        
        lines.append(f"{prefix})")
        return "\n".join(lines)
    
    return repr(node)


def match_type1(code_1: str, code_2: str) -> bool:
    try:
        tree_1 = get_parse_tree(code_1)
        tree_2 = get_parse_tree(code_2)
        return tree_1 == tree_2
    except ValueError:
        return False


def get_parse_tree_normalized(code: str) -> str:
    try:
        tree = javalang.parse.parse(code)
        return _tree_to_string_normalized(tree, {}, [0])
    except javalang.parser.JavaSyntaxError:
        pass
    
    try:
        wrapped = f"class _Wrapper {{ void _method() {{ {code} }} }}"
        tree = javalang.parse.parse(wrapped)
        for _, node in tree.filter(javalang.tree.MethodDeclaration):
            if node.name == "_method" and node.body:
                return _tree_to_string_normalized(node.body, {}, [0])
        return _tree_to_string_normalized(tree, {}, [0])
    except javalang.parser.JavaSyntaxError:
        pass
    
    try:
        wrapped = f"class _Wrapper {{ {code} }}"
        tree = javalang.parse.parse(wrapped)
        for _, node in tree.filter(javalang.tree.ClassDeclaration):
            if node.name == "_Wrapper" and node.body:
                return _tree_to_string_normalized(node.body, {}, [0])
        return _tree_to_string_normalized(tree, {}, [0])
    except javalang.parser.JavaSyntaxError as e:
        raise ValueError(f"Could not parse Java code: {e}")


def _normalize_literal(value: str) -> str:
    if value is None:
        return "$NULL"
    
    if value.startswith('"') and value.endswith('"'):
        return "$STRING"
    
    if value.startswith("'") and value.endswith("'"):
        return "$CHAR"
    
    if value in ('true', 'false'):
        return "$BOOL"
    
    if value == 'null':
        return "$NULL"
    
    if re.match(r'^-?[\d.]+[fFdD]?$', value) and ('.' in value or value[-1] in 'fFdD'):
        return "$FLOAT"
    if 'e' in value.lower() and re.match(r'^-?[\d.]+[eE][+-]?\d+[fFdD]?$', value):
        return "$FLOAT"
    
    if re.match(r'^-?(?:0[xX][\da-fA-F]+|0[bB][01]+|0[0-7]*|\d+)[lL]?$', value):
        return "$INT"
    
    return value


def _tree_to_string_normalized(node, name_map: dict, counter: list, indent: int = 0) -> str:
    if node is None:
        return "None"
    
    if isinstance(node, (bool,)):
        return repr(node)
    
    if isinstance(node, (int, float)):
        return "$NUM"
    
    if isinstance(node, str):
        return repr(node)
    
    if isinstance(node, (list, tuple)):
        if not node:
            return "[]"
        items = [_tree_to_string_normalized(item, name_map, counter, indent + 1) for item in node]
        return "[\n" + ",\n".join(items) + "\n" + "  " * indent + "]"
    
    if isinstance(node, set):
        if not node:
            return "{}"
        items = sorted([_tree_to_string_normalized(item, name_map, counter, indent + 1) for item in node])
        return "{" + ", ".join(items) + "}"
    
    if hasattr(node, '__class__') and hasattr(node, 'attrs'):
        node_type = node.__class__.__name__
        prefix = "  " * indent
        
        lines = [f"{prefix}{node_type}("]
        
        for attr_name in node.attrs:
            attr_value = getattr(node, attr_name, None)
            if attr_value is not None:
                if attr_name == 'value' and isinstance(node, javalang.tree.Literal):
                    normalized = _normalize_literal(attr_value)
                    lines.append(f"{prefix}  {attr_name}={repr(normalized)},")
                elif attr_name == 'name' and isinstance(node, (
                    javalang.tree.VariableDeclarator,
                    javalang.tree.FormalParameter,
                )):
                    if attr_value not in name_map:
                        name_map[attr_value] = f"$VAR{counter[0]}"
                        counter[0] += 1
                    lines.append(f"{prefix}  {attr_name}={repr(name_map[attr_value])},")
                elif attr_name == 'member' and isinstance(node, javalang.tree.MemberReference):
                    lines.append(f"{prefix}  {attr_name}='$VAR',")
                else:
                    attr_str = _tree_to_string_normalized(attr_value, name_map, counter, indent + 1)
                    lines.append(f"{prefix}  {attr_name}={attr_str},")
        
        lines.append(f"{prefix})")
        return "\n".join(lines)
    
    return repr(node)


def match_type2(code_1: str, code_2: str) -> bool:
    try:
        tree_1 = get_parse_tree_normalized(code_1)
        tree_2 = get_parse_tree_normalized(code_2)
        return tree_1 == tree_2
    except ValueError:
        return False

def get_parse_tree_type2_plus(code: str) -> str:
    try:
        tree = javalang.parse.parse(code)
        return _tree_to_string_type2_plus(tree, 0)
    except javalang.parser.JavaSyntaxError:
        pass
    
    try:
        wrapped = f"class _Wrapper {{ void _method() {{ {code} }} }}"
        tree = javalang.parse.parse(wrapped)
        for _, node in tree.filter(javalang.tree.MethodDeclaration):
            if node.name == "_method" and node.body:
                return _tree_to_string_type2_plus(node.body, 0)
        return _tree_to_string_type2_plus(tree, 0)
    except javalang.parser.JavaSyntaxError:
        pass
    
    try:
        wrapped = f"class _Wrapper {{ {code} }}"
        tree = javalang.parse.parse(wrapped)
        for _, node in tree.filter(javalang.tree.ClassDeclaration):
            if node.name == "_Wrapper" and node.body:
                return _tree_to_string_type2_plus(node.body, 0)
        return _tree_to_string_type2_plus(tree, 0)
    except javalang.parser.JavaSyntaxError as e:
        raise ValueError(f"Could not parse Java code: {e}")


def _tree_to_string_type2_plus(node, indent: int = 0) -> str:
    if node is None:
        return "None"
    
    if isinstance(node, (bool,)):
        return repr(node)
    
    if isinstance(node, (int, float)):
        return "$NUM"
    
    if isinstance(node, str):
        return repr(node)
    
    if isinstance(node, (list, tuple)):
        if not node:
            return "[]"
        items = [_tree_to_string_type2_plus(item, indent + 1) for item in node]
        return "[\n" + ",\n".join(items) + "\n" + "  " * indent + "]"
    
    if isinstance(node, set):
        if not node:
            return "{}"
        items = sorted([_tree_to_string_type2_plus(item, indent + 1) for item in node])
        return "{" + ", ".join(items) + "}"
    
    if hasattr(node, '__class__') and hasattr(node, 'attrs'):
        node_type = node.__class__.__name__
        prefix = "  " * indent
        
        # Replace type nodes entirely with $TYPE
        if isinstance(node, (javalang.tree.BasicType, javalang.tree.ReferenceType)):
            return f"{prefix}$TYPE"
        
        lines = [f"{prefix}{node_type}("]
        
        for attr_name in node.attrs:
            attr_value = getattr(node, attr_name, None)
            if attr_value is not None:
                # Normalize all literals to $LITERAL
                if attr_name == 'value' and isinstance(node, javalang.tree.Literal):
                    lines.append(f"{prefix}  {attr_name}='$LITERAL',")
                # Normalize variable names
                elif attr_name == 'name' and isinstance(node, (
                    javalang.tree.VariableDeclarator,
                    javalang.tree.FormalParameter,
                )):
                    lines.append(f"{prefix}  {attr_name}='$VAR',")
                # Normalize variable references
                elif attr_name == 'member' and isinstance(node, javalang.tree.MemberReference):
                    lines.append(f"{prefix}  {attr_name}='$VAR',")
                else:
                    attr_str = _tree_to_string_type2_plus(attr_value, indent + 1)
                    lines.append(f"{prefix}  {attr_name}={attr_str},")
        
        lines.append(f"{prefix})")
        return "\n".join(lines)
    
    return repr(node)


def match_type2_plus(code_1: str, code_2: str) -> bool:
    """
    Type-2+ clone detection.
    
    Ignores: variable names, all literals, variable types
    Matches on: structure, operators, method names
    
    Examples:
        int a = 5;   matches  String a = "Hi";
        5            matches  "Hi"
        5            matches  5.5
        y + a        NOT matches  y - a
    """
    try:
        tree_1 = get_parse_tree_type2_plus(code_1)
        tree_2 = get_parse_tree_type2_plus(code_2)
        return tree_1 == tree_2
    except ValueError:
        return False