    public Cursor query(IndexPlan plan, NodeState root) {
        LOG.debug("query(IndexPlan, NodeState)");
        LOG.debug("query() - plan: {}", plan);
        LOG.debug("query() - rootState: {}", root);

        Filter filter = plan.getFilter();
        List<OrderEntry> sortOrder = plan.getSortOrder();
        String pathPrefix = plan.getPathPrefix();
        Iterable<String> paths = null;
        OrderedContentMirrorStoreStrategy strategy
                = OrderedPropertyIndexLookup.getStrategy(plan.getDefinition());
        int depth = 1;
        PropertyRestriction pr = plan.getPropertyRestriction();
        if (pr != null) {
            String propertyName = PathUtils.getName(pr.propertyName);
            depth = PathUtils.getDepth(propertyName);
            paths = strategy.query(plan.getFilter(), propertyName,
                    plan.getDefinition(), pr, pathPrefix);
        }
        if (paths == null && sortOrder != null && !sortOrder.isEmpty()) {
            // we could be here if we have a query where the ORDER BY makes us play it.
            for (OrderEntry oe : sortOrder) {
                String propertyName = PathUtils.getName(oe.getPropertyName());
                depth = PathUtils.getDepth(oe.getPropertyName());
                paths = strategy.query(plan.getFilter(), propertyName,
                        plan.getDefinition(), new PropertyRestriction(), pathPrefix);
            }
        }

        if (paths == null) {
            // if still here then something went wrong.
            throw new IllegalStateException(
                    "OrderedPropertyIndex index is used even when no index is available for filter "
                            + filter);
        }
        Cursor cursor = Cursors.newPathCursor(paths, filter.getQueryEngineSettings());
        if (depth > 1) {
            cursor = Cursors.newAncestorCursor(cursor, depth - 1, filter.getQueryEngineSettings());
        }
        return cursor;
    }
    public Iterable<String> query(Filter filter, String propertyName, PropertyRestriction pr) {
        NodeState indexMeta = getIndexNode(root, propertyName, filter);
        if (indexMeta == null) {
            throw new IllegalArgumentException("No index for " + propertyName);
        }
        return getStrategy(indexMeta).query(
                filter, propertyName, indexMeta, pr, "");
    }
    public double getCost(Filter filter, String propertyName, PropertyValue value) {
        NodeState indexMeta = getIndexNode(root, propertyName, filter);
        if (indexMeta == null) {
            return Double.POSITIVE_INFINITY;
        }
        return COST_OVERHEAD +
                getStrategy(indexMeta).count(filter, indexMeta, encode(value), MAX_COST);
    }
    PropertyIndexPlan(String name, NodeState definition, Filter filter) {
        this.name = name;
        this.definition = definition;
        this.properties = newHashSet(definition.getNames(PROPERTY_NAMES));

        if (definition.getBoolean(UNIQUE_PROPERTY_NAME)) {
            this.strategy = UNIQUE;
        } else {
            this.strategy = MIRROR;
        }

        this.filter = filter;

        Iterable<String> types = definition.getNames(DECLARING_NODE_TYPES);
        this.matchesAllTypes = isEmpty(types);
        this.matchesNodeTypes =
                matchesAllTypes || any(types, in(filter.getSupertypes()));

        double bestCost = Double.POSITIVE_INFINITY;
        Set<String> bestValues = emptySet();
        int bestDepth = 1;

        if (matchesNodeTypes) {
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
                    Set<String> values = getValues(restriction);
                    double cost = strategy.count(filter, definition, values, MAX_COST);
                    if (cost < bestCost) {
                        bestDepth = depth;
                        bestValues = values;
                        bestCost = cost;
                    }
                }
            }

            // OAK-1965: let's see if we can find a (x='...' OR y='...')
            // constraint where both x and y are covered by this index
            // TODO: avoid repeated scans through the constraints
            for (ConstraintImpl constraint
                    : filter.getSelector().getSelectorConstraints()) {
                if (constraint instanceof OrImpl) {
                    Set<String> values = findMultiProperty((OrImpl) constraint);
                    if (values != null) {
                        double cost = strategy.count(filter, definition, values, MAX_COST);
                        if (cost < bestCost) {
                            bestDepth = 1;
                            bestValues = values;
                            bestCost = cost;
                        }
                    }
                }
            }
        }

        this.depth = bestDepth;
        this.values = bestValues;
        this.cost = COST_OVERHEAD + bestCost;
    }
    private static Set<String> getValues(PropertyRestriction restriction) {
        if (restriction.firstIncluding
                && restriction.lastIncluding
                && restriction.first != null
                && restriction.first.equals(restriction.last)) {
            // "[property] = $value"
            return encode(restriction.first);
        } else if (restriction.list != null) {
            // "[property] IN (...)
            Set<String> values = newLinkedHashSet(); // keep order for testing
            for (PropertyValue value : restriction.list) {
                values.addAll(encode(value));
            }
            return values;
        } else {
            // processed as "[property] is not null"
            return null;
        }
    }
