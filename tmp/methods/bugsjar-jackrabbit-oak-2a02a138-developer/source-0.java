    Map<String, String> convertNodeState(NodeState state) {
        Map<String, String> result = Maps.newHashMap();
        for (PropertyState ps : state.getProperties()) {
            String name = ps.getName();
            if (ps.getType() != Type.BINARY
                    && !ps.isArray()
                    && !IGNORE_PROP_NAMES.contains(name)) {
                result.put(name, ps.getValue(Type.STRING));
            }
        }
        result.put(LuceneIndexConstants.ANL_LUCENE_MATCH_VERSION, getVersion(state).toString());
        return result;
    }
