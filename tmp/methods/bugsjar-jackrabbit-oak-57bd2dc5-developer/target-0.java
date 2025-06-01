    private boolean isCommitted(@Nonnull RevisionContext context,
                                @Nonnull Revision revision,
                                @Nullable String commitValue,
                                @Nonnull Revision readRevision) {
        if (commitValue == null) {
            commitValue = getCommitValue(revision);
        }
        if (commitValue == null) {
            return false;
        }
        if (Utils.isCommitted(commitValue)) {
            if (context.getBranches().getBranch(readRevision) == null
                    && !readRevision.isBranch()) {
                // resolve commit revision
                revision = Utils.resolveCommitRevision(revision, commitValue);
                // readRevision is not from a branch
                // compare resolved revision as is
                return !isRevisionNewer(context, revision, readRevision);
            } else {
                // on same merged branch?
                if (commitValue.equals(getCommitValue(readRevision.asTrunkRevision()))) {
                    // compare unresolved revision
                    return !isRevisionNewer(context, revision, readRevision);
                }
            }
        } else {
            // branch commit (not merged)
            if (Revision.fromString(commitValue).getClusterId() != context.getClusterId()) {
                // this is an unmerged branch commit from another cluster node,
                // hence never visible to us
                return false;
            }
        }
        return includeRevision(context, Utils.resolveCommitRevision(revision, commitValue), readRevision);
    }
