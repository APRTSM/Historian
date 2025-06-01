    public SortedMap<Revision, Revision> getUncommittedRevisions(RevisionContext context) {
        // only look at revisions in this document.
        // uncommitted revisions are not split off
        Map<Revision, String> valueMap = getLocalRevisions();
        SortedMap<Revision, Revision> revisions =
                new TreeMap<Revision, Revision>(context.getRevisionComparator());
        for (Map.Entry<Revision, String> commit : valueMap.entrySet()) {
            if (!Utils.isCommitted(commit.getValue())) {
                Revision r = commit.getKey();
                if (r.getClusterId() == context.getClusterId()) {
                    Revision b = Revision.fromString(commit.getValue());
                    revisions.put(r, b);
                }
            }
        }
        return revisions;
    }
    void init(DocumentStore store, RevisionContext context) {
        if (!initialized.compareAndSet(false, true)) {
            throw new IllegalStateException("already initialized");
        }
        NodeDocument doc = store.find(Collection.NODES, Utils.getIdFromPath("/"));
        if (doc == null) {
            return;
        }
        SortedMap<Revision, Revision> revisions = doc.getUncommittedRevisions(context);
        while (!revisions.isEmpty()) {
            SortedSet<Revision> commits = new TreeSet<Revision>(comparator);
            Revision head = revisions.lastKey();
            commits.add(head);
            Revision base = revisions.remove(head).asTrunkRevision();
            while (revisions.containsKey(base)) {
                commits.add(base);
                base = revisions.remove(base).asTrunkRevision();
            }
            branches.add(new Branch(commits, base));
        }
    }
