    public List<IndexPlan> getPlans(Filter filter, List<OrderEntry> sortOrder, NodeState rootState) {
        Collection<String> indexPaths = new LuceneIndexLookup(rootState).collectIndexNodePaths(filter);
        List<IndexPlan> plans = Lists.newArrayListWithCapacity(indexPaths.size());
        for (String path : indexPaths) {
            IndexNode indexNode = null;
            try {
                indexNode = tracker.acquireIndexNode(path);

                if (indexNode != null) {
                    IndexPlan plan = new IndexPlanner(indexNode, path, filter, sortOrder).getPlan();
                    if (plan != null) {
                        plans.add(plan);
                    }
                }
            } finally {
                if (indexNode != null) {
                    indexNode.release();
                }
            }
        }
        return plans;
    }
