        public long getMinimumTimestamp(@Nonnull Revision revision,
                                        @Nonnull Map<Integer, Long> inactive) {
            long timestamp = checkNotNull(revision).getTimestamp();
            Revision seenAt = getRevisionSeen(revision);
            if (seenAt == null) {
                // already purged
                return timestamp;
            }
            // go through all known cluster nodes
            for (Map.Entry<Integer, List<RevisionRange>> e : map.entrySet()) {
                if (revision.getClusterId() == currentClusterNodeId
                        && e.getKey() == currentClusterNodeId) {
                    // range and revision is for current cluster node
                    // no need to adjust timestamp
                    continue;
                }
                List<RevisionRange> list = e.getValue();
                RevisionRange range;
                for (int i = list.size() - 1; i >= 0; i--) {
                    range = list.get(i);
                    if (range.seenAt.compareRevisionTimeThenClusterId(seenAt) <= 0) {
                        // found newest range older or equal the given seenAt
                        // check if the cluster node is still active
                        Long inactiveSince = inactive.get(range.revision.getClusterId());
                        if (inactiveSince != null
                                && revision.getTimestamp() > inactiveSince
                                && range.revision.getTimestamp() < inactiveSince) {
                            // ignore, because the revision is after the
                            // cluster node became inactive and the most recent
                            // range is before it became inactive
                        } else {
                            timestamp = Math.min(timestamp, range.revision.getTimestamp());
                        }
                        break;
                    }
                }
            }
            return timestamp;
        }
