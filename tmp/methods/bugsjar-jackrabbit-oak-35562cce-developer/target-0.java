    public double getCost(Filter filter, NodeState root) {
        if (filter.getFullTextConstraint() != null) {
            // not an appropriate index for full-text search
            return Double.POSITIVE_INFINITY;
        }
        if (filter.containsNativeConstraint()) {
            // not an appropriate index for native search
            return Double.POSITIVE_INFINITY;
        }

        PropertyIndexLookup lookup = getLookup(root);
        Cheapest cheapest = findCheapestProperty(filter, lookup);
        LOG.debug("Cheapest property cost is {} for property {}", cheapest.cost, cheapest.propertyRestriction != null ? cheapest.propertyRestriction.propertyName : null);
        return cheapest.cost;
    }
    public Cursor query(Filter filter, NodeState root) {
        Iterable<String> paths = null;

        PropertyIndexLookup lookup = getLookup(root);
        int depth = 1;

        Cheapest cheapest = findCheapestProperty(filter, lookup);
        PropertyRestriction pr = cheapest.propertyRestriction;

        if (pr != null) {
            String propertyName = PathUtils.getName(pr.propertyName);
            depth = PathUtils.getDepth(pr.propertyName);
            // TODO support indexes on a path
            // currently, only indexes on the root node are supported
            if (lookup.isIndexed(propertyName, "/", filter)) {
                // equality
                if (pr.firstIncluding && pr.lastIncluding
                    && pr.first != null && pr.first.equals(pr.last)) {
                    // "[property] = $value"
                    paths = lookup.query(filter, propertyName, pr.first);
                } else if (pr.list != null) {
                    for (PropertyValue pv : pr.list) {
                        Iterable<String> p = lookup.query(filter, propertyName, pv);
                        if (paths == null) {
                            paths = p;
                        } else {
                            paths = Iterables.concat(paths, p);
                        }
                    }
                } else {
                    // processed as "[property] is not null"
                    paths = lookup.query(filter, propertyName, null);
                }
            }
        }
        if (paths == null) {
            throw new IllegalStateException("Property index is used even when no index is available for filter " + filter);
        }
        Cursor c = Cursors.newPathCursor(paths, filter.getQueryEngineSettings());
        if (depth > 1) {
            c = Cursors.newAncestorCursor(c, depth - 1, filter.getQueryEngineSettings());
        }
        return c;
    }
    public String getPlan(Filter filter, NodeState root) {
        StringBuilder buff = new StringBuilder("property");
        StringBuilder notIndexed = new StringBuilder();
        PropertyIndexLookup lookup = getLookup(root);
        Cheapest cheapest = findCheapestProperty(filter, lookup);
        PropertyRestriction pr = cheapest.propertyRestriction;

        if (pr != null) {
            String propertyName = PathUtils.getName(pr.propertyName);
            // TODO support indexes on a path
            // currently, only indexes on the root node are supported
            if (lookup.isIndexed(propertyName, "/", filter)) {
                if (pr.firstIncluding && pr.lastIncluding
                    && pr.first != null && pr.first.equals(pr.last)) {
                    buff.append(' ').append(propertyName).append('=').append(pr.first);
                } else {
                    buff.append(' ').append(propertyName);
                }
            } else if (pr.list != null) {
                buff.append(' ').append(propertyName).append(" IN(");
                int i = 0;
                for (PropertyValue pv : pr.list) {
                    if (i++ > 0) {
                        buff.append(", ");
                    }
                    buff.append(pv);
                }
                buff.append(')');
            } else {
                notIndexed.append(' ').append(propertyName);
                if (!pr.toString().isEmpty()) {
                    notIndexed.append(':').append(pr);
                }
            }
        }
        if (notIndexed.length() > 0) {
            buff.append(" (").append(notIndexed.toString().trim()).append(")");
        }
        return buff.toString();
    }
    private Cheapest findCheapestProperty(Filter filter, PropertyIndexLookup lookup) {
        Cheapest cost = new Cheapest();
        for (PropertyRestriction pr : filter.getPropertyRestrictions()) {
            String propertyName = PathUtils.getName(pr.propertyName);
            double propertyCost = Double.POSITIVE_INFINITY;
            // TODO support indexes on a path
            // currently, only indexes on the root node are supported
            if (lookup.isIndexed(propertyName, "/", filter)) {
                if (pr.firstIncluding && pr.lastIncluding
                    && pr.first != null && pr.first.equals(pr.last)) {
                    // "[property] = $value"
                    propertyCost = lookup.getCost(filter, propertyName, pr.first);
                } else if (pr.list != null) {
                    propertyCost = 0;
                    for (PropertyValue p : pr.list) {
                        propertyCost += lookup.getCost(filter, propertyName, p);
                    }
                } else {
                    // processed as "[property] is not null"
                    propertyCost = lookup.getCost(filter, propertyName, null);
                }
            }
            LOG.debug("property cost for {} is {}", propertyName, propertyCost);
            if (propertyCost < cost.cost) {
                cost.cost = propertyCost;
                cost.propertyRestriction = pr;
            }
        }
        return cost;
    }
