    private void applyToDocumentStore(Revision baseBranchRevision) {
        // the value in _revisions.<revision> property of the commit root node
        // regular commits use "c", which makes the commit visible to
        // other readers. branch commits use the base revision to indicate
        // the visibility of the commit
        String commitValue = baseBranchRevision != null ? baseBranchRevision.toString() : "c";
        DocumentStore store = nodeStore.getDocumentStore();
        String commitRootPath = null;
        if (baseBranchRevision != null) {
            // branch commits always use root node as commit root
            commitRootPath = "/";
        }
        ArrayList<UpdateOp> newNodes = new ArrayList<UpdateOp>();
        ArrayList<UpdateOp> changedNodes = new ArrayList<UpdateOp>();
        // operations are added to this list before they are executed,
        // so that all operations can be rolled back if there is a conflict
        ArrayList<UpdateOp> opLog = new ArrayList<UpdateOp>();

        // Compute the commit root
        for (String p : operations.keySet()) {
            markChanged(p);
            if (commitRootPath == null) {
                commitRootPath = p;
            } else {
                while (!PathUtils.isAncestor(commitRootPath, p)) {
                    commitRootPath = PathUtils.getParentPath(commitRootPath);
                    if (denotesRoot(commitRootPath)) {
                        break;
                    }
                }
            }
        }

        // push branch changes to journal
        if (baseBranchRevision != null) {
            // store as external change
            JournalEntry doc = JOURNAL.newDocument(store);
            doc.modified(modifiedNodes);
            Revision r = revision.asBranchRevision();
            store.create(JOURNAL, singletonList(doc.asUpdateOp(r)));
        }

        int commitRootDepth = PathUtils.getDepth(commitRootPath);
        // check if there are real changes on the commit root
        boolean commitRootHasChanges = operations.containsKey(commitRootPath);
        // create a "root of the commit" if there is none
        UpdateOp commitRoot = getUpdateOperationForNode(commitRootPath);
        for (String p : operations.keySet()) {
            UpdateOp op = operations.get(p);
            if (op.isNew()) {
                NodeDocument.setDeleted(op, revision, false);
            }
            if (op == commitRoot) {
                if (!op.isNew() && commitRootHasChanges) {
                    // commit root already exists and this is an update
                    changedNodes.add(op);
                }
            } else {
                NodeDocument.setCommitRoot(op, revision, commitRootDepth);
                if (op.isNew()) {
                    newNodes.add(op);
                } else {
                    changedNodes.add(op);
                }
            }
        }
        if (changedNodes.size() == 0 && commitRoot.isNew()) {
            // no updates and root of commit is also new. that is,
            // it is the root of a subtree added in a commit.
            // so we try to add the root like all other nodes
            NodeDocument.setRevision(commitRoot, revision, commitValue);
            newNodes.add(commitRoot);
        }
        try {
            if (newNodes.size() > 0) {
                // set commit root on new nodes
                if (!store.create(NODES, newNodes)) {
                    // some of the documents already exist:
                    // try to apply all changes one by one
                    for (UpdateOp op : newNodes) {
                        if (op == commitRoot) {
                            // don't write the commit root just yet
                            // (because there might be a conflict)
                            NodeDocument.unsetRevision(commitRoot, revision);
                        }
                        changedNodes.add(op);
                    }
                    newNodes.clear();
                }
            }
            for (UpdateOp op : changedNodes) {
                // set commit root on changed nodes. this may even apply
                // to the commit root. the _commitRoot entry is removed
                // again when the _revisions entry is set at the end
                NodeDocument.setCommitRoot(op, revision, commitRootDepth);
                opLog.add(op);
                createOrUpdateNode(store, op);
            }
            // finally write the commit root, unless it was already written
            // with added nodes (the commit root might be written twice,
            // first to check if there was a conflict, and only then to commit
            // the revision, with the revision property set)
            if (changedNodes.size() > 0 || !commitRoot.isNew()) {
                // set revision to committed
                NodeDocument.setRevision(commitRoot, revision, commitValue);
                if (commitRootHasChanges) {
                    // remove previously added commit root
                    NodeDocument.removeCommitRoot(commitRoot, revision);
                }
                opLog.add(commitRoot);
                if (baseBranchRevision == null) {
                    // create a clone of the commitRoot in order
                    // to set isNew to false. If we get here the
                    // commitRoot document already exists and
                    // only needs an update
                    UpdateOp commit = commitRoot.copy();
                    commit.setNew(false);
                    // only set revision on commit root when there is
                    // no collision for this commit revision
                    commit.containsMapEntry(COLLISIONS, revision, false);
                    NodeDocument before = nodeStore.updateCommitRoot(commit);
                    if (before == null) {
                        String msg = "Conflicting concurrent change. " +
                                "Update operation failed: " + commitRoot;
                        throw new DocumentStoreException(msg);
                    } else {
                        // if we get here the commit was successful and
                        // the commit revision is set on the commitRoot
                        // document for this commit.
                        // now check for conflicts/collisions by other commits.
                        // use original commitRoot operation with
                        // correct isNew flag.
                        checkConflicts(commitRoot, before);
                        checkSplitCandidate(before);
                    }
                } else {
                    // this is a branch commit, do not fail on collisions now
                    // trying to merge the branch will fail later
                    createOrUpdateNode(store, commitRoot);
                }
                operations.put(commitRootPath, commitRoot);
            }
        } catch (DocumentStoreException e) {
            rollback(newNodes, opLog, commitRoot);
            throw e;
        }
    }
