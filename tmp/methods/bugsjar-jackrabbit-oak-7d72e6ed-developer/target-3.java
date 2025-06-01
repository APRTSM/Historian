    private String getPlan() {
        return source.getPlan(rootState);
    }
    Iterator<ResultRowImpl> getRows() {
        prepare();
        Iterator<ResultRowImpl> it;
        if (explain) {
            String plan = getPlan();
            columns = new ColumnImpl[] { new ColumnImpl("explain", "plan", "plan")};
            ResultRowImpl r = new ResultRowImpl(this,
                    new String[0], 
                    new PropertyValue[] { PropertyValues.newString(plan)},
                    null);
            it = Arrays.asList(r).iterator();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("plan: " + getPlan());
            }
            if (orderings == null) {
                // can apply limit and offset directly
                it = new RowIterator(rootState, limit, offset);
            } else {
                // read and order first; skip and limit afterwards
                it = new RowIterator(rootState, Long.MAX_VALUE, 0);
            }
            long readCount = 0;
            if (orderings != null) {
                // TODO "order by" is not necessary if the used index returns
                // rows in the same order
                    
                // avoid overflow (both offset and limit could be Long.MAX_VALUE)
                int keep = (int) Math.min(Integer.MAX_VALUE, 
                        Math.min(Integer.MAX_VALUE, offset) + 
                        Math.min(Integer.MAX_VALUE, limit));
                
                ArrayList<ResultRowImpl> list = new ArrayList<ResultRowImpl>();
                while (it.hasNext()) {
                    readCount++;
                    ResultRowImpl r = it.next();
                    list.add(r);
                    // from time to time, sort and truncate
                    // this should results in O(n*log(2*keep)) operations,
                    // which is close to the optimum O(n*log(keep))
                    if (list.size() > keep * 2) {
                        // remove tail entries right now, to save memory
                        Collections.sort(list);
                        keepFirst(list, keep);
                    }
                }
                Collections.sort(list);
                keepFirst(list, keep);
                
                it = list.iterator();
                // skip the head (this is more efficient than removing
                // if there are many entries)
                for (int i = 0; i < offset && it.hasNext(); i++) {
                    it.next();
                }
                size = list.size() - offset;
            } else if (measure) {
                while (it.hasNext()) {
                    readCount++;
                    it.next();
                }
            }
            if (measure) {
                columns = new ColumnImpl[] {
                        new ColumnImpl("measure", "selector", "selector"),
                        new ColumnImpl("measure", "scanCount", "scanCount")
                };
                ArrayList<ResultRowImpl> list = new ArrayList<ResultRowImpl>();
                ResultRowImpl r = new ResultRowImpl(this,
                        new String[0],
                        new PropertyValue[] {
                                PropertyValues.newString("query"),
                                PropertyValues.newLong(readCount)
                            },
                        null);
                list.add(r);
                for (SelectorImpl selector : selectors) {
                    r = new ResultRowImpl(this,
                            new String[0],
                            new PropertyValue[] {
                                    PropertyValues.newString(selector.getSelectorName()),
                                    PropertyValues.newLong(selector.getScanCount()),
                                },
                            null);
                    list.add(r);
                }
                it = list.iterator();
            }
        }
        return it;
    }
    public QueryIndex getBestIndex(Query query, NodeState rootState, Filter filter) {
        QueryIndex best = null;
        if (LOG.isDebugEnabled()) {
            LOG.debug("cost using filter " + filter);
        }
        double bestCost = Double.POSITIVE_INFINITY;
        for (QueryIndex index : getIndexes(rootState)) {
            double cost = index.getCost(filter, rootState);
            if (LOG.isDebugEnabled()) {
                LOG.debug("cost for " + index.getIndexName() + " is " + cost);
            }
            if (cost < bestCost) {
                bestCost = cost;
                best = index;
            }
        }
        QueryIndex index = new TraversingIndex();
        double cost = index.getCost(filter, rootState);
        if (LOG.isDebugEnabled()) {
            LOG.debug("cost for " + index.getIndexName() + " is " + cost);
        }
        if (cost < bestCost) {
            bestCost = cost;
            best = index;
        }
        return best;
    }
    public void restrict(FilterImpl f) {
        if (f.getSelector() == parentSelector) {
            String c = childSelector.currentPath();
            if (c == null && f.isPreparing() && childSelector.isPrepared()) {
                // during the prepare phase, if the selector is already
                // prepared, then we would know the value
                c = KNOWN_PATH;
            }
            if (c != null) {
                f.restrictPath(PathUtils.getParentPath(c), Filter.PathRestriction.EXACT);
            }
        }
        if (f.getSelector() == childSelector) {
            String p = parentSelector.currentPath();
            if (p == null && f.isPreparing() && parentSelector.isPrepared()) {
                // during the prepare phase, if the selector is already
                // prepared, then we would know the value
                p = KNOWN_PATH;
            }
            if (p != null) {
                f.restrictPath(p, Filter.PathRestriction.DIRECT_CHILDREN);
            }
        }
    }
    public void restrict(FilterImpl f) {
        if (f.getSelector() == ancestorSelector) {
            String d = descendantSelector.currentPath();
            if (d == null && f.isPreparing() && descendantSelector.isPrepared()) {
                // during the prepare phase, if the selector is already
                // prepared, then we would know the value
                d = KNOWN_PATH;
            }
            if (d != null) {
                f.restrictPath(PathUtils.getParentPath(d), Filter.PathRestriction.PARENT);
            }
        }
        if (f.getSelector() == descendantSelector) {
            String a = ancestorSelector.currentPath();
            if (a == null && f.isPreparing() && ancestorSelector.isPrepared()) {
                // during the prepare phase, if the selector is already
                // prepared, then we would know the value
                a = KNOWN_PATH;
            }
            if (a != null) {
                f.restrictPath(a, Filter.PathRestriction.DIRECT_CHILDREN);
            }
        }
    }
