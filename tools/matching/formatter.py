import subprocess
import os
import javalang
import re

def _remove_java_comments(code):
    """
    Remove all Java comments (single-line, multi-line, and javadoc).
    """
    # Remove single-line comments (//)
    code = re.sub(r'//.*$', '', code, flags=re.MULTILINE)
    
    # Remove multi-line comments (/* ... */) and javadoc (/** ... */)
    code = re.sub(r'/\*.*?\*/', '', code, flags=re.DOTALL)
    
    return code

def _format_java_code(code):
    """
    Format Java code with proper indentation and spacing.
    """
    # Remove extra whitespace and normalize
    code = re.sub(r'\s+', ' ', code.strip())
    
    # Add newlines after specific tokens
    code = re.sub(r'(\{)', r'\1\n', code)
    code = re.sub(r'(\})', r'\n\1\n', code)
    code = re.sub(r'(;)', r'\1\n', code)
    
    # Split into lines and process
    lines = code.split('\n')
    formatted_lines = []
    indent_level = 0
    
    for line in lines:
        line = line.strip()
        if not line:
            continue
            
        # Decrease indent for closing braces
        if line.startswith('}'):
            indent_level = max(0, indent_level - 1)
        
        # Add indentation
        formatted_line = '    ' * indent_level + line
        formatted_lines.append(formatted_line)
        
        # Increase indent for opening braces
        if line.endswith('{'):
            indent_level += 1
    
    return '\n'.join(formatted_lines)

def _normalize_strings_and_numbers(code):
    """
    Replace all strings with "" and all numbers with 1.
    """
    # Replace all string literals (including escape sequences)
    code = re.sub(r'"([^"\\]|\\.)*"', '""', code)
    code = re.sub(r"'([^'\\]|\\.)*'", "''", code)
    
    # Replace all numeric literals (integers, floats, hex, etc.) including negative numbers
    code = re.sub(r'-?\b\d+\.?\d*[fFdDlL]?\b', '1', code)
    code = re.sub(r'-?\b0[xX][0-9a-fA-F]+[lL]?\b', '1', code)  # hex numbers
    code = re.sub(r'-?\b0[bB][01]+[lL]?\b', '1', code)  # binary numbers
    code = re.sub(r'-?\b0[0-7]+[lL]?\b', '1', code)  # octal numbers
    
    return code

def _format_java_with_parser(code):
    """
    Format Java code using javalang parser for better accuracy.
    Removes all comments, normalizes strings/numbers, and formats the code.
    """
    
    # Remove comments first
    code = _remove_java_comments(code)
    
    # Normalize strings and numbers
    code = _normalize_strings_and_numbers(code)
    
    # Try to parse to validate syntax
    try:
        tree = javalang.parse.parse(code)
    except:
        pass  # Continue with formatting even if parsing fails
    
    # Format the code
    return _format_java_code(code)
        
def _format_java_with_google_formatter(code, jar_path="google-java-format-1.17.0-all-deps.jar"):
    """
    Format Java code using google-java-format with Java 11+.
    Wraps code snippets in a class if needed.
    """
    
    # Check if code is a snippet (doesn't contain class/interface/enum)
    is_snippet = not any(keyword in code for keyword in ['class ', 'interface ', 'enum ', 'record '])
    
    if is_snippet:
        wrapped_code = f"public class TempClass {{\n{code}\n}}"
    else:
        wrapped_code = code
    
    java_paths = [
        "/usr/lib/jvm/java-17-openjdk-amd64/bin/java",
        "/usr/lib/jvm/java-11-openjdk-amd64/bin/java"
    ]
    
    for java_path in java_paths:
        try:
            if os.path.exists(java_path):
                result = subprocess.run([
                    java_path, '-jar', jar_path, '-'
                ], input=wrapped_code, capture_output=True, text=True)
                
                formatted_code = result.stdout
                
                # If we wrapped it, unwrap it
                if is_snippet:
                    # Remove the class wrapper
                    lines = formatted_code.split('\n')
                    # Find content between the braces
                    start_idx = next(i for i, line in enumerate(lines) if '{' in line) + 1
                    end_idx = next(i for i in range(len(lines)-1, -1, -1) if '}' in lines[i])
                    
                    inner_lines = lines[start_idx:end_idx]
                    # Remove one level of indentation
                    unwrapped_lines = []
                    for line in inner_lines:
                        if line.startswith('  '):
                            unwrapped_lines.append(line[2:])
                        else:
                            unwrapped_lines.append(line)
                    
                    formatted_code = '\n'.join(unwrapped_lines).strip()
                
                return formatted_code
                
        except Exception as e:
            continue

    return code

def run_formatter(code, use_google_formatter=True):
    """
    Run the formatter on the provided Java code.
    If use_google_formatter is True, uses google-java-format.
    Otherwise, uses javalang parser for formatting.
    """
    return _format_java_with_parser(_format_java_with_google_formatter(_format_java_with_parser(code)))

def _get_java_version():
    """Get current Java version"""
    try:
        result = subprocess.run(['java', '-version'], capture_output=True, text=True)
        return result.stderr.split('\n')[0]
    except:
        return "Java version not found"

# Example usage
if __name__ == "__main__":
    print(_get_java_version())
    code = """public class Example{
            // s
            /*     * This is a sample Java class
            * with a method that demonstrates formatting.
            */
        public void testMethod(){ // This is a single-line comment 
            int x=5;if(x>0){System.out.println("Positive");} else{System.out.println("Non-

            positive");}
            if(x>10)


        {System.out.println("Greater than 10");if(x>100) {System.out.println("Huge");}}/* multi-line comment
            * that should be removed
            */
        else
        {System.out.println("Small or equal to 10");}

        int index = -1;


        
        String a="hello";String b="world";System.out.println(a+b );;
        }
        }
    """
    
    print("Original:")
    print(code)
    print("\n" + "="*50 + "\n")
    
    formatted = run_formatter(code)
    print("Formatted:")
    print(formatted)
    print(_get_java_version())



