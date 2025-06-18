import re
from collections import defaultdict, deque
import pandas as pd
from utils.config import *
from utils.benchmark import *
from utils.utils import *
from utils.tool import *
from utils.dataset import *

def detect_exact_matches(pairs, tool_patche):
    tool_patches['content'] = tool_patches['target_methods'].apply(lambda x: read_file(x[0]))

    for index, row in pairs.iterrows():
        uid = row['uid']
        groundtruth_index = row['groundtruth_index']
        
        # Get the content of the target method
        target_method_content = tool_patches.at[uid, 'content']
        target_method_groundtruth_content = tool_patches.at[groundtruth_index, 'content']

        if target_method_content == target_method_groundtruth_content:
            # If the content matches, assign 'Type-1' to expert_label
            pairs.at[index, 'expert_label'] = 'Exact'

    return pairs
    
def assign_type_1_spacing(pairs, tool_patches):
    tool_patches['content'] = tool_patches['target_methods'].apply(lambda x: read_file(x[0]))

    for index, row in pairs.iterrows():
        uid = row['uid']
        groundtruth_index = row['groundtruth_index']
        
        # Get the content of the target method
        target_method_content = tool_patches.at[uid, 'content']
        target_method_groundtruth_content = tool_patches.at[groundtruth_index, 'content']

        
        # Remove whitespaces tabs and newlines
        target_method_content = re.sub(r'\s+', '', target_method_content)
        target_method_groundtruth_content = re.sub(r'\s+', '', target_method_groundtruth_content)

        if target_method_content == target_method_groundtruth_content:
            # If the content matches, assign 'Type-1' to expert_label
            pairs.at[index, 'expert_label'] = 'Type-1'

    return pairs
    
def select_representatives_and_drop(patches, pairs, label):
    """
    Select representative patches from connected components based on pairs with specified label.
    
    Args:
        patches: DataFrame with patches (uid as index or column)
        pairs: DataFrame with columns ['uid', 'groundtruth_index', 'expert_label']
        label: The label to use for grouping (e.g., "Exact Match", "Match Degree 1")
    
    Returns:
        patches_after_dropping: DataFrame with only representative patches
        dropped_patches: DataFrame with patches that were dropped (includes 'representative_id' column)
        representative_summary: DataFrame with columns ['representative_id', 'group_size', 'bug_uid']
    """
    import pandas as pd
    from collections import defaultdict, deque
    
    # Filter pairs to only those with the specified label
    relevant_pairs = pairs[pairs['expert_label'] == label].copy()
    
    if relevant_pairs.empty:
        # No pairs with the specified label, return original patches
        return patches.copy(), pd.DataFrame()
    
    # Build adjacency list for the graph
    graph = defaultdict(set)
    all_nodes = set()
    
    for _, row in relevant_pairs.iterrows():
        uid1 = row['uid']
        uid2 = row['groundtruth_index']
        graph[uid1].add(uid2)
        graph[uid2].add(uid1)
        all_nodes.add(uid1)
        all_nodes.add(uid2)
    
    # Find connected components using BFS
    visited = set()
    connected_components = []
    
    for node in all_nodes:
        if node not in visited:
            # BFS to find all nodes in this connected component
            component = []
            queue = deque([node])
            visited.add(node)
            
            while queue:
                current = queue.popleft()
                component.append(current)
                
                for neighbor in graph[current]:
                    if neighbor not in visited:
                        visited.add(neighbor)
                        queue.append(neighbor)
            
            connected_components.append(component)
    
    # Select representatives and create mapping
    representatives = set()
    to_drop = set()
    representative_mapping = {}  # Maps dropped patch uid -> representative uid
    
    for component in connected_components:
        if len(component) > 1:
            # Keep the first one as representative
            representative = component[0]
            representatives.add(representative)
            # Mark the rest for dropping and map them to representative
            for patch_to_drop in component[1:]:
                to_drop.add(patch_to_drop)
                representative_mapping[patch_to_drop] = representative
        else:
            # Single node component, keep it
            representatives.add(component[0])
    
    # Determine which patches to keep and which to drop
    # Assuming patches has either 'uid' column or uid as index
    if 'uid' in patches.columns:
        patch_ids = patches['uid']
        patch_id_col = 'uid'
    else:
        # Assume uid is the index
        patch_ids = patches.index
        patch_id_col = patches.index.name if patches.index.name else 'index'
    
    # Create masks for keeping and dropping
    keep_mask = ~patch_ids.isin(to_drop)
    drop_mask = patch_ids.isin(to_drop)
    
    patches_after_dropping = patches[keep_mask].copy()
    dropped_patches = patches[drop_mask].copy()
    
    # Add representative_id column to dropped_patches
    if 'uid' in patches.columns:
        dropped_patches['representative_id'] = dropped_patches['uid'].map(representative_mapping)
    else:
        dropped_patches['representative_id'] = dropped_patches.index.map(representative_mapping)
    
    # Create summary of representatives and their group sizes
    representative_counts = {}
    for component in connected_components:
        representative = component[0]  # First element is the representative
        representative_counts[representative] = len(component)  # Total count including representative
    
    # Also include patches that weren't in any pairs with the specified label
    # These are implicitly representatives of size 1
    if 'uid' in patches.columns:
        all_patch_ids = set(patches['uid'])
    else:
        all_patch_ids = set(patches.index)
    
    # Find patches that weren't in any relevant pairs (size 1 groups)
    patches_in_pairs = all_nodes  # patches that appeared in pairs with specified label
    isolated_patches = all_patch_ids - patches_in_pairs
    
    # Add isolated patches as representatives of size 1
    for patch_id in isolated_patches:
        representative_counts[patch_id] = 1
    
    # Create DataFrame for representative summary
    if representative_counts:
        # Get bug_uid for each representative
        if 'uid' in patches.columns:
            # Create mapping from uid to bug_uid
            uid_to_bug_uid = dict(zip(patches['uid'], patches['bug_uid']))
            representative_data = [
                {
                    'representative_id': rep_id, 
                    'group_size': count,
                    'bug_uid': uid_to_bug_uid.get(rep_id, None)
                }
                for rep_id, count in representative_counts.items()
            ]
        else:
            # patches has uid as index
            representative_data = [
                {
                    'representative_id': rep_id, 
                    'group_size': count,
                    'bug_uid': patches.loc[rep_id, 'bug_uid'] if rep_id in patches.index else None
                }
                for rep_id, count in representative_counts.items()
            ]
        
        representative_summary = pd.DataFrame(representative_data)
    else:
        representative_summary = pd.DataFrame(columns=['representative_id', 'group_size', 'bug_uid'])
    
    return patches_after_dropping, dropped_patches, representative_summary

def propagate_labels_to_original_pairs(new_pairs, dropped_patches, old_pairs):
    """
    Propagate labels from new_pairs back to old_pairs using representative mapping.
    
    Args:
        new_pairs: DataFrame with pairs from reduced patch set (with new labels)
        dropped_patches: DataFrame with 'representative_id' column mapping dropped patches to representatives
        old_pairs: DataFrame with original pairs (to be updated with propagated labels)
    
    Returns:
        old_pairs: Updated DataFrame with propagated labels
    """
    
    # Create mapping from dropped patch to its representative
    if dropped_patches.empty:
        # No dropped patches, just return old_pairs as is
        return old_pairs.copy()
    
    # Create representative mapping
    if 'uid' in dropped_patches.columns:
        representative_map = dict(zip(dropped_patches['uid'], dropped_patches['representative_id']))
    else:
        representative_map = dict(zip(dropped_patches.index, dropped_patches['representative_id']))
    
    # Create a copy of old_pairs to modify
    updated_old_pairs = old_pairs.copy()
    
    # For each pair in new_pairs that has a non-default label
    for _, new_row in new_pairs.iterrows():
        new_uid1 = new_row['uid']
        new_uid2 = new_row['groundtruth_index']
        new_label = new_row['expert_label']
        
        # Skip if this is a default/empty label (like '-')
        if pd.isna(new_label) or new_label == '-' or new_label == '':
            continue
        
        # Find all patches that have this representative (including the representative itself)
        patches_for_uid1 = [new_uid1]  # Representative itself
        patches_for_uid2 = [new_uid2]  # Representative itself
        
        # Add all dropped patches that have these as representatives
        for dropped_patch, representative in representative_map.items():
            if representative == new_uid1:
                patches_for_uid1.append(dropped_patch)
            if representative == new_uid2:
                patches_for_uid2.append(dropped_patch)
        
        # Now propagate the label to all combinations in old_pairs
        for patch1 in patches_for_uid1:
            for patch2 in patches_for_uid2:
                if patch1 == patch2:
                    continue  # Skip self-pairs
                
                # Find this pair in old_pairs (check both directions)
                mask1 = (updated_old_pairs['uid'] == patch1) & (updated_old_pairs['groundtruth_index'] == patch2)
                mask2 = (updated_old_pairs['uid'] == patch2) & (updated_old_pairs['groundtruth_index'] == patch1)
                
                # Update the label for this pair only if it currently has no label ("-")
                if mask1.any():
                    # Only update if current label is "-"
                    current_labels = updated_old_pairs.loc[mask1, 'expert_label']
                    update_mask = (current_labels == '-') | pd.isna(current_labels)
                    if update_mask.any():
                        updated_old_pairs.loc[mask1 & update_mask, 'expert_label'] = new_label
                
                if mask2.any():
                    # Only update if current label is "-"
                    current_labels = updated_old_pairs.loc[mask2, 'expert_label']
                    update_mask = (current_labels == '-') | pd.isna(current_labels)
                    if update_mask.any():
                        updated_old_pairs.loc[mask2 & update_mask, 'expert_label'] = new_label
    
    return updated_old_pairs


if __name__ == "__main__":
    # Get the tool patches and developer patches (Numbers match with previous versions if patch matches are considered)
    tool_patches = pd.read_pickle(TMP_DEDUPLICATED_TOOL_PATHCES_PKL)
    developer_patches = pd.read_pickle(TMP_GENERATOR_NORMALIZED_DEVELOPER_PATHCES_PKL)


    # Select only the correct tool patches for RQ1
    correct_tool_patche = tool_patches[tool_patches["correctness"] == "Correct"].copy()
    print(f"Number of Correct tool patches: {len(correct_tool_patche)}")

    # Remove developer identical-1 patches (cleaned) (method match)
    patches = remove_developer_identical_patches(correct_tool_patche, developer_patches)
    print(f"Number of Correct-non-developer-identical tool patches: {len(patches)}")

    """ Start Experiments Exact Match """

    # Detect exact matches and assign labels using target methods
    pairs = get_pairs(patches)
    print(f"Number of pairs: {len(pairs)}")
    pairs = detect_exact_matches(pairs, patches)
    patches_kept, dropped, cluster_sizes = select_representatives_and_drop(patches, pairs, "Exact")
    print(f"Number of patches after dropping exact matches: {len(patches_kept)}")
    print(f"Number of dropped patches: {len(dropped)}")


    # pairs = get_pairs(patches_kept)
    # print(f"Number of pairs after dropping exact matches: {len(pairs)}")

    # updated_original_pairs = propagate_labels_to_original_pairs(new_pairs, dropped, original_pairs)
    # print(f"Labels propagated to original {len(updated_original_pairs)} pairs")


    pairs to html and labels

    """ Type-1 Spacing """

    # # Detect space removed matches and assign labels using target methods (To ease Manual)
    pairs = get_pairs(patches_kept)
    print(f"Number of pairs after dropping exact matches: {len(pairs)}")
    pairs = assign_type_1_spacing(pairs, patches_kept)
    patches_kept, dropped, cluster_sizes = select_representatives_and_drop(patches_kept, pairs, "Type-1")
    print(f"Number of patches after dropping Type-1 matches: {len(patches_kept)}")
    print(f"Number of dropped patches: {len(dropped)}")

    """ Type-1/2 Manual """
    pairs = get_pairs(patches_kept)
    print(f"Number of pairs after dropping Type-1 matches: {len(pairs)}")