    public int compareRows(PropertyValue[] orderValues,
            PropertyValue[] orderValues2) {
        int comp = 0;
        for (int i = 0, size = orderings.length; i < size; i++) {
            PropertyValue a = orderValues[i];
            PropertyValue b = orderValues2[i];
            if (a == null || b == null) {
                if (a == b) {
                    comp = 0;
                } else if (a == null) {
                    // TODO order by: nulls first (it looks like), or low?
                    comp = -1;
                } else {
                    comp = 1;
                }
            } else {
                comp = a.compareTo(b);
            }
            if (comp != 0) {
                if (orderings[i].isDescending()) {
                    comp = -comp;
                }
                break;
            }
        }
        return comp;
    }
    Iterator<ResultRowImpl> getRows() {
        prepare();
        Iterator<ResultRowImpl> it;
        if (explain) {
            String plan = source.getPlan(rootState);
            columns = new ColumnImpl[] { new ColumnImpl("explain", "plan", "plan")};
            ResultRowImpl r = new ResultRowImpl(this,
                    new String[0], 
                    new PropertyValue[] { PropertyValues.newString(plan)},
                    null);
            it = Arrays.asList(r).iterator();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("plan: " + source.getPlan(rootState));
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
