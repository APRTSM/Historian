    public double getCost(Filter filter, NodeState root) {
        // TODO don't call getCost for such queries
        if (filter.getFullTextConstraint() != null) {
            // not an appropriate index for full-text search
            return Double.POSITIVE_INFINITY;
        }
        if (!hasNodeTypeRestriction(filter)) {
            // this is not an appropriate index if the filter
            // doesn't have a node type restriction
            return Double.POSITIVE_INFINITY;
        }
        NodeTypeIndexLookup lookup = new NodeTypeIndexLookup(root);
        if (lookup.isIndexed(filter.getPath(), filter)) {
            return lookup.getCost(filter);
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }
    public Cursor query(Filter filter, NodeState root) {
        NodeTypeIndexLookup lookup = new NodeTypeIndexLookup(root);
        if (!hasNodeTypeRestriction(filter) || !lookup.isIndexed(filter.getPath(), filter)) {
            throw new IllegalStateException(
                    "NodeType index is used even when no index is available for filter " + filter);
        }
        return Cursors.newPathCursorDistinct(lookup.query(filter));
    }
    public double getCost(Filter filter) {
        PropertyIndexLookup lookup = new PropertyIndexLookup(root);
        return lookup.getCost(filter, JCR_PRIMARYTYPE, newName(filter.getPrimaryTypes()))
                + lookup.getCost(filter, JCR_MIXINTYPES, newName(filter.getMixinTypes()));
    }
    public boolean isIndexed(String path, Filter f) {
        PropertyIndexLookup lookup = new PropertyIndexLookup(root);
        if (lookup.isIndexed(JCR_PRIMARYTYPE, path, f)
                && lookup.isIndexed(JCR_MIXINTYPES, path, f)) {
            return true;
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        int slash = path.indexOf('/');
        if (slash == -1) {
            return false;
        }

        NodeState child = root.getChildNode(path.substring(0, slash));
        return new NodeTypeIndexLookup(child).isIndexed(
                path.substring(slash), f);
    }
