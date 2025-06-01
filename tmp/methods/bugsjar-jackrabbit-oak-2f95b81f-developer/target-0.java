    Revision rebase(@Nonnull Revision branchHead, @Nonnull Revision base) {
        checkNotNull(branchHead);
        checkNotNull(base);
        // TODO conflict handling
        Branch b = getBranches().getBranch(branchHead);
        if (b == null) {
            // empty branch
            return base.asBranchRevision();
        }
        if (b.getBase(branchHead).equals(base)) {
            return branchHead;
        }
        // add a pseudo commit to make sure current head of branch
        // has a higher revision than base of branch
        Revision head = newRevision().asBranchRevision();
        b.rebase(head, base);
        return head;
    }
