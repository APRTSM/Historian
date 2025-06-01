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
    public void restrict(FilterImpl f) {
        if (f.getSelector() == selector1) {
            PropertyValue p2 = selector2.currentProperty(property2Name);
            if (p2 == null && f.isPreparing() && selector2.isPrepared()) {
                // during the prepare phase, if the selector is already
                // prepared, then we would know the value
                p2 = PropertyValues.newString(KNOWN_VALUE);
            }
            if (p2 != null) {
                if (p2.isArray()) {
                    // TODO support join on multi-valued properties
                    p2 = null;
                }
            }
            // always set the condition, even if unkown ( -> is not null)
            f.restrictProperty(property1Name, Operator.EQUAL, p2);
        }
        if (f.getSelector() == selector2) {
            PropertyValue p1 = selector1.currentProperty(property1Name);
            if (p1 == null && f.isPreparing() && selector1.isPrepared()) {
                // during the prepare phase, if the selector is already
                // prepared, then we would know the value
                p1 = PropertyValues.newString(KNOWN_VALUE);
            }
            if (p1 != null) {
                if (p1.isArray()) {
                    // TODO support join on multi-valued properties
                    p1 = null;
                }
            }
            // always set the condition, even if unkown ( -> is not null)
            f.restrictProperty(property2Name, Operator.EQUAL, p1);
        }
    }
    public abstract boolean evaluate();
    public abstract void restrictPushDown(SelectorImpl s);
    public String getPlan(NodeState rootState) {
        StringBuilder buff = new StringBuilder();
        buff.append(left.getPlan(rootState)).
            append(' ').
            append(joinType).
            append(' ').
            append(right.getPlan(rootState)).
            append(" on ").
            append(joinCondition);
        return buff.toString();
    }
    public void restrict(FilterImpl f) {
        if (f.getSelector() == selector1) {
            String p2 = selector2.currentPath();
            if (p2 == null && f.isPreparing() && selector2.isPrepared()) {
                // during the prepare phase, if the selector is already
                // prepared, then we would know the value
                p2 = KNOWN_PATH;
            }
            if (p2 != null) {
                if (selector2Path.equals(".")) {
                    f.restrictPath(p2, Filter.PathRestriction.EXACT);
                } else {
                    // TODO normalize paths; support more complex relative path (".." and so on)
                    String p = PathUtils.concat(p2, selector2Path);
                    f.restrictPath(p, Filter.PathRestriction.EXACT);
                }
            }
        }
        if (f.getSelector() == selector2) {
            String p1 = selector1.currentPath();
            if (p1 == null && f.isPreparing() && selector1.isPrepared()) {
                // during the prepare phase, if the selector is already
                // prepared, then we would know the value
                p1 = KNOWN_PATH;
            }
            if (p1 != null) {
                if (selector2Path.equals(".")) {
                    f.restrictPath(p1, Filter.PathRestriction.EXACT);
                } else {
                    // TODO normalize paths; support relative path (".." and so on)
                }
            }
        }
    }
    public String getPlan(NodeState rootState) {
        StringBuilder buff = new StringBuilder();
        buff.append(toString());
        buff.append(" /* ").append(index.getPlan(createFilter(true), rootState));
        if (selectorCondition != null) {
            buff.append(" where ").append(selectorCondition);
        }
        buff.append(" */");
        return buff.toString();
    }
    private Filter createFilter(boolean preparing) {
        FilterImpl f = new FilterImpl(this, query.getStatement());
        f.setPreparing(preparing);
        validateNodeType(nodeTypeName);
        f.setNodeType(nodeTypeName);
        if (joinCondition != null) {
            joinCondition.restrict(f);
        }
        
        // all conditions can be pushed to the selectors -
        // except in some cases to "outer joined" selectors,
        // but the exceptions are handled in the condition
        // itself.
        // An example where it *is* a problem:
        //  "select * from a left outer join b on a.x = b.y
        // where b.y is null" - in this case the selector b
        // must not use an index condition on "y is null"
        // (".. is null" must be written as "not .. is not null").
        if (queryConstraint != null) {
            queryConstraint.restrict(f);
        }

        return f;
    }
    public void prepare() {
        if (queryConstraint != null) {
            queryConstraint.restrictPushDown(this);
        }
        if (!outerJoinLeftHandSide && !outerJoinRightHandSide) {
            for (JoinConditionImpl c : allJoinConditions) {
                c.restrictPushDown(this);
            }
        }
        index = query.getBestIndex(createFilter(true));
    }
    public void execute(NodeState rootState) {
        cursor = index.query(createFilter(false), rootState);
    }
    public boolean isPrepared() {
        return index != null;
    }
    public void setPreparing(boolean preparing) {
        this.preparing = preparing;
    }
    public boolean isPreparing() {
        return preparing;
    }
