    private Map<String, PropertyDefinition> collectPropertyDefns(NodeBuilder defn) {
        Map<String, PropertyDefinition> propDefns = newHashMap();
        NodeBuilder propNode = defn.getChildNode(LuceneIndexConstants.PROP_NODE);
        //Include all immediate child nodes to 'properties' node by default
        for (String propName : Iterables.concat(includes, orderedProps, propNode.getChildNodeNames())) {
            NodeBuilder propDefnNode;
            if (relativeProps.containsKey(propName)) {
                propDefnNode = relativeProps.get(propName).getPropDefnNode(propNode);
            } else {
                propDefnNode = propNode.getChildNode(propName);
            }

            if (propDefnNode.exists() && !propDefns.containsKey(propName)) {
                propDefns.put(propName, new PropertyDefinition(this, propName, propDefnNode));
            }
        }
        return ImmutableMap.copyOf(propDefns);
    }
