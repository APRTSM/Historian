    public NodeState merge(@Nonnull NodeBuilder builder,
                           @Nonnull CommitHook commitHook,
                           @Nullable CommitInfo info)
            throws CommitFailedException {
        return asDocumentRootBuilder(builder).merge(commitHook, info);
    }
    public Revision getNewestRevision(RevisionContext context,
                                      Revision changeRev,
                                      CollisionHandler handler) {
        // no need to look at all commits. the primary document
        // always contains at least one commit, including all
        // branch commits which are not yet merged
        SortedMap<Revision, String> revisions = getLocalRevisions();
        SortedMap<Revision, String> commitRoots = getLocalCommitRoot();
        Revision newestRev = null;
        for (Revision r : Iterables.mergeSorted(
                Arrays.asList(revisions.keySet(), commitRoots.keySet()),
                revisions.comparator())) {
            if (!r.equals(changeRev)) {
                if (isValidRevision(context, r, null, changeRev, new HashMap<Revision, String>())) {
                    newestRev = r;
                    // found newest revision, no need to check more revisions
                    // revisions are sorted newest first
                    break;
                } else {
                    handler.concurrentModification(r);
                }
            }
        }
        if (newestRev == null) {
            return null;
        }

        // the local deleted map contains the most recent revisions
        SortedMap<Revision, String> deleted = getLocalDeleted();
        String value = deleted.get(newestRev);
        if (value == null && deleted.headMap(newestRev).isEmpty()) {
            // newestRev is newer than most recent entry in local deleted
            // no need to check previous docs
            return newestRev;
        }

        if (value == null) {
            // get from complete map
            value = getDeleted().get(newestRev);
        }
        if ("true".equals(value)) {
            // deleted in the newest revision
            return null;
        }
        return newestRev;
    }
