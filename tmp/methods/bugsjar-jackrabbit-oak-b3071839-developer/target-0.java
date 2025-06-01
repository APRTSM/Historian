    public Revision getLiveRevision(RevisionContext context, Revision maxRev,
                                    Map<Revision, String> validRevisions,
                                    LastRevs lastRevs) {
        // check local deleted map first
        Value value = getLatestValue(context, getLocalDeleted(),
                null, maxRev, validRevisions, lastRevs);
        if (value.value == null && !getPreviousRanges().isEmpty()) {
            // need to check complete map
            value = getLatestValue(context, getDeleted(),
                    null, maxRev, validRevisions, lastRevs);
        }

        return "false".equals(value.value) ? value.revision : null;
    }
    public DocumentNodeState getNodeAtRevision(@Nonnull DocumentNodeStore nodeStore,
                                               @Nonnull Revision readRevision,
                                               @Nullable Revision lastModified) {
        Map<Revision, String> validRevisions = Maps.newHashMap();
        Branch branch = nodeStore.getBranches().getBranch(readRevision);
        LastRevs lastRevs = new LastRevs(getLastRev(), readRevision, branch);
        // overlay with unsaved last modified from this instance
        lastRevs.update(lastModified);

        Revision min = getLiveRevision(nodeStore, readRevision, validRevisions, lastRevs);
        if (min == null) {
            // deleted
            return null;
        }
        String path = getPath();
        DocumentNodeState n = new DocumentNodeState(nodeStore, path, readRevision, hasChildren());
        Revision lastRevision = min;
        for (String key : keySet()) {
            if (!Utils.isPropertyName(key)) {
                continue;
            }
            // first check local map, which contains most recent values
            Value value = getLatestValue(nodeStore, getLocalMap(key),
                    min, readRevision, validRevisions, lastRevs);

            // check if there may be more recent values in a previous document
            if (!getPreviousRanges().isEmpty()) {
                Revision newest = getLocalMap(key).firstKey();
                if (isRevisionNewer(nodeStore, newest, value.revision)) {
                    // not reading the most recent value, we may need to
                    // consider previous documents as well
                    Revision newestPrev = getPreviousRanges().firstKey();
                    if (isRevisionNewer(nodeStore, newestPrev, value.revision)) {
                        // a previous document has more recent changes
                        // than value.revision
                        value = null;
                    }
                }
            }

            if (value == null && !getPreviousRanges().isEmpty()) {
                // check complete revision history
                value = getLatestValue(nodeStore, getValueMap(key),
                        min, readRevision, validRevisions, lastRevs);
            }
            String propertyName = Utils.unescapePropertyName(key);
            String v = value != null ? value.value : null;
            n.setProperty(propertyName, v);
            // keep track of when this node was last modified
            if (value != null && isRevisionNewer(nodeStore, value.revision, lastRevision)) {
                lastRevision = value.revision;
            }
        }

        // lastRevision now points to the revision when this node was
        // last modified directly. but it may also have been 'modified'
        // by an operation on a descendant node, which is tracked in
        // _lastRev.

        // when was this node last modified?
        Revision branchBase = null;
        if (branch != null) {
            branchBase = branch.getBase(readRevision);
        }
        for (Revision r : lastRevs.get().values()) {
            // ignore if newer than readRevision
            if (isRevisionNewer(nodeStore, r, readRevision)) {
                // the node has a _lastRev which is newer than readRevision
                // this means we don't know when this node was
                // modified by an operation on a descendant node between
                // current lastRevision and readRevision. therefore we have
                // to stay on the safe side and use readRevision
                lastRevision = readRevision;
                continue;
            } else if (branchBase != null && isRevisionNewer(nodeStore, r, branchBase)) {
                // readRevision is on a branch and the node has a
                // _lastRev which is newer than the base of the branch
                // we cannot use this _lastRev because it is not visible
                // from this branch. highest possible revision of visible
                // changes is the base of the branch
                r = branchBase;
            }
            if (revisionAreAmbiguous(nodeStore, r, lastRevision)) {
                // _lastRev entries from multiple cluster nodes are ambiguous
                // use readRevision to make sure read is consistent
                lastRevision = readRevision;
            } else if (isRevisionNewer(nodeStore, r, lastRevision)) {
                lastRevision = r;
            }
        }
        if (branch != null) {
            // read from a branch
            // -> possibly overlay with unsaved last revs from branch
            lastRevs.updateBranch(branch.getUnsavedLastRevision(path, readRevision));
            Revision r = lastRevs.getBranchRevision();
            if (r != null) {
                lastRevision = r;
            }
        }
        n.setLastRevision(lastRevision);
        return n;
    }
    private Value getLatestValue(@Nonnull RevisionContext context,
                                 @Nonnull Map<Revision, String> valueMap,
                                 @Nullable Revision min,
                                 @Nonnull Revision readRevision,
                                 @Nonnull Map<Revision, String> validRevisions,
                                 @Nonnull LastRevs lastRevs) {
        for (Map.Entry<Revision, String> entry : valueMap.entrySet()) {
            Revision propRev = entry.getKey();
            String commitValue = validRevisions.get(propRev);
            if (commitValue == null) {
                // resolve revision
                NodeDocument commitRoot = getCommitRoot(propRev);
                if (commitRoot == null) {
                    continue;
                }
                commitValue = commitRoot.getCommitValue(propRev);
                if (commitValue == null) {
                    continue;
                }
            }

            Revision commitRev = resolveCommitRevision(propRev, commitValue);
            if (Utils.isCommitted(commitValue)) {
                lastRevs.update(commitRev);
            } else {
                // branch commit
                lastRevs.updateBranch(commitRev.asBranchRevision());
            }

            if (min != null && isRevisionNewer(context, min, commitRev)) {
                continue;
            }
            if (isValidRevision(context, propRev, commitValue, readRevision, validRevisions)) {
                // TODO: need to check older revisions as well?
                return new Value(commitRev, entry.getValue());
            }
        }

        Revision r = min != null ? min : readRevision;
        return new Value(r, null);
    }
