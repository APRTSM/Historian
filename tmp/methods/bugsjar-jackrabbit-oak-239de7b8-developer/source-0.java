    private void collectRevisionsAndCommitRoot() {
        NavigableMap<Revision, String> revisions =
                new TreeMap<Revision, String>(context.getRevisionComparator());
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
                new TreeMap<Revision, String>(context.getRevisionComparator());
        for (Map.Entry<Revision, String> entry : doc.getLocalCommitRoot().entrySet()) {
            if (splitRevs.contains(entry.getKey())) {
                commitRoot.put(entry.getKey(), entry.getValue());
                numValues++;
            }
        }
        committedChanges.put(COMMIT_ROOT, commitRoot);
    }
    private Map<String, NavigableMap<Revision, String>> getCommittedLocalChanges() {
        Map<String, NavigableMap<Revision, String>> committedLocally
                = new HashMap<String, NavigableMap<Revision, String>>();
        for (String property : filter(doc.keySet(), PROPERTY_OR_DELETED)) {
            NavigableMap<Revision, String> splitMap
                    = new TreeMap<Revision, String>(context.getRevisionComparator());
            committedLocally.put(property, splitMap);
            Map<Revision, String> valueMap = doc.getLocalMap(property);
            // collect committed changes of this cluster node
            for (Map.Entry<Revision, String> entry : valueMap.entrySet()) {
                Revision rev = entry.getKey();
                if (rev.getClusterId() != context.getClusterId()) {
                    continue;
                }
                if (doc.isCommitted(rev)) {
                    splitMap.put(rev, entry.getValue());
                } else if (isGarbage(rev)) {
                    addGarbage(rev, property);
                }
            }
        }
        return committedLocally;
    }
    private void removeGarbage() {
        if (garbage.isEmpty()) {
            return;
        } else if (main == null) {
            main = new UpdateOp(id, false);
        }
        for (Map.Entry<String, Set<Revision>> entry : garbage.entrySet()) {
            for (Revision r : entry.getValue()) {
                main.removeMapEntry(entry.getKey(), r);
                NodeDocument.removeCommitRoot(main, r);
                NodeDocument.removeRevision(main, r);
            }
        }
    }
    private void addGarbage(Revision rev, String property) {
        Set<Revision> revisions = garbage.get(property);
        if (revisions == null) {
            revisions = Sets.newHashSet();
            garbage.put(property, revisions);
        }
        revisions.add(rev);
    }
    private List<UpdateOp> create() {
        if (!considerSplit()) {
            return Collections.emptyList();
        }
        splitOps = Lists.newArrayList();
        mostRecentRevs = Sets.newHashSet();
        splitRevs = Sets.newHashSet();
        garbage = Maps.newHashMap();
        committedChanges = getCommittedLocalChanges();

        // revisions of the most recent committed changes on this document
        // these are kept in the main document. _revisions and _commitRoot
        // entries with these revisions are retained in the main document
        populateSplitRevs();

        // collect _revisions and _commitRoot entries for split document
        collectRevisionsAndCommitRoot();

        // create split ops out of the split values
        main = createSplitOps();

        // create intermediate docs if needed
        createIntermediateDocs();

        // remove stale references to previous docs
        disconnectStalePrevDocs();
        
        // remove garbage
        removeGarbage();

        // main document must be updated last
        if (main != null) {
            splitOps.add(main);
        }

        return splitOps;
    }
