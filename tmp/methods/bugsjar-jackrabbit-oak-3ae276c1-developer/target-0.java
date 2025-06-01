    private void collectIndexEditors(NodeBuilder definitions,
            NodeState before) throws CommitFailedException {
        for (String name : definitions.getChildNodeNames()) {
            NodeBuilder definition = definitions.getChildNode(name);
            if (Objects.equal(async, definition.getString(ASYNC_PROPERTY_NAME))) {
                String type = definition.getString(TYPE_PROPERTY_NAME);
                boolean shouldReindex = shouldReindex(definition,
                        before, name);
                Editor editor = provider.getIndexEditor(type, definition, root, updateCallback);
                if (editor == null) {
                    // trigger reindexing when an indexer becomes available
                    definition.setProperty(REINDEX_PROPERTY_NAME, true);
                } else if (shouldReindex) {
                    if (definition.getBoolean(REINDEX_ASYNC_PROPERTY_NAME)
                            && definition.getString(ASYNC_PROPERTY_NAME) == null) {
                        // switch index to an async update mode
                        definition.setProperty(ASYNC_PROPERTY_NAME,
                                ASYNC_REINDEX_VALUE);
                    } else {
                        definition.setProperty(REINDEX_PROPERTY_NAME, false);
                        // as we don't know the index content node name
                        // beforehand, we'll remove all child nodes
                        for (String rm : definition.getChildNodeNames()) {
                            definition.getChildNode(rm).remove();
                        }
                        reindex.put(concat(getPath(), INDEX_DEFINITIONS_NAME, name), wrap(editor));
                    }
                } else {
                    editors.add(wrap(editor));
                }
            }
        }
    }
    private boolean shouldReindex(NodeBuilder definition, NodeState before,
            String name) {
        PropertyState ps = definition.getProperty(REINDEX_PROPERTY_NAME);
        if (ps != null && ps.getValue(BOOLEAN)) {
            return true;
        }
        // reindex in the case this is a new node, even though the reindex flag
        // might be set to 'false' (possible via content import)
        return !before.getChildNode(INDEX_DEFINITIONS_NAME).hasChildNode(name);
    }
    public void enter(NodeState before, NodeState after)
            throws CommitFailedException {
        collectIndexEditors(builder.getChildNode(INDEX_DEFINITIONS_NAME), before);

        // no-op when reindex is empty
        CommitFailedException exception = EditorDiff.process(
                CompositeEditor.compose(reindex.values()), MISSING_NODE, after);
        if (exception != null) {
            throw exception;
        }

        for (Editor editor : editors) {
            editor.enter(before, after);
        }
    }
