    public String getNodes(String path, String revisionId, int depth,
            long offset, int maxChildNodes, String filter)
            throws MicroKernelException {
        if (depth != 0) {
            throw new MicroKernelException("Only depth 0 is supported, depth is " + depth);
        }
        revisionId = revisionId != null ? revisionId : nodeStore.getHeadRevision().toString();
        Revision rev = Revision.fromString(revisionId);
        DocumentNodeState n = nodeStore.getNode(path, rev);
        if (n == null) {
            return null;
        }
        JsopStream json = new JsopStream();
        boolean includeId = filter != null && filter.contains(":id");
        includeId |= filter != null && filter.contains(":hash");
        json.object();
        n.append(json, includeId);
        int max;
        if (maxChildNodes == -1) {
            max = Integer.MAX_VALUE;
            maxChildNodes = Integer.MAX_VALUE;
        } else {
            // use long to avoid overflows
            long m = ((long) maxChildNodes) + offset;
            max = (int) Math.min(m, Integer.MAX_VALUE);
        }
        Children c = nodeStore.getChildren(n, null, max);
        for (long i = offset; i < c.children.size(); i++) {
            if (maxChildNodes-- <= 0) {
                break;
            }
            String name = PathUtils.getName(c.children.get((int) i));
            json.key(name).object().endObject();
        }
        if (c.hasMore) {
            // TODO use a better way to notify there are more children
            json.key(":childNodeCount").value(Long.MAX_VALUE);
        } else {
            json.key(":childNodeCount").value(c.children.size());
        }
        json.endObject();
        return json.toString();
    }
    private boolean dispatch(@Nonnull String jsonDiff,
                             @Nonnull DocumentNodeState base,
                             @Nonnull NodeStateDiff diff) {
        if (!AbstractNodeState.comparePropertiesAgainstBaseState(this, base, diff)) {
            return false;
        }
        if (jsonDiff.trim().isEmpty()) {
            return true;
        }
        JsopTokenizer t = new JsopTokenizer(jsonDiff);
        boolean continueComparison = true;
        while (continueComparison) {
            int r = t.read();
            if (r == JsopReader.END) {
                break;
            }
            switch (r) {
                case '+': {
                    String path = t.readString();
                    t.read(':');
                    t.read('{');
                    while (t.read() != '}') {
                        // skip properties
                    }
                    String name = PathUtils.getName(path);
                    continueComparison = diff.childNodeAdded(name, getChildNode(name));
                    break;
                }
                case '-': {
                    String path = t.readString();
                    String name = PathUtils.getName(path);
                    continueComparison = diff.childNodeDeleted(name, base.getChildNode(name));
                    break;
                }
                case '^': {
                    String path = t.readString();
                    t.read(':');
                    if (t.matches('{')) {
                        t.read('}');
                        String name = PathUtils.getName(path);
                        continueComparison = diff.childNodeChanged(name,
                                base.getChildNode(name), getChildNode(name));
                    } else if (t.matches('[')) {
                        // ignore multi valued property
                        while (t.read() != ']') {
                            // skip values
                        }
                    } else {
                        // ignore single valued property
                        t.read();
                    }
                    break;
                }
                case '>': {
                    String from = t.readString();
                    t.read(':');
                    String to = t.readString();
                    String fromName = PathUtils.getName(from);
                    continueComparison = diff.childNodeDeleted(
                            fromName, base.getChildNode(fromName));
                    if (!continueComparison) {
                        break;
                    }
                    String toName = PathUtils.getName(to);
                    continueComparison = diff.childNodeAdded(
                            toName, getChildNode(toName));
                    break;
                }
                default:
                    throw new IllegalArgumentException("jsonDiff: illegal token '"
                            + t.getToken() + "' at pos: " + t.getLastPos() + ' ' + jsonDiff);
            }
        }
        return continueComparison;
    }
    private void diffManyChildren(JsopWriter w, String path, Revision fromRev, Revision toRev) {
        long minTimestamp = Math.min(fromRev.getTimestamp(), toRev.getTimestamp());
        long minValue = Commit.getModified(minTimestamp);
        String fromKey = Utils.getKeyLowerLimit(path);
        String toKey = Utils.getKeyUpperLimit(path);
        Set<String> paths = Sets.newHashSet();
        for (NodeDocument doc : store.query(Collection.NODES, fromKey, toKey,
                NodeDocument.MODIFIED, minValue, Integer.MAX_VALUE)) {
            paths.add(Utils.getPathFromId(doc.getId()));
        }
        // also consider nodes with not yet stored modifications (OAK-1107)
        Revision minRev = new Revision(minTimestamp, 0, getClusterId());
        addPathsForDiff(path, paths, getPendingModifications(), minRev);
        for (Revision r : new Revision[]{fromRev, toRev}) {
            if (r.isBranch()) {
                Branch b = getBranches().getBranch(fromRev);
                if (b != null) {
                    addPathsForDiff(path, paths, b.getModifications(r), r);
                }
            }
        }
        for (String p : paths) {
            DocumentNodeState fromNode = getNode(p, fromRev);
            DocumentNodeState toNode = getNode(p, toRev);
            if (fromNode != null) {
                // exists in fromRev
                if (toNode != null) {
                    // exists in both revisions
                    // check if different
                    if (!fromNode.getLastRevision().equals(toNode.getLastRevision())) {
                        w.tag('^').key(p).object().endObject().newline();
                    }
                } else {
                    // does not exist in toRev -> was removed
                    w.tag('-').value(p).newline();
                }
            } else {
                // does not exist in fromRev
                if (toNode != null) {
                    // exists in toRev
                    w.tag('+').key(p).object().endObject().newline();
                } else {
                    // does not exist in either revisions
                    // -> do nothing
                }
            }
        }
    }
    public void applyChanges(Revision rev, Revision before, String path,
                             boolean isNew, boolean isDelete, boolean pendingLastRev,
                             boolean isBranchCommit, List<String> added,
                             List<String> removed, List<String> changed) {
        UnsavedModifications unsaved = unsavedLastRevisions;
        if (isBranchCommit) {
            Revision branchRev = rev.asBranchRevision();
            unsaved = branches.getBranch(branchRev).getModifications(branchRev);
        }
        if (isBranchCommit || pendingLastRev) {
            // write back _lastRev with background thread
            unsaved.put(path, rev);
        }
        if (isNew) {
            CacheValue key = childNodeCacheKey(path, rev, null);
            DocumentNodeState.Children c = new DocumentNodeState.Children();
            Set<String> set = Sets.newTreeSet(added);
            set.removeAll(removed);
            for (String p : added) {
                set.add(Utils.unshareString(p));
            }
            c.children.addAll(set);
            nodeChildrenCache.put(key, c);
        } else if (!isDelete) {
            // update diff cache for modified nodes
            PathRev key = diffCacheKey(path, before, rev);
            JsopWriter w = new JsopStream();
            for (String p : added) {
                w.tag('+').key(p).object().endObject().newline();
            }
            for (String p : removed) {
                w.tag('-').value(p).newline();
            }
            for (String p : changed) {
                w.tag('^').key(p).object().endObject().newline();
            }
            diffCache.put(key, new StringValue(w.toString()));
        }
        // update docChildrenCache
        if (!added.isEmpty()) {
            CacheValue docChildrenKey = new StringValue(path);
            NodeDocument.Children docChildren = docChildrenCache.getIfPresent(docChildrenKey);
            if (docChildren != null) {
                int currentSize = docChildren.childNames.size();
                NavigableSet<String> names = Sets.newTreeSet(docChildren.childNames);
                // incomplete cache entries must not be updated with
                // names at the end of the list because there might be
                // a next name in DocumentStore smaller than the one added
                if (!docChildren.isComplete) {
                    for (String childPath : added) {
                        String name = PathUtils.getName(childPath);
                        if (names.higher(name) != null) {
                            names.add(Utils.unshareString(name));
                        }
                    }
                } else {
                    // add all
                    for (String childPath : added) {
                        names.add(Utils.unshareString(PathUtils.getName(childPath)));
                    }
                }
                // any changes?
                if (names.size() != currentSize) {
                    // create new cache entry with updated names
                    boolean complete = docChildren.isComplete;
                    docChildren = new NodeDocument.Children();
                    docChildren.isComplete = complete;
                    docChildren.childNames.addAll(names);
                    docChildrenCache.put(docChildrenKey, docChildren);
                }
            }
        }
    }
    DocumentNodeState.Children readChildren(DocumentNodeState parent, String name, int limit) {
        // TODO use offset, to avoid O(n^2) and running out of memory
        // to do that, use the *name* of the last entry of the previous batch of children
        // as the starting point
        String path = parent.getPath();
        Revision rev = parent.getLastRevision();
        Iterable<NodeDocument> docs;
        DocumentNodeState.Children c = new DocumentNodeState.Children();
        // add one to the requested limit for the raw limit
        // this gives us a chance to detect whether there are more
        // child nodes than requested.
        int rawLimit = (int) Math.min(Integer.MAX_VALUE, ((long) limit) + 1);
        for (;;) {
            c.children.clear();
            docs = readChildDocs(path, name, rawLimit);
            int numReturned = 0;
            for (NodeDocument doc : docs) {
                numReturned++;
                // filter out deleted children
                String p = Utils.getPathFromId(doc.getId());
                DocumentNodeState child = getNode(p, rev);
                if (child == null) {
                    continue;
                }
                if (c.children.size() < limit) {
                    // add to children until limit is reached
                    c.children.add(p);
                } else {
                    // enough collected and we know there are more
                    c.hasMore = true;
                    return c;
                }
            }
            // if we get here we have less than or equal the requested children
            if (numReturned < rawLimit) {
                // fewer documents returned than requested
                // -> no more documents
                c.hasMore = false;
                return c;
            }
            // double rawLimit for next round
            rawLimit = (int) Math.min(((long) rawLimit) * 2, Integer.MAX_VALUE);
        }
    }
    String diff(@Nonnull final String fromRevisionId,
                @Nonnull final String toRevisionId,
                @Nonnull final String path) throws MicroKernelException {
        if (fromRevisionId.equals(toRevisionId)) {
            return "";
        }
        Revision fromRev = Revision.fromString(fromRevisionId);
        Revision toRev = Revision.fromString(toRevisionId);
        final DocumentNodeState from = getNode(path, fromRev);
        final DocumentNodeState to = getNode(path, toRev);
        if (from == null || to == null) {
            // TODO implement correct behavior if the node does't/didn't exist
            String msg = String.format("Diff is only supported if the node exists in both cases. " +
                    "Node [%s], fromRev [%s] -> %s, toRev [%s] -> %s",
                    path, fromRev, from != null, toRev, to != null);
            throw new MicroKernelException(msg);
        }
        PathRev key = diffCacheKey(path, fromRev, toRev);
        try {
            JsopWriter writer = new JsopStream();
            diffProperties(from, to, writer);
            return writer.toString() + diffCache.get(key, new Callable<StringValue>() {
                @Override
                public StringValue call() throws Exception {
                    return new StringValue(diffImpl(from, to));
                }
            });
        } catch (ExecutionException e) {
            if (e.getCause() instanceof MicroKernelException) {
                throw (MicroKernelException) e.getCause();
            } else {
                throw new MicroKernelException(e.getCause());
            }
        }
    }
    Iterable<DocumentNodeState> getChildNodes(final @Nonnull DocumentNodeState parent,
                                              final @Nullable String name,
                                              final int limit) {
        // Preemptive check. If we know there are no children then
        // return straight away
        if (checkNotNull(parent).hasNoChildren()) {
            return Collections.emptyList();
        }

        final Revision readRevision = parent.getLastRevision();
        return Iterables.transform(getChildren(parent, name, limit).children,
                new Function<String, DocumentNodeState>() {
            @Override
            public DocumentNodeState apply(String input) {
                return getNode(input, readRevision);
            }
        });
    }
    private String diffImpl(DocumentNodeState from, DocumentNodeState to)
            throws MicroKernelException {
        JsopWriter w = new JsopStream();
        diffProperties(from, to, w);
        // TODO this does not work well for large child node lists
        // use a document store index instead
        int max = MANY_CHILDREN_THRESHOLD;
        DocumentNodeState.Children fromChildren, toChildren;
        fromChildren = getChildren(from, null, max);
        toChildren = getChildren(to, null, max);
        if (!fromChildren.hasMore && !toChildren.hasMore) {
            diffFewChildren(w, fromChildren, from.getLastRevision(),
                    toChildren, to.getLastRevision());
        } else {
            if (FAST_DIFF) {
                diffManyChildren(w, from.getPath(),
                        from.getLastRevision(), to.getLastRevision());
            } else {
                max = Integer.MAX_VALUE;
                fromChildren = getChildren(from, null, max);
                toChildren = getChildren(to, null, max);
                diffFewChildren(w, fromChildren, from.getLastRevision(),
                        toChildren, to.getLastRevision());
            }
        }
        return w.toString();
    }
    private void diffFewChildren(JsopWriter w, DocumentNodeState.Children fromChildren, Revision fromRev, DocumentNodeState.Children toChildren, Revision toRev) {
        Set<String> childrenSet = Sets.newHashSet(toChildren.children);
        for (String n : fromChildren.children) {
            if (!childrenSet.contains(n)) {
                w.tag('-').value(n).newline();
            } else {
                DocumentNodeState n1 = getNode(n, fromRev);
                DocumentNodeState n2 = getNode(n, toRev);
                // this is not fully correct:
                // a change is detected if the node changed recently,
                // even if the revisions are well in the past
                // if this is a problem it would need to be changed
                checkNotNull(n1, "Node at [%s] not found for fromRev [%s]", n, fromRev);
                checkNotNull(n2, "Node at [%s] not found for toRev [%s]", n, toRev);
                if (!n1.getId().equals(n2.getId())) {
                    w.tag('^').key(n).object().endObject().newline();
                }
            }
        }
        childrenSet = Sets.newHashSet(fromChildren.children);
        for (String n : toChildren.children) {
            if (!childrenSet.contains(n)) {
                w.tag('+').key(n).object().endObject().newline();
            }
        }
    }
