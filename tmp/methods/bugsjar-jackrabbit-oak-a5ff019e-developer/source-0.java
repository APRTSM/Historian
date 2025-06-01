    private void checkConflicts(@Nonnull UpdateOp op,
                                @Nullable NodeDocument before)
            throws ConflictException {
        DocumentStore store = nodeStore.getDocumentStore();
        collisions.clear();
        if (baseRevision != null) {
            Revision newestRev = null;
            if (before != null) {
                Revision base = baseRevision;
                if (nodeStore.isDisableBranches()) {
                    base = base.asTrunkRevision();
                }
                newestRev = before.getNewestRevision(
                        nodeStore, base, revision, getBranch(), collisions);
            }
            String conflictMessage = null;
            Revision conflictRevision = newestRev;
            if (newestRev == null) {
                if ((op.isDelete() || !op.isNew()) && isConflicting(before, op)) {
                    conflictMessage = "The node " +
                            op.getId() + " does not exist or is already deleted";
                    if (before != null && !before.getLocalDeleted().isEmpty()) {
                        conflictRevision = before.getLocalDeleted().firstKey();
                    }
                }
            } else {
                if (op.isNew() && isConflicting(before, op)) {
                    conflictMessage = "The node " +
                            op.getId() + " was already added in revision\n" +
                            formatConflictRevision(newestRev);
                } else if (nodeStore.isRevisionNewer(newestRev, baseRevision)
                        && (op.isDelete() || isConflicting(before, op))) {
                    conflictMessage = "The node " +
                            op.getId() + " was changed in revision\n" +
                            formatConflictRevision(newestRev) +
                            ", which was applied after the base revision\n" +
                            baseRevision;
                }
            }
            if (conflictMessage == null) {
                // the modification was successful
                // -> check for collisions and conflict (concurrent updates
                // on a node are possible if property updates do not overlap)
                // TODO: unify above conflict detection and isConflicting()
                if (!collisions.isEmpty() && isConflicting(before, op)) {
                    for (Revision r : collisions) {
                        // mark collisions on commit root
                        Collision c = new Collision(before, r, op, revision);
                        if (c.mark(store).equals(revision)) {
                            // our revision was marked
                            if (baseRevision.isBranch()) {
                                // this is a branch commit. do not fail immediately
                                // merging this branch will fail later.
                            } else {
                                // fail immediately
                                conflictMessage = "The node " +
                                        op.getId() + " was changed in revision\n" +
                                        formatConflictRevision(r) +
                                        ", which was applied after the base revision\n" +
                                        baseRevision;
                                conflictRevision = r;
                            }
                        }
                    }
                }
            }
            if (conflictMessage != null) {
                conflictMessage += ", before\n" + revision;
                if (LOG.isDebugEnabled()) {
                    LOG.debug(conflictMessage  + "; document:\n" +
                            (before == null ? "" : before.format()) +
                            ",\nrevision order:\n" +
                            nodeStore.getRevisionComparator());
                }
                throw new ConflictException(conflictMessage, conflictRevision);
            }
        }
    }
