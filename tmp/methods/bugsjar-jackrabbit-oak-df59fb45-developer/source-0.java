    void prune(final NodeBuilder index, final Deque<NodeBuilder> builders, final String key) {
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
                        for (int lane = walkedLanes.length - 1; lane >= 0; lane--) {
                            prevNext = getPropertyNext(walkedLanes[lane], lane);
                            if (key.equals(prevNext)) {
                                // if it's actually pointing to us let's deal with it
                                currNext = getPropertyNext(node, lane);
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
                String[] values = Iterables.toArray(next.getValue(Type.STRINGS), String.class);
                values[lane] = value;
                setPropertyNext(node, values);
            }
        }
    }
