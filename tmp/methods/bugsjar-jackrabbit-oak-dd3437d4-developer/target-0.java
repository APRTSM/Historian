        Revision getRevisionSeen(Revision r) {
            List<RevisionRange> list = map.get(r.getClusterId());
            if (list == null) {
                if (r.getTimestamp() <= oldestTimestamp) {
                    // old revision with already purged range
                    return null;
                }
                if (r.getClusterId() != currentClusterNodeId) {
                    // this is from a cluster node we did not see yet
                    // see also OAK-1170
                    return FUTURE;
                }
                return null;
            }
            // search from latest backward
            // (binary search could be used, but we expect most queries
            // at the end of the list)
            RevisionRange range = null;
            for (int i = list.size() - 1; i >= 0; i--) {
                range = list.get(i);
                int compare = r.compareRevisionTime(range.revision);
                if (compare == 0) {
                    return range.seenAt;
                } else if (compare > 0) {
                    if (i == list.size() - 1) {
                        // newer than the newest range
                        if (r.getClusterId() == currentClusterNodeId) {
                            // newer than all others, except for FUTURE
                            return NEWEST;
                        } else {
                            // happens in the future (not visible yet)
                            return FUTURE;
                        }
                    } else {
                        // there is a newer range
                        return list.get(i + 1).seenAt;
                    }
                }
            }
            if (range != null && r.getTimestamp() > oldestTimestamp) {
                // revision is older than earliest range and after purge
                // timestamp. return seen-at revision of earliest range.
                return range.seenAt;
            }
            return null;
        }
