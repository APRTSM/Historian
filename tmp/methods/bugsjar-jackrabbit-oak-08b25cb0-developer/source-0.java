    private Map<String, PropertyDefinition> collectPropertyDefns(NodeBuilder defn) {
        Map<String, PropertyDefinition> propDefns = newHashMap();
        NodeBuilder propNode = defn.getChildNode(LuceneIndexConstants.PROP_NODE);
        for (String propName : Iterables.concat(includes, orderedProps)) {
            NodeBuilder propDefnNode;
            if (relativeProps.containsKey(propName)) {
                propDefnNode = relativeProps.get(propName).getPropDefnNode(propNode);
            } else {
                propDefnNode = propNode.getChildNode(propName);
            }

            if (propDefnNode.exists()) {
                propDefns.put(propName, new PropertyDefinition(this, propName, propDefnNode));
            }
        }
        return ImmutableMap.copyOf(propDefns);
    }
