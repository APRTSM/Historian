import json
from collections import defaultdict

PROMPT_TO_COLUMN = {
    # SS (semantic)
    "llm4cc-simple_prompt-semantical": ("SS", "S"),
    "llm4cc-reasoning-patch-semantical": ("SS", "R"),
    "llm4cc-similarity_line-patch-semantical": ("SS", "LS"),

    # SI (identical)
    "llm4cc-simple_prompt-identical": ("SI", "S"),
    "llm4cc-reasoning-patch-identical": ("SI", "R"),
    "llm4cc-similarity_line-patch-identical": ("SI", "LS"),

    # CC
    "llm4cc-clone_type": ("CC", "SCC"),
    # "llm4cc-clone_type-patch": ("CC", "SCC"),
    "llm4cc-integrated": ("CC", "I"),
    # "llm4cc-integrated-patch": ("CC", "I"),
}



MODEL_MAP = {
    "magicoder:7b-s-cl": "MC7B",
    "codellama:7b-instruct": "CL7B",
    "deepseek-coder:6.7b": "DSC6.7B",
    "codegemma:7b-instruct": "CG7B",
    "qwen2.5:7b": "QW7B",
    "qwen2.5-coder:7b": "QWC7B",
    "yi-coder:9b": "YC9B",
    "hermes3:8b": "H8B",
}




METRICS = [
    "regex_portion_yes-no",
    # "total_yes-no",
    "zero_shot_acc_yes-no",
    # "regex_detected_yes-no",
    "regex_portion_type-1-type-2-type-3-type-4-not-clone",
    # "total_type-1-type-2-type-3-type-4-not-clone",
    "zero_shot_acc_type-1-type-2-type-3-type-4-not-clone",
    # "regex_detected_type-1-type-2-type-3-type-4-not-clone",
]


COLUMN_ORDER = [
    ("SS", "S"), ("SS", "R"), ("SS", "LS"),
    ("SI", "S"), ("SI", "R"), ("SI", "LS"),
    ("CC", "SCC"), ("CC", "I"),
]



# ---------- Load JSON ----------
with open("tmp/plots/rq3/archive/evaluation_results_method.json") as f:
    data = json.load(f)

# ---------- Create empty tables ----------
tables = {
    metric: defaultdict(lambda: {col: None for col in COLUMN_ORDER})
    for metric in METRICS
}

# ---------- Fill tables ----------
for row in data:
    model_uid = eval(row["model"])["uid"]
    prompt_uid = eval(row["prompt"])["uid"]

    if model_uid not in MODEL_MAP:
        continue
    if prompt_uid not in PROMPT_TO_COLUMN:
        continue

    model = MODEL_MAP[model_uid]
    column = PROMPT_TO_COLUMN[prompt_uid]

    for metric in METRICS:
        tables[metric][model][column] = row.get(metric)

# ---------- Pretty printer ----------
def print_table(title, table):
    print("\n" + "=" * 110)
    print(title.upper())
    print("=" * 110)

    header = ["Model"]
    for g, m in COLUMN_ORDER:
        header.append(f"{g}-{m}")
    print("{:<8}".format(header[0]) + "".join(f"{h:>12}" for h in header[1:]))

    for model in sorted(table.keys()):
        row = [model]
        for col in COLUMN_ORDER:
            val = table[model][col]
            if val is None:
                row.append("-")
            elif isinstance(val, float):
                row.append(f"{val:.3f}")
            else:
                row.append(str(val))

        print("{:<8}".format(row[0]) + "".join(f"{c:>12}" for c in row[1:]))

# ---------- Print all 8 tables ----------
for metric in METRICS:
    print_table(metric, tables[metric])
