    public Resolution addExistingProperty(NodeBuilder parent,
            PropertyState ours,
            PropertyState theirs) {
        if (isChildOrderProperty(ours)) {
            // two sessions concurrently called orderBefore() on a Tree
            // that was previously unordered.
            merge(parent, ours, theirs);
            return Resolution.MERGED;
        } else {
            return handler.addExistingProperty(parent, ours, theirs);
        }
    }
    private static void merge(NodeBuilder parent, PropertyState ours, PropertyState theirs) {
        Set<String> theirOrder = Sets.newHashSet(theirs.getValue(Type.NAMES));
        PropertyBuilder<String> merged = PropertyBuilder.array(Type.NAME).assignFrom(theirs);

        // Append child node names from ours that are not in theirs
        for (String ourChild : ours.getValue(Type.NAMES)) {
            if (!theirOrder.contains(ourChild)) {
                merged.addValue(ourChild);
            }
        }

        // Remove child node names of nodes that have been removed
        for (String child : merged.getValues()) {
            if (!parent.hasChildNode(child)) {
                merged.removeValue(child);
            }
        }

        parent.setProperty(merged.getPropertyState());
    }
