    public Iterable<UpdateOp> split(@Nonnull RevisionContext context) {
        SortedMap<Revision, Range> previous = getPreviousRanges();
        // only consider if there are enough commits,
        // unless document is really big
        if (getLocalRevisions().size() + getLocalCommitRoot().size() <= NUM_REVS_THRESHOLD
                && getMemory() < DOC_SIZE_THRESHOLD
                && previous.size() < PREV_SPLIT_FACTOR) {
            return Collections.emptyList();
        }
        String path = getPath();
        String id = getId();
        if (id == null) {
            throw new IllegalStateException("document does not have an id: " + this);
        }
        // collect ranges and create a histogram of the height
        Map<Integer, List<Range>> prevHisto = Maps.newHashMap();
        for (Map.Entry<Revision, Range> entry : previous.entrySet()) {
            Revision rev = entry.getKey();
            if (rev.getClusterId() != context.getClusterId()) {
                continue;
            }
            Range r = entry.getValue();
            List<Range> list = prevHisto.get(r.getHeight());
            if (list == null) {
                list = new ArrayList<Range>();
                prevHisto.put(r.getHeight(), list);
            }
            list.add(r);
        }
        Map<String, NavigableMap<Revision, String>> splitValues
                = new HashMap<String, NavigableMap<Revision, String>>();
        for (String property : data.keySet()) {
            if (IGNORE_ON_SPLIT.contains(property)) {
                continue;
            }
            NavigableMap<Revision, String> splitMap
                    = new TreeMap<Revision, String>(context.getRevisionComparator());
            splitValues.put(property, splitMap);
            Map<Revision, String> valueMap = getLocalMap(property);
            // collect committed changes of this cluster node after the
            // most recent previous split revision
            for (Map.Entry<Revision, String> entry : valueMap.entrySet()) {
                Revision rev = entry.getKey();
                if (rev.getClusterId() != context.getClusterId()) {
                    continue;
                }
                if (isCommitted(rev)) {
                    splitMap.put(rev, entry.getValue());
                }
            }
        }

        List<UpdateOp> splitOps = Lists.newArrayList();
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
        UpdateOp main = null;
        if (high != null && low != null
                && (numValues >= NUM_REVS_THRESHOLD
                    || getMemory() > DOC_SIZE_THRESHOLD)) {
            // enough revisions to split off
            // move to another document
            main = new UpdateOp(id, false);
            setPrevious(main, new Range(high, low, 0));
            String oldPath = Utils.getPreviousPathFor(path, high, 0);
            UpdateOp old = new UpdateOp(Utils.getIdFromPath(oldPath), true);
            old.set(ID, old.getId());
            if (Utils.isLongPath(oldPath)) {
                old.set(PATH, oldPath);
            }
            for (String property : splitValues.keySet()) {
                NavigableMap<Revision, String> splitMap = splitValues.get(property);
                for (Map.Entry<Revision, String> entry : splitMap.entrySet()) {
                    Revision r = entry.getKey();
                    main.removeMapEntry(property, r);
                    old.setMapEntry(property, r, entry.getValue());
                }
            }
            // check size of old document
            NodeDocument oldDoc = new NodeDocument(store);
            UpdateUtils.applyChanges(oldDoc, old, context.getRevisionComparator());
            setSplitDocProps(this, oldDoc, old, high);
            // only split if enough of the data can be moved to old document
            if (oldDoc.getMemory() > getMemory() * SPLIT_RATIO
                    || numValues >= NUM_REVS_THRESHOLD) {
                splitOps.add(old);
            } else {
                main = null;
            }
        }

        // check if we need to create intermediate previous documents
        for (Map.Entry<Integer, List<Range>> entry : prevHisto.entrySet()) {
            if (entry.getValue().size() >= PREV_SPLIT_FACTOR) {
                if (main == null) {
                    main = new UpdateOp(id, false);
                }
                // calculate range new range
                Revision h = null;
                Revision l = null;
                for (Range r : entry.getValue()) {
                    if (h == null || isRevisionNewer(context, r.high, h)) {
                        h = r.high;
                    }
                    if (l == null || isRevisionNewer(context, l, r.low)) {
                        l = r.low;
                    }
                    removePrevious(main, r);
                }
                if (h == null || l == null) {
                    throw new IllegalStateException();
                }
                String prevPath = Utils.getPreviousPathFor(path, h, entry.getKey() + 1);
                String prevId = Utils.getIdFromPath(prevPath);
                UpdateOp intermediate = new UpdateOp(prevId, true);
                intermediate.set(ID, prevId);
                if (Utils.isLongPath(prevPath)) {
                    intermediate.set(PATH, prevPath);
                }
                setPrevious(main, new Range(h, l, entry.getKey() + 1));
                for (Range r : entry.getValue()) {
                    setPrevious(intermediate, r);
                }
                setIntermediateDocProps(intermediate, h);
                splitOps.add(intermediate);
            }
        }

        // main document must be updated last
        if (main != null && !splitOps.isEmpty()) {
            splitOps.add(main);
        }

        return splitOps;
    }
