    public NodeState merge(@Nonnull NodeBuilder builder,
                           @Nonnull CommitHook commitHook,
                           @Nonnull CommitInfo info)
            throws CommitFailedException {
        return asDocumentRootBuilder(builder).merge(commitHook, info);
    }
    public Revision getNewestRevision(final RevisionContext context,
                                      final Revision changeRev,
                                      final CollisionHandler handler) {
        final Map<Revision, String> validRevisions = Maps.newHashMap();
        Predicate<Revision> predicate = new Predicate<Revision>() {
            @Override
            public boolean apply(Revision input) {
                if (input.equals(changeRev)) {
                    return false;
                }
                if (isValidRevision(context, input, null, changeRev, validRevisions)) {
                    return true;
                }
                handler.concurrentModification(input);
                return false;
            }
        };

        Revision newestRev = null;
        // check local commits first
        SortedMap<Revision, String> revisions = getLocalRevisions();
        SortedMap<Revision, String> commitRoots = getLocalCommitRoot();
        Iterator<Revision> it = filter(Iterables.mergeSorted(
                Arrays.asList(revisions.keySet(), commitRoots.keySet()),
                revisions.comparator()), predicate).iterator();
        if (it.hasNext()) {
            newestRev = it.next();
        } else {
            // check full history (only needed in rare cases)
            it = filter(Iterables.mergeSorted(
                    Arrays.asList(
                            getValueMap(REVISIONS).keySet(),
                            getValueMap(COMMIT_ROOT).keySet()),
                    revisions.comparator()), predicate).iterator();
            if (it.hasNext()) {
                newestRev = it.next();
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
