    SortedMap<Revision, Range> getPreviousRanges() {
        @SuppressWarnings("unchecked")
        SortedMap<Revision, Range> previous = (SortedMap<Revision, Range>) get(PREVIOUS);
        if (previous == null) {
            previous = EMPTY_RANGE_MAP;
        }
        return previous;
    }
    public Iterable<UpdateOp> split(@Nonnull RevisionContext context) {
        // only consider if there are enough commits
        if (getLocalRevisions().size() + getLocalCommitRoot().size() <= REVISIONS_SPLIT_OFF_SIZE) {
            return Collections.emptyList();
        }
        String id = getId();
        SortedMap<Revision, Range> previous = getPreviousRanges();
        // what's the most recent previous revision?
        Revision recentPrevious = null;
        for (Revision rev : previous.keySet()) {
            if (rev.getClusterId() != context.getClusterId()) {
                continue;
            }
            if (recentPrevious == null
                    || isRevisionNewer(context, rev, recentPrevious)) {
                recentPrevious = rev;
            }
        }
        Map<String, NavigableMap<Revision, String>> splitValues
                = new HashMap<String, NavigableMap<Revision, String>>();
        for (String property : new String[]{REVISIONS, COMMIT_ROOT, DELETED}) {
            NavigableMap<Revision, String> splitMap
                    = new TreeMap<Revision, String>(context.getRevisionComparator());
            splitValues.put(property, splitMap);
            Map<String, String> valueMap = getLocalMap(property);
            // collect committed changes of this cluster node after the
            // most recent previous split revision
            for (Map.Entry<String, String> entry : valueMap.entrySet()) {
                Revision rev = Revision.fromString(entry.getKey());
                if (rev.getClusterId() != context.getClusterId()) {
                    continue;
                }
                if (recentPrevious == null
                        || isRevisionNewer(context, rev, recentPrevious)) {
                    if (isCommitted(rev)) {
                        splitMap.put(rev, entry.getValue());
                    }
                }
            }
        }

        List<UpdateOp> splitOps = Collections.emptyList();
        int numValues = 0;
        Revision high = null;
        Revision low = null;
        for (NavigableMap<Revision, String> splitMap : splitValues.values()) {
            // keep the most recent in the main document
            if (!splitMap.isEmpty()) {
                splitMap.remove(splitMap.lastKey());
            }
            if (splitMap.isEmpty()) {
                continue;
            }
            // remember highest / lowest revision
            if (high == null || isRevisionNewer(context, splitMap.lastKey(), high)) {
                high = splitMap.lastKey();
            }
            if (low == null || isRevisionNewer(context, low, splitMap.firstKey())) {
                low = splitMap.firstKey();
            }
            numValues += splitMap.size();
        }
        if (high != null && low != null && numValues >= REVISIONS_SPLIT_OFF_SIZE) {
            // enough revisions to split off
            splitOps = new ArrayList<UpdateOp>(2);
            // move to another document
            UpdateOp main = new UpdateOp(id, false);
            main.setMapEntry(PREVIOUS, high.toString(), low.toString());
            UpdateOp old = new UpdateOp(Utils.getPreviousIdFor(id, high), true);
            old.set(ID, old.getKey());
            for (String property : splitValues.keySet()) {
                NavigableMap<Revision, String> splitMap = splitValues.get(property);
                for (Map.Entry<Revision, String> entry : splitMap.entrySet()) {
                    String r = entry.getKey().toString();
                    main.removeMapEntry(property, r);
                    old.setMapEntry(property, r, entry.getValue());
                }
                splitOps.add(old);
                splitOps.add(main);
            }
        }
        return splitOps;
    }
    public boolean isCommitted(@Nonnull Revision revision) {
        String rev = checkNotNull(revision).toString();
        String value = getLocalRevisions().get(rev);
        if (value != null) {
            return Utils.isCommitted(value);
        }
        // check previous docs
        for (NodeDocument prev : getPreviousDocs(revision, REVISIONS)) {
            if (prev.containsRevision(revision)) {
                return prev.isCommitted(revision);
            }
        }
        return false;
    }
    protected Map<?, ?> transformAndSeal(@Nonnull Map<Object, Object> map,
                                         @Nullable String key,
                                         int level) {
        if (level == 1) {
            if (PREVIOUS.equals(key)) {
                SortedMap<Revision, Range> transformed = new TreeMap<Revision, Range>(
                        new Comparator<Revision>() {
                            @Override
                            public int compare(Revision o1, Revision o2) {
                                // in reverse order!
                                int c = o2.compareRevisionTime(o1);
                                if (c == 0) {
                                    c = o1.getClusterId() < o2.getClusterId()
                                            ? -1
                                            : (o1.getClusterId() == o2.getClusterId() ? 0 : 1);
                                }
                                return c;
                            }
                        });
                for (Map.Entry<Object, Object> entry : map.entrySet()) {
                    Revision high = Revision.fromString(entry.getKey().toString());
                    Revision low = Revision.fromString(entry.getValue().toString());
                    transformed.put(high, new Range(high, low));
                }
                return Collections.unmodifiableSortedMap(transformed);
            }
        }
        return super.transformAndSeal(map, key, level);
    }
