    void prune(final NodeBuilder index, final Deque<NodeBuilder> builders, final String key) {
        LOG.debug("prune() - deleting: {}", key);
        for (NodeBuilder node : builders) {
            if (node.hasProperty("match") || node.getChildNodeCount(1) > 0) {
                return;
            } else if (node.exists()) {
                if (node.hasProperty(NEXT)) {
                    ChildNodeEntry[] walkedLanes = new ChildNodeEntry[OrderedIndex.LANES];
                    ChildNodeEntry entry;
                    String lane0Next, prevNext, currNext;
                    
                    // for as long as we have the an entry and we didn't update the lane0 we have
                    // to keep searching and update
                    do {
                        entry = seek(index.getNodeState(),
                            new PredicateEquals(key),
                            walkedLanes
                            );
                        lane0Next = getPropertyNext(walkedLanes[0]);
                        if (LOG.isDebugEnabled()) {
                            for (int i = 0; i < walkedLanes.length; i++) {
                                LOG.debug("prune() - walkedLanes[{}]: {}", i,
                                    walkedLanes[i].getName());
                            }
                        }
                        for (int lane = walkedLanes.length - 1; lane >= 0; lane--) {
                            prevNext = getPropertyNext(walkedLanes[lane], lane);
                            if (key.equals(prevNext)) {
                                // if it's actually pointing to us let's deal with it
                                currNext = getPropertyNext(node, lane);
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(
                                        "prune() - setting next for '{}' on lane '{}' with '{}'",
                                        new Object[] {
                                        walkedLanes[lane].getName(),
                                        lane,
                                        currNext});
                                }
                                setPropertyNext(index.getChildNode(walkedLanes[lane].getName()),
                                    currNext, lane);
                            }
                        }
                    } while (entry != null && !key.equals(lane0Next));
                }
                node.remove();
            }
        }
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
                        values = vv.toArray(new String[0]);
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
