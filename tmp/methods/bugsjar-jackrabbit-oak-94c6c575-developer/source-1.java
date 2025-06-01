    public String reset(@Nonnull String branchRevisionId,
                        @Nonnull String ancestorRevisionId)
            throws DocumentStoreException {
        RevisionVector branch = RevisionVector.fromString(branchRevisionId);
        if (!branch.isBranch()) {
            throw new DocumentStoreException("Not a branch revision: " + branchRevisionId);
        }
        RevisionVector ancestor = RevisionVector.fromString(ancestorRevisionId);
        if (!ancestor.isBranch()) {
            throw new DocumentStoreException("Not a branch revision: " + ancestorRevisionId);
        }
        try {
            return nodeStore.reset(branch, ancestor, null).toString();
        } catch (DocumentStoreException e) {
            throw new DocumentStoreException(e);
        }
    }
    RevisionVector reset(@Nonnull RevisionVector branchHead,
                         @Nonnull RevisionVector ancestor,
                         @Nullable DocumentNodeStoreBranch branch) {
        checkNotNull(branchHead);
        checkNotNull(ancestor);
        Branch b = getBranches().getBranch(branchHead);
        if (b == null) {
            throw new DocumentStoreException("Empty branch cannot be reset");
        }
        if (!b.getCommits().last().equals(branchHead.getRevision(getClusterId()))) {
            throw new DocumentStoreException(branchHead + " is not the head " +
                    "of a branch");
        }
        if (!b.containsCommit(ancestor.getBranchRevision())) {
            throw new DocumentStoreException(ancestor + " is not " +
                    "an ancestor revision of " + branchHead);
        }
        if (branchHead.equals(ancestor)) {
            // trivial
            return branchHead;
        }
        boolean success = false;
        Commit commit = newCommit(branchHead, branch);
        try {
            Iterator<Revision> it = b.getCommits().tailSet(ancestor.getBranchRevision()).iterator();
            // first revision is the ancestor (tailSet is inclusive)
            // do not undo changes for this revision
            it.next();
            Map<String, UpdateOp> operations = Maps.newHashMap();
            if (it.hasNext()) {
                Revision reset = it.next();
                // TODO: correct?
                getRoot(b.getCommit(reset).getBase().update(reset))
                        .compareAgainstBaseState(getRoot(ancestor),
                                new ResetDiff(reset.asTrunkRevision(), operations));
                UpdateOp rootOp = operations.get("/");
                if (rootOp == null) {
                    rootOp = new UpdateOp(Utils.getIdFromPath("/"), false);
                    NodeDocument.setModified(rootOp, commit.getRevision());
                    operations.put("/", rootOp);
                }
                NodeDocument.removeCollision(rootOp, reset.asTrunkRevision());
                NodeDocument.removeRevision(rootOp, reset.asTrunkRevision());
            }
            // update root document first
            if (store.findAndUpdate(Collection.NODES, operations.get("/")) != null) {
                // clean up in-memory branch data
                // first revision is the ancestor (tailSet is inclusive)
                List<Revision> revs = Lists.newArrayList(b.getCommits().tailSet(ancestor.getBranchRevision()));
                for (Revision r : revs.subList(1, revs.size())) {
                    b.removeCommit(r);
                }
                // successfully updating the root document can be considered
                // as success because the changes are not marked as committed
                // anymore
                success = true;
            }
            operations.remove("/");
            // update remaining documents
            for (UpdateOp op : operations.values()) {
                store.findAndUpdate(Collection.NODES, op);
            }
        } finally {
            if (!success) {
                canceled(commit);
            } else {
                done(commit, true, null);
            }
        }
        return ancestor;
    }
