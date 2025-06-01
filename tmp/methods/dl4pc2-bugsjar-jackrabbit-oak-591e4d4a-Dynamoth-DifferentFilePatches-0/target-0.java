    public static void createIndexDefinition(@Nonnull NodeBuilder indexNode, 
                                             @Nonnull String indexDefName, 
                                             boolean unique, 
                                             @Nonnull Iterable<String> propertyNames, 
                                             @Nullable String[] declaringNodeTypeNames, 
                                             @Nonnull String propertyIndexType,
                                             Map<String, String> properties) throws RepositoryException {

        NodeBuilder entry = indexNode.child(indexDefName)
            .setProperty(JCR_PRIMARYTYPE, INDEX_DEFINITIONS_NODE_TYPE, NAME)
            .setProperty(TYPE_PROPERTY_NAME, propertyIndexType)
            .setProperty(REINDEX_PROPERTY_NAME, false);
        if (unique) {
            entry.setProperty(UNIQUE_PROPERTY_NAME, unique);
        }
        entry.setProperty(PropertyStates.createProperty(PROPERTY_NAMES, propertyNames, NAMES));
        if (declaringNodeTypeNames != null && declaringNodeTypeNames.length > 0) {
            entry.setProperty(PropertyStates.createProperty(DECLARING_NODE_TYPES,
                declaringNodeTypeNames, NAMES));
        }
        // additional properties
        if (properties != null) {
            for (String k : properties.keySet()) {
                if (false) {
                    entry.setProperty(k, properties.get(k));
                }
            }
        }
    }
