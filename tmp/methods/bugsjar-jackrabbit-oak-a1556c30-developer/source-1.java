    static String getPropertyNext(@Nonnull final NodeBuilder node, final int lane) {
        checkNotNull(node);
        
        String next = "";
        PropertyState ps = node.getProperty(NEXT);
        if (ps != null) {
            if (ps.isArray()) {
                next = ps.getValue(Type.STRING, Math.min(ps.count() - 1, lane));
            } else {
                next = ps.getValue(Type.STRING);
            }
        }
        return next;
    }
    static void setPropertyNext(@Nonnull final NodeBuilder node, 
                                final String value, final int lane) {
        if (node != null && value != null && lane >= 0 && lane < OrderedIndex.LANES) {
            PropertyState next = node.getProperty(NEXT);
            if (next != null) {
                String[] values;
                if (next.isArray()) {
                    values = Iterables.toArray(next.getValue(Type.STRINGS), String.class);
                    if (values.length < OrderedIndex.LANES) {
                        // it could be we increased the number of lanes and running on some existing
                        // content
                        LOG.debug("topping-up the number of lanes.");
                        List<String> vv = Lists.newArrayList(values);
                        for (int i = vv.size(); i <= OrderedIndex.LANES; i++) {
                            vv.add("");
                        }
                        values = vv.toArray(new String[vv.size()]);
                    }
                } else {
                    values = Iterables.toArray(EMPTY_NEXT, String.class);
                    values[0] = next.getValue(Type.STRING);
                }
                values[lane] = value;
                setPropertyNext(node, values);
            }
        }
    }
    String seek(@Nonnull final NodeBuilder index,
                               @Nonnull final Predicate<String> condition,
                               @Nullable final String[] walkedLanes) {
        boolean keepWalked = false;
        String searchfor = condition.getSearchFor();
        if (LOG.isDebugEnabled()) {
            LOG.debug("seek() - Searching for: {}", condition.getSearchFor());        
            LOG.debug("seek() - condition: {}", condition);
        }
        Predicate<String> walkingPredicate = direction.isAscending() 
                                                             ? new PredicateLessThan(searchfor, true)
                                                             : new PredicateGreaterThan(searchfor, true);
        // we always begin with :start
        String currentKey = START;
        String found = null;
        
        if (walkedLanes != null) {
            if (walkedLanes.length != OrderedIndex.LANES) {
                throw new IllegalArgumentException(String.format(
                    "Wrong size for keeping track of the Walked Lanes. Expected %d but was %d",
                    OrderedIndex.LANES, walkedLanes.length));
            }
            // ensuring the right data
            for (int i = 0; i < walkedLanes.length; i++) {
                walkedLanes[i] = currentKey;
            }
            keepWalked = true;
        }

        int lane;
        boolean stillLaning;
        String nextkey; 

        if ((direction.isAscending() && condition instanceof PredicateLessThan)
            || (direction.isDescending() && condition instanceof PredicateGreaterThan)) {
            // we're asking for a <, <= query from ascending index or >, >= from descending
            // we have to walk the lanes from bottom to up rather than up to bottom.
            
            LOG.debug("seek() - cross case");
            
            lane = 0;
            do {
                stillLaning = lane < OrderedIndex.LANES;
                nextkey = getPropertyNext(index.getChildNode(currentKey), lane);
                if ((Strings.isNullOrEmpty(nextkey) || !walkingPredicate.apply(nextkey)) && lane < OrderedIndex.LANES) {
                    // if we're currently pointing to NIL or the next element does not fit the search
                    // but we still have lanes left
                    lane++;
                } else {
                    if (condition.apply(nextkey)) {
                        found = nextkey;
                    } else {
                        currentKey = nextkey;
                        if (keepWalked && !Strings.isNullOrEmpty(currentKey)) {
                            walkedLanes[lane] = currentKey;
                        }
                    }
                }
            } while (((!Strings.isNullOrEmpty(nextkey) && walkingPredicate.apply(nextkey)) || stillLaning) && (found == null));
        } else {
            LOG.debug("seek() - plain case");
            
            lane = OrderedIndex.LANES - 1;
            
            do {
                stillLaning = lane > 0;
                nextkey = getPropertyNext(index.getChildNode(currentKey), lane);
                if ((Strings.isNullOrEmpty(nextkey) || !walkingPredicate.apply(nextkey)) && lane > 0) {
                    // if we're currently pointing to NIL or the next element does not fit the search
                    // but we still have lanes left, let's lower the lane;
                    lane--;
                } else {
                    if (condition.apply(nextkey)) {
                        found = nextkey;
                    } else {
                        currentKey = nextkey;
                        if (keepWalked && !Strings.isNullOrEmpty(currentKey)) {
                            for (int l = lane; l >= 0; l--) {
                                walkedLanes[l] = currentKey;
                            }
                        }
                    }
                }
            } while (((!Strings.isNullOrEmpty(nextkey) && walkingPredicate.apply(nextkey)) || stillLaning) && (found == null));
        }
        
        return found;
    }
    static void setPropertyNext(@Nonnull final NodeBuilder node, final String... next) {
        if (node != null && next != null) {
            String n1 = (next.length > 0) ? next[0] : "";
            String n2 = (next.length > 1) ? next[1] : "";
            String n3 = (next.length > 2) ? next[2] : "";
            String n4 = (next.length > 3) ? next[3] : "";
            
            node.setProperty(NEXT, ImmutableList.of(n1, n2, n3, n4), Type.STRINGS);
        }
    }
