    private void collectRevisionsAndCommitRoot() {
        NavigableMap<Revision, String> revisions =
                new TreeMap<Revision, String>(StableRevisionComparator.INSTANCE);
        for (Map.Entry<Revision, String> entry : doc.getLocalRevisions().entrySet()) {
            if (splitRevs.contains(entry.getKey())) {
                revisions.put(entry.getKey(), entry.getValue());
                numValues++;
            } else {
                // move _revisions entries that act as commit root without
                // local changes
                if (context.getClusterId() != entry.getKey().getClusterId()) {
                    // only consider local changes
                    continue;
                }
                if (doc.isCommitted(entry.getKey())
                        && !mostRecentRevs.contains(entry.getKey())) {
                    // this is a commit root for changes in other documents
                    revisions.put(entry.getKey(), entry.getValue());
                    numValues++;
                    trackHigh(entry.getKey());
                    trackLow(entry.getKey());
                }
            }
        }
        committedChanges.put(REVISIONS, revisions);
        NavigableMap<Revision, String> commitRoot =
                new TreeMap<Revision, String>(StableRevisionComparator.INSTANCE);
        boolean mostRecent = true;
        for (Map.Entry<Revision, String> entry : doc.getLocalCommitRoot().entrySet()) {
            Revision r = entry.getKey();
            if (splitRevs.contains(r)) {
                commitRoot.put(r, entry.getValue());
                numValues++;
            } else if (r.getClusterId() == context.getClusterId() 
                    && !changes.contains(r)) {
                // OAK-2528: _commitRoot entry without associated change
                // consider all but most recent as garbage (OAK-3333)
                if (mostRecent) {
                    mostRecent = false;
                } else {
                    addGarbage(r, COMMIT_ROOT);
                }
            }
        }
        committedChanges.put(COMMIT_ROOT, commitRoot);
    }
