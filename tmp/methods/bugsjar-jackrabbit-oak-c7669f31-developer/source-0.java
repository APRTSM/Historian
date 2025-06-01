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
                        reindex.put(concat(getPath(), INDEX_DEFINITIONS_NAME, name), editor);
                    }
                } else {
                    editors.add(editor);
                }
            }
        }
    }
