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
