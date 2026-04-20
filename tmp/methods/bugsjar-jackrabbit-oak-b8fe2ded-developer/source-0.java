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
        if (lookup.isIndexed(filter.getPath())) {
            return lookup.getCost(filter);
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }
    public Cursor query(Filter filter, NodeState root) {
        NodeTypeIndexLookup lookup = new NodeTypeIndexLookup(root);
        if (!hasNodeTypeRestriction(filter) || !lookup.isIndexed(filter.getPath())) {
            throw new IllegalStateException(
                    "NodeType index is used even when no index is available for filter " + filter);
        }
        return Cursors.newPathCursorDistinct(lookup.query(filter));
    }
