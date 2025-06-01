    private Revision determineMissedLastRev(NodeDocument doc, int clusterId) {
        Revision currentLastRev = doc.getLastRev().get(clusterId);
        if (currentLastRev == null) {
            currentLastRev = new Revision(0, 0, clusterId);
        }

        ClusterPredicate cp = new ClusterPredicate(clusterId);

        // Merge sort the revs for which changes have been made
        // to this doc

        // localMap always keeps the most recent valid commit entry
        // per cluster node so looking into that should be sufficient
        Iterable<Revision> revs = mergeSorted(of(
                filter(doc.getLocalCommitRoot().keySet(), cp),
                filter(doc.getLocalRevisions().keySet(), cp)),
                StableRevisionComparator.REVERSE
                );

        // Look for latest valid revision > currentLastRev
        // if found then lastRev needs to be fixed
        for (Revision rev : revs) {
            if (rev.compareRevisionTime(currentLastRev) > 0) {
                rev = doc.getCommitRevision(rev);
                if (rev != null) {
                    return rev;
                }
            } else {
                // No valid revision found > currentLastRev
                // indicates that lastRev is valid for given clusterId
                // and no further checks are required
                break;
            }
        }
        return null;
    }
