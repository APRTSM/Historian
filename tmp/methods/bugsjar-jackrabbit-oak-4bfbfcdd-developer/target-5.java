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
        private void fetchNextPossiblyDuplicate() {
            while (!nodeIterators.isEmpty()) {
                Iterator<? extends ChildNodeEntry> iterator = nodeIterators.getLast();
                if (iterator.hasNext()) {
                    ChildNodeEntry entry = iterator.next();

                    readCount++;
                    if (readCount % 1000 == 0) {
                        FilterIterators.checkReadLimit(readCount, maxMemoryEntries);
                        LOG.warn("Traversed " + readCount + " nodes using index " + indexName + " with filter " + filter);
                    }

                    NodeState node = entry.getNodeState();

                    String name = entry.getName();
                    if (NodeStateUtils.isHidden(name)) {
                        continue;
                    }
                    currentPath = PathUtils.concat(parentPath, name);

                    if (!"".equals(filterPath)) {
                        String p = currentPath;
                        if (pathContainsValue) {
                            String value = PathUtils.elements(p).iterator().next();
                            p = PathUtils.relativize(value, p);
                        }
                        if ("".equals(pathPrefix)) {
                            p = PathUtils.concat("/", p);
                        } else {
                            p = PathUtils.concat(pathPrefix, p);
                        }
                        if (!"".equals(p) &&
                                !p.equals(filterPath) &&
                                !PathUtils.isAncestor(p, filterPath) &&
                                !PathUtils.isAncestor(filterPath, p)) {
                            continue;
                        }
                    }

                    nodeIterators.addLast(node.getChildNodeEntries().iterator());
                    parentPath = currentPath;

                    if (node.getBoolean("match")) {
                        return;
                    }

                } else {
                    nodeIterators.removeLast();
                    parentPath = PathUtils.getParentPath(parentPath);
                }
            }
            currentPath = null;
            closed = true;
        }
    public long count(NodeState indexMeta, final String indexStorageNodeName,
            Set<String> values, int max) {
        return count(null, indexMeta, indexStorageNodeName, values, max);
    }
        PathIterator(Filter filter, String indexName, String pathPrefix) {
            this.filter = filter;
            this.pathPrefix = pathPrefix;
            this.indexName = indexName;
            boolean shouldDescendDirectly = filter.getPathRestriction().equals(Filter.PathRestriction.ALL_CHILDREN);
            if (shouldDescendDirectly) {
                filterPath = filter.getPath();
                if (PathUtils.denotesRoot(filterPath)) {
                    filterPath = "";
                }
            } else {
                filterPath = "";
            }
            parentPath = "";
            currentPath = "/";
            this.maxMemoryEntries = filter.getQueryEngineSettings().getLimitInMemory();
        }
        public String next() {
            if (closed) {
                throw new IllegalStateException("This iterator is closed");
            }
            if (!init) {
                fetchNext();
                init = true;
            }
            String result = PathUtils.concat(pathPrefix, currentPath);
            fetchNext();
            return result;
        }
    public Iterable<String> query(final Filter filter, final String indexName,
            final NodeState indexMeta, final String indexStorageNodeName,
            final Iterable<String> values) {
        final NodeState index = indexMeta.getChildNode(indexStorageNodeName);
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                PathIterator it = new PathIterator(filter, indexName, "");
                if (values == null) {
                    it.setPathContainsValue(true);
                    it.enqueue(getChildNodeEntries(index).iterator());
                } else {
                    for (String p : values) {
                        NodeState property = index.getChildNode(p);
                        if (property.exists()) {
                            // we have an entry for this value, so use it
                            it.enqueue(Iterators.singletonIterator(
                                    new MemoryChildNodeEntry("", property)));
                        }
                    }
                }
                return it;
            }
        };
    }
    public long count(final Filter filter, NodeState indexMeta, Set<String> values, int max) {
        return count(filter, indexMeta, INDEX_CONTENT_NODE_NAME, values, max);
    }
    public long count(Filter filter, NodeState indexMeta, final String indexStorageNodeName,
            Set<String> values, int max) {
        NodeState index = indexMeta.getChildNode(indexStorageNodeName);
        int count = 0;
        if (values == null) {
            PropertyState ec = indexMeta.getProperty(ENTRY_COUNT_PROPERTY_NAME);
            if (ec != null) {
                return ec.getValue(Type.LONG);
            }
            CountingNodeVisitor v = new CountingNodeVisitor(max);
            v.visit(index);
            count = v.getEstimatedCount();
            if (count >= max) {
                // "is not null" queries typically read more data
                count *= 10;
            }
        } else {
            int size = values.size();
            if (size == 0) {
                return 0;
            }
            PropertyState ec = indexMeta.getProperty(ENTRY_COUNT_PROPERTY_NAME);       
            if (ec != null) {
                long entryCount = ec.getValue(Type.LONG);
                // assume 10000 entries per key, so that this index is used
                // instead of traversal, but not instead of a regular property index
                long keyCount = entryCount / 10000;
                ec = indexMeta.getProperty(KEY_COUNT_PROPERTY_NAME);
                if (ec != null) {
                    keyCount = ec.getValue(Type.LONG);
                }
                // cast to double to avoid overflow 
                // (entryCount could be Long.MAX_VALUE)
                // the cost is not multiplied by the size, 
                // otherwise the traversing index might be used
                return (long) ((double) entryCount / keyCount) + size;
            }
            max = Math.max(10, max / size);
            int i = 0;
            String filterRootPath = null;
            if (filter != null &&
                    filter.getPathRestriction().equals(Filter.PathRestriction.ALL_CHILDREN)) {
                filterRootPath = filter.getPath();
            }
            for (String p : values) {
                if (count > max && i > 3) {
                    // the total count is extrapolated from the the number 
                    // of values counted so far to the total number of values
                    count = count * size / i;
                    break;
                }
                NodeState s = index.getChildNode(p);
                if (filterRootPath != null && s.exists()) {
                    // Descend directly to path restriction inside index tree
                    for (String pathFragment : PathUtils
                            .elements(filterRootPath)) {
                        s = s.getChildNode(pathFragment);
                        if (!s.exists()) {
                            break;
                        }
                    }
                }
                if (s.exists()) {
                    CountingNodeVisitor v = new CountingNodeVisitor(max);
                    v.visit(s);
                    count += v.getEstimatedCount();
                }
                i++;
            }
        }
        return count;
    }
    long count(Filter filter, NodeState indexMeta, Set<String> values, int max);
