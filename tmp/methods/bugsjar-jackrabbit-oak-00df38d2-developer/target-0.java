    public NodeBuilder child(String name) {
        read(); // shortcut when dealing with a read-only child node
        if (baseState != null
                && baseState.hasChildNode(name)
                && (writeState == null
                    || (writeState.base == baseState
                        && !writeState.nodes.containsKey(name)))) {
            return createChildBuilder(name);
        }

        // no read-only child node found, switch to write mode
        write();
        assert writeState != null; // guaranteed by write()

        NodeState childBase = null;
        if (baseState != null) {
            childBase = baseState.getChildNode(name);
        }

        if (writeState.nodes.get(name) == null) {
            if (writeState.nodes.containsKey(name)) {
                // The child node was removed earlier and we're creating
                // a new child with the same name. Use the null state to
                // prevent the previous child state from re-surfacing.
                childBase = null;
            }
            writeState.nodes.put(name, new MutableNodeState(childBase));
        }

        MemoryNodeBuilder builder = createChildBuilder(name);
        builder.write();
        return builder;
    }
