    Revision getNewestRevision(final RevisionContext context,
                               final Revision baseRev,
                               final Revision changeRev,
                               final Branch branch,
                               final Set<Revision> collisions) {
        checkArgument(!baseRev.isBranch() || branch != null,
                "Branch must be non-null if baseRev is a branch revision");
        Revision head = context.getHeadRevision();
        Revision lower = branch != null ? branch.getBase() : baseRev;
        // the clusterIds to check when walking the changes
        Set<Integer> clusterIds = Collections.emptySet();
        if (!getPreviousRanges().isEmpty()) {
            clusterIds = Sets.newHashSet();
            for (Revision prevRev : getPreviousRanges().keySet()) {
                if (!isRevisionNewer(context, lower, prevRev)) {
                    clusterIds.add(prevRev.getClusterId());
                }
            }
        }
        // if we don't have clusterIds, we can use the local changes only
        boolean fullScan = true;
        Iterable<Revision> changes;
        if (clusterIds.isEmpty()) {
            // baseRev is newer than all previous documents
            changes = Iterables.mergeSorted(
                    ImmutableList.of(
                            getLocalRevisions().keySet(),
                            getLocalCommitRoot().keySet()),
                    getLocalRevisions().comparator());
        } else {
            // include previous documents as well (only needed in rare cases)
            fullScan = false;
            changes = getAllChanges();
            if (LOG.isDebugEnabled()) {
                LOG.debug("getNewestRevision() with changeRev {} on {}, " +
                                "_revisions {}, _commitRoot {}",
                        changeRev, getId(), getLocalRevisions(), getLocalCommitRoot());
            }
        }
        Map<Integer, Revision> newestRevs = Maps.newHashMap();
        Map<Revision, String> validRevisions = Maps.newHashMap();
        for (Revision r : changes) {
            if (r.equals(changeRev)) {
                continue;
            }
            if (!fullScan) {
                // check if we can stop going through changes
                if (clusterIds.contains(r.getClusterId())) {
                    if (isRevisionNewer(context, lower, r)) {
                        clusterIds.remove(r.getClusterId());
                        if (clusterIds.isEmpty()) {
                            // all remaining revisions are older than
                            // the lower bound
                            break;
                        }
                    }
                }
            }
            if (newestRevs.containsKey(r.getClusterId())) {
                // we already found the newest revision for this clusterId
                // from a baseRev point of view
                // we still need to find collisions up to the base
                // of the branch if this is for a commit on a branch
                if (branch != null && !branch.containsCommit(r)) {
                    // change does not belong to the branch
                    if (isRevisionNewer(context, r, branch.getBase())) {
                        // and happened after the base of the branch
                        collisions.add(r);
                    }
                }
            } else {
                // we don't yet have the newest committed change
                // for this clusterId
                // check if change is visible from baseRev
                if (isValidRevision(context, r, null, baseRev, validRevisions)) {
                    // consider for newestRev
                    newestRevs.put(r.getClusterId(), r);
                } else {
                    // not valid means:
                    // 1) 'r' is not committed -> collision
                    // 2) 'r' is on a branch, but not the same as
                    //    changeRev -> collisions
                    // 3) changeRev is on a branch and 'r' is newer than
                    //    the base of the branch -> collision
                    // 4) 'r' is committed but not yet visible to current
                    //    cluster node -> collisions
                    // 5) changeRev is not on a branch, 'r' is committed and
                    //    newer than baseRev -> newestRev

                    NodeDocument commitRoot = getCommitRoot(r);
                    Revision commitRevision = null;
                    if (commitRoot != null) {
                        commitRevision = commitRoot.getCommitRevision(r);
                    }
                    if (commitRevision != null // committed but not yet visible
                            && isRevisionNewer(context, commitRevision, head)) {
                        // case 4)
                        collisions.add(r);
                    } else if (commitRevision != null // committed
                            && branch == null         // changeRev not on branch
                            && isRevisionNewer(context, r, baseRev)) {
                        // case 5)
                        newestRevs.put(r.getClusterId(), r);
                    } else {
                        // remaining cases 1), 2) and 3)
                        collisions.add(r);
                    }
                }
            }

        }
        // select the newest committed change
        Revision newestRev = null;
        for (Revision r : newestRevs.values()) {
            newestRev = Utils.max(newestRev, r, context.getRevisionComparator());
        }

        if (newestRev == null) {
            return null;
        }

        // the local deleted map contains the most recent revisions
        SortedMap<Revision, String> deleted = getLocalDeleted();
        String value = deleted.get(newestRev);
        if (value == null && deleted.headMap(newestRev).isEmpty()) {
            // newestRev is newer than most recent entry in local deleted
            // no need to check previous docs
            return newestRev;
        }

        if (value == null) {
            // get from complete map
            value = getDeleted().get(newestRev);
        }
        if ("true".equals(value)) {
            // deleted in the newest revision
            return null;
        }
        return newestRev;
    }
