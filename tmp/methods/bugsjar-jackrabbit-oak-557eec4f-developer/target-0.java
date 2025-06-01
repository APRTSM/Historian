    public double getCost(Filter filter, NodeState root) {
        if (filter.getFullTextConstraint() != null) {
            // not an appropriate index for full-text search
            return Double.POSITIVE_INFINITY;
        }
        if (filter.containsNativeConstraint()) {
            // not an appropriate index for native search
            return Double.POSITIVE_INFINITY;
        }
        if (filter.getPropertyRestrictions().isEmpty()) {
            // not an appropriate index for no property restrictions & selector constraints
            return Double.POSITIVE_INFINITY;
        }

        PropertyIndexPlan plan = getPlan(root, filter);
        if (plan != null) {
            return plan.getCost();
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }
