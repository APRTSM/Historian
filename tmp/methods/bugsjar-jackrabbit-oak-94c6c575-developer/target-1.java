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
            return nodeStore.reset(branch, ancestor).toString();
        } catch (DocumentStoreException e) {
            throw new DocumentStoreException(e);
        }
    }
    RevisionVector reset(@Nonnull RevisionVector branchHead,
                         @Nonnull RevisionVector ancestor) {
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
        if (!b.containsCommit(ancestor.getBranchRevision())
                && !b.getBase().asBranchRevision(getClusterId()).equals(ancestor)) {
            throw new DocumentStoreException(ancestor + " is not " +
                    "an ancestor revision of " + branchHead);
        }
        // tailSet is inclusive -> use an ancestorRev with a
        // counter incremented by one to make the call exclusive
        Revision ancestorRev = ancestor.getBranchRevision();
        ancestorRev = new Revision(ancestorRev.getTimestamp(),
                ancestorRev.getCounter() + 1, ancestorRev.getClusterId(), true);
        List<Revision> revs = newArrayList(b.getCommits().tailSet(ancestorRev));
        if (revs.isEmpty()) {
            // trivial
            return branchHead;
        }
        UpdateOp rootOp = new UpdateOp(Utils.getIdFromPath("/"), false);
        // reset each branch commit in reverse order
        Map<String, UpdateOp> operations = Maps.newHashMap();
        for (Revision r : reverse(revs)) {
            NodeDocument.removeCollision(rootOp, r.asTrunkRevision());
            NodeDocument.removeRevision(rootOp, r.asTrunkRevision());
            operations.clear();
            BranchCommit bc = b.getCommit(r);
            if (bc.isRebase()) {
                continue;
            }
            getRoot(bc.getBase().update(r))
                    .compareAgainstBaseState(getRoot(bc.getBase()),
                            new ResetDiff(r.asTrunkRevision(), operations));
            // apply reset operations
            for (UpdateOp op : operations.values()) {
                store.findAndUpdate(Collection.NODES, op);
            }
        }
        store.findAndUpdate(Collection.NODES, rootOp);
        // clean up in-memory branch data
        for (Revision r : revs) {
            b.removeCommit(r);
        }
        return ancestor;
    }
