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
    PropertyIndexPlan(String name, NodeState root, NodeState definition, Filter filter) {
        this.name = name;
        this.definition = definition;
        this.properties = newHashSet(definition.getNames(PROPERTY_NAMES));
        pathFilter = PathFilter.from(definition.builder());

        if (definition.getBoolean(UNIQUE_PROPERTY_NAME)) {
            this.strategy = UNIQUE;
        } else {
            this.strategy = MIRROR;
        }

        this.filter = filter;

        Iterable<String> types = definition.getNames(DECLARING_NODE_TYPES);
        // if there is no such property, then all nodetypes are matched
        this.matchesAllTypes = !definition.hasProperty(DECLARING_NODE_TYPES);
        this.matchesNodeTypes =
                matchesAllTypes || any(types, in(filter.getSupertypes()));

        double bestCost = Double.POSITIVE_INFINITY;
        Set<String> bestValues = emptySet();
        int bestDepth = 1;

        if (matchesNodeTypes && 
                pathFilter.areAllDescendantsIncluded(filter.getPath())) {
            for (String property : properties) {
                PropertyRestriction restriction =
                        filter.getPropertyRestriction(property);
                int depth = 1;

                if (restriction == null) {
                    // no direct restriction, try one with a relative path
                    // TODO: avoid repeated scans through the restrictions
                    String suffix = "/" + property;
                    for (PropertyRestriction relative
                            : filter.getPropertyRestrictions()) {
                        if (relative.propertyName.endsWith(suffix)) {
                            restriction = relative;
                            depth = PathUtils.getDepth(relative.propertyName);
                        }
                    }
                }

                if (restriction != null) {
                    if (restriction.isNullRestriction()) {
                        // covering indexes are not currently supported
                        continue;
                    }
                    if (depth != 1 && !matchesAllTypes) {
                        // OAK-3589
                        // index has a nodetype condition, and the property condition is
                        // relative: can not use this index, as we don't know the nodetype
                        // of the child node (well, we could, for some node types)
                        continue;
                    }
                    Set<String> values = getValues(restriction);
                    double cost = strategy.count(filter, root, definition, values, MAX_COST);
                    if (cost < bestCost) {
                        bestDepth = depth;
                        bestValues = values;
                        bestCost = cost;
                    }
                }
            }
        }

        this.depth = bestDepth;
        this.values = bestValues;
        this.cost = COST_OVERHEAD + bestCost;
    }
