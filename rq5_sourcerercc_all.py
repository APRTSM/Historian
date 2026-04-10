"""Cumulative SourcererCC clustering across the longitudinal corpus (paper RQ5).

For each bug, collects every patch from every tool (including the baseline),
runs pairwise SourcererCC comparisons, and groups matching patches into
clusters via connected components. Each cluster is assigned a globally
unique integer id.

Output per bug: a DataFrame with columns ``uid``, ``tool``, ``bug_uid``,
``correctness``, ``cluster_id``.
"""

import os
import logging
import pandas as pd
import numpy as np
from collections import defaultdict
from itertools import combinations
from tqdm import tqdm

from utils.config import *
from utils.benchmark import *
from utils.utils import *
from utils.tool import *
from utils.dataset import *
from rq5_llms import *


# ---------------------------------------------------------------------------
# 1. Patch loading helpers
# ---------------------------------------------------------------------------

def load_tool_patches(tool_name):
    """
    Load and preprocess patches for a given tool through the standard pipeline.
    Returns a DataFrame with 'content' ready for comparison.
    """
    bugs, developer_patches, tool_patches = init(configure=False)

    df = pd.read_pickle(os.path.join(RQ5_META_DATA_DIR, f"{tool_name}_patches.pkl"))
    df = clean_and_save_patches(bugs, df, os.path.join(RQ5_META_DATA_DIR, f"{tool_name}_patches_cleaned.pkl"))
    df = get_methods_and_save(bugs, df, os.path.join(RQ5_META_DATA_DIR, f"{tool_name}_method_patches.pkl"))
    df = normalize_names_and_save(df, os.path.join(RQ5_META_DATA_DIR, f"{tool_name}_normalized_patches.pkl"))
    df = get_single_methods_and_save(df, os.path.join(RQ5_META_DATA_DIR, f"{tool_name}_single_hunk_patches.pkl"))
    df = deduplicate_patches_and_save(df, os.path.join(RQ5_META_DATA_DIR, f"{tool_name}_deduplicated_patches.pkl"))

    if 'content' not in df.columns:
        df['content'] = df.apply(get_single_hunk_method, axis=1)

    df['tool'] = tool_name
    return df


def load_baseline_patches():
    """
    Load the baseline (pre-existing) deduplicated tool patches and tag them
    as tool='baseline'.
    """
    df = pd.read_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)

    df = df[df['bug_uid'].str.startswith("defects4j")]

    if 'content' not in df.columns:
        df['content'] = df.apply(get_single_hunk_method, axis=1)

    df['tool'] = 'baseline'
    return df


# ---------------------------------------------------------------------------
# 2. Union-Find (Disjoint Set) for clustering
# ---------------------------------------------------------------------------

class UnionFind:
    """Weighted quick-union with path compression."""

    def __init__(self):
        self.parent = {}
        self.rank = {}

    def find(self, x):
        if x not in self.parent:
            self.parent[x] = x
            self.rank[x] = 0
        while self.parent[x] != x:
            self.parent[x] = self.parent[self.parent[x]]  # path compression
            x = self.parent[x]
        return x

    def union(self, a, b):
        ra, rb = self.find(a), self.find(b)
        if ra == rb:
            return
        if self.rank[ra] < self.rank[rb]:
            ra, rb = rb, ra
        self.parent[rb] = ra
        if self.rank[ra] == self.rank[rb]:
            self.rank[ra] += 1

    def connected(self, a, b):
        return self.find(a) == self.find(b)

    def clusters(self, keys):
        """Return dict: root -> set of keys."""
        groups = defaultdict(set)
        for k in keys:
            groups[self.find(k)].add(k)
        return groups


# ---------------------------------------------------------------------------
# 3. Core clustering logic
# ---------------------------------------------------------------------------

def cluster_patches_per_bug(all_patches_df):
    """
    For every bug_uid, compare all patches pairwise with SourcererCC and
    build clusters of matching patches via connected components.

    Args:
        all_patches_df: DataFrame indexed by uid with columns
            ['bug_uid', 'tool', 'correctness', 'content']

    Returns:
        DataFrame with columns:
            uid, bug_uid, tool, correctness, cluster_id
    """
    uf = UnionFind()

    # Deduplicate identical content within each bug to reduce comparisons.
    # Patches with identical (bug_uid, content) are trivially in the same cluster.
    # deduped = all_patches_df.drop_duplicates(subset=['bug_uid', 'content'], keep='first')
    deduped = all_patches_df.copy()  # Ensure we have a copy to avoid SettingWithCopyWarning
    logging.info(
        f"Deduplicated for comparison: {len(all_patches_df)} -> {len(deduped)} "
        f"(skipping {len(all_patches_df) - len(deduped)} identical-content patches)"
    )
    print(
        f"Deduplicated for comparison: {len(all_patches_df)} -> {len(deduped)} "
        f"(skipping {len(all_patches_df) - len(deduped)} identical-content patches)"
    )

    # --- build comparison list (only within same bug_uid) --------------------
    comparisons = []
    for bug_uid, group in deduped.groupby('bug_uid'):
        uids = list(group.index)
        if len(uids) < 2:
            continue
        for uid_a, uid_b in combinations(uids, 2):
            comparisons.append((uid_a, uid_b, bug_uid))

    total = len(comparisons)
    logging.info(f"Total pairwise comparisons: {total}")
    print(f"Total pairwise comparisons: {total}")

    # --- run SourcererCC comparisons -----------------------------------------
    for uid_a, uid_b, bug_uid in tqdm(comparisons, total=total, desc="Clustering patches"):
        content_a = deduped.loc[uid_a, 'content']
        content_b = deduped.loc[uid_b, 'content']

        try:
            is_clone = match_type2(content_a, content_b)
        except Exception as e:
            logging.error(f"Error comparing {uid_a} vs {uid_b} (bug {bug_uid}): {e}")
            is_clone = False

        if is_clone:
            logging.info(f"Match: {uid_a} <-> {uid_b} (bug {bug_uid})")
            print(f"Match: {uid_a} <-> {uid_b} (bug {bug_uid})")
            uf.union(uid_a, uid_b)

    # --- propagate clusters to duplicates that were removed ------------------
    # Build map: (bug_uid, content) -> representative uid kept after dedup
    content_to_rep_uid = {}
    for uid, row in deduped.iterrows():
        key = (row['bug_uid'], row['content'])
        if key not in content_to_rep_uid:
            content_to_rep_uid[key] = uid

    # Ensure every uid is registered in union-find (including singletons)
    for uid in all_patches_df.index:
        uf.find(uid)

    # Union duplicates with their representative
    for uid, row in all_patches_df.iterrows():
        key = (row['bug_uid'], row['content'])
        rep_uid = content_to_rep_uid.get(key)
        if rep_uid is not None and rep_uid != uid:
            uf.union(uid, rep_uid)

    # --- assign sequential cluster IDs ---------------------------------------
    all_uids = list(all_patches_df.index)
    raw_clusters = uf.clusters(all_uids)
    root_to_id = {root: cid for cid, root in enumerate(sorted(raw_clusters.keys()))}

    results = []
    for uid in all_uids:
        root = uf.find(uid)
        results.append({
            'uid': uid,
            'bug_uid': all_patches_df.loc[uid, 'bug_uid'],
            'tool': all_patches_df.loc[uid, 'tool'],
            'correctness': all_patches_df.loc[uid, 'correctness'],
            'cluster_id': root_to_id[root],
        })

    return pd.DataFrame(results)


# ---------------------------------------------------------------------------
# 4. Reporting helpers
# ---------------------------------------------------------------------------

def report_clusters(cluster_df):
    """Print summary statistics about the clustering results."""
    n_patches = len(cluster_df)
    n_clusters = cluster_df['cluster_id'].nunique()
    n_bugs = cluster_df['bug_uid'].nunique()
    n_tools = cluster_df['tool'].nunique()

    print("=" * 70)
    print("CLUSTERING SUMMARY")
    print(f"  Patches : {n_patches}")
    print(f"  Bugs    : {n_bugs}")
    print(f"  Tools   : {n_tools}  ({', '.join(sorted(cluster_df['tool'].unique()))})")
    print(f"  Clusters: {n_clusters}")
    print("=" * 70)

    # Cluster size distribution
    sizes = cluster_df.groupby('cluster_id').size()
    print(f"  Singleton clusters (size 1): {(sizes == 1).sum()}")
    print(f"  Multi-patch clusters       : {(sizes > 1).sum()}")
    print(f"  Largest cluster size       : {sizes.max()}")
    print()

    # Per-tool breakdown
    for tool in sorted(cluster_df['tool'].unique()):
        tool_df = cluster_df[cluster_df['tool'] == tool]
        n_correct = len(tool_df[tool_df['correctness'] == 'Correct'])
        n_overfit = len(tool_df[tool_df['correctness'] == 'Overfitting'])

        # How many of this tool's patches share a cluster with another tool?
        cross_tool = 0
        for _, row in tool_df.iterrows():
            cluster_tools = cluster_df[cluster_df['cluster_id'] == row['cluster_id']]['tool'].unique()
            if len(cluster_tools) > 1:
                cross_tool += 1

        print(f"  {tool:20s}  Correct={n_correct:4d}  Overfitting={n_overfit:4d}  "
              f"Cross-tool matches={cross_tool:4d}")
    print()


def report_cross_tool_matrix(cluster_df):
    """
    Print a tool x tool matrix showing how many patches from tool_A share
    a cluster with at least one patch from tool_B.
    """
    tools = sorted(cluster_df['tool'].unique())
    matrix = pd.DataFrame(0, index=tools, columns=tools)

    for cid, group in cluster_df.groupby('cluster_id'):
        tools_in_cluster = group['tool'].unique()
        if len(tools_in_cluster) < 2:
            continue
        for t1, t2 in combinations(tools_in_cluster, 2):
            n1 = len(group[group['tool'] == t1])
            n2 = len(group[group['tool'] == t2])
            matrix.loc[t1, t2] += n1
            matrix.loc[t2, t1] += n2

    print("CROSS-TOOL CLUSTER OVERLAP MATRIX")
    print("(cell = # patches from row-tool sharing a cluster with col-tool)")
    print(matrix.to_string())
    print()


# ---------------------------------------------------------------------------
# 5. Main
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)

    # ---- Configure your tool list here ----
    TOOL_NAMES = [
        "arjae",
        "recoder",
        "selfapr",
        "knod",
        "tare",
        "transplantfix",
        "t5apr",
    ]

    # ---- Load all patches (baseline is treated as just another tool) ----
    print("Loading baseline patches...")
    baseline_df = load_baseline_patches()
    print(f"  Baseline: {len(baseline_df)} patches")

    frames = [baseline_df]
    for tool_name in TOOL_NAMES:
        print(f"Loading {tool_name} patches...")
        tdf = load_tool_patches(tool_name)
        print(f"  {tool_name}: {len(tdf)} patches")
        frames.append(tdf)

    all_patches = pd.concat(frames)

    # Ensure 'content' is present
    if 'content' not in all_patches.columns:
        all_patches['content'] = all_patches.apply(get_single_hunk_method, axis=1)

    print(f"\nTotal patches (all tools + baseline): {len(all_patches)}")
    print(f"Unique bugs: {all_patches['bug_uid'].nunique()}")

    # ---- Cluster ----
    OUTPUT_DIR = os.path.join("rq5", "all_vs_all")
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    OUTPUT_PKL  = os.path.join(OUTPUT_DIR, "all_tools_clusters.pkl")
    OUTPUT_HTML = os.path.join(OUTPUT_DIR, "all_tools_clusters.html")
    OUTPUT_CSV  = os.path.join(OUTPUT_DIR, "all_tools_clusters.csv")

    if os.path.exists(OUTPUT_PKL):
        print(f"✅ Cluster results already exist at {OUTPUT_PKL}")
        cluster_df = pd.read_pickle(OUTPUT_PKL)
    else:
        cluster_df = cluster_patches_per_bug(all_patches)
        cluster_df.to_pickle(OUTPUT_PKL)
        cluster_df.to_html(OUTPUT_HTML)
        cluster_df.to_csv(OUTPUT_CSV, index=False)
        print(f"Saved: {OUTPUT_PKL}")
        print(f"Saved: {OUTPUT_HTML}")
        print(f"Saved: {OUTPUT_CSV}")

    # ---- Report ----
    report_clusters(cluster_df)
    report_cross_tool_matrix(cluster_df)