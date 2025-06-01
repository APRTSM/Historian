    Revision getNewestRevision(final RevisionContext context,
                               final RevisionVector baseRev,
                               final Revision changeRev,
                               final Branch branch,
                               final Set<Revision> collisions) {
        checkArgument(!baseRev.isBranch() || branch != null,
                "Branch must be non-null if baseRev is a branch revision");
        RevisionVector head = context.getHeadRevision();
        RevisionVector lower = branch != null ? branch.getBase() : baseRev;
        // the clusterIds to check when walking the changes
        Set<Integer> clusterIds = Collections.emptySet();
        if (!getPreviousRanges().isEmpty()) {
            clusterIds = Sets.newHashSet();
            for (Revision prevRev : getPreviousRanges().keySet()) {
                if (lower.isRevisionNewer(prevRev) ||
                        equal(prevRev, lower.getRevision(prevRev.getClusterId()))) {
                    clusterIds.add(prevRev.getClusterId());
                }
            }
            if (!clusterIds.isEmpty()) {
                // add clusterIds of local changes as well
                for (Revision r : getLocalCommitRoot().keySet()) {
                    clusterIds.add(r.getClusterId());
                }
                for (Revision r : getLocalRevisions().keySet()) {
                    clusterIds.add(r.getClusterId());
                }
            }
        }
        // if we don't have clusterIds, we can use the local changes only
        boolean fullScan = true;
        Iterable<Revision> changes = Iterables.mergeSorted(
                ImmutableList.of(
                        getLocalRevisions().keySet(),
                        getLocalCommitRoot().keySet()),
                getLocalRevisions().comparator()
        );
        if (!clusterIds.isEmpty()) {
            // there are some previous documents that potentially
            // contain changes after 'lower' revision vector
            // include previous documents as well (only needed in rare cases)
            fullScan = false;
            changes = Iterables.mergeSorted(
                    ImmutableList.of(
                            changes,
                            getChanges(REVISIONS, lower),
                            getChanges(COMMIT_ROOT, lower)
                    ), getLocalRevisions().comparator()
            );
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
                if (clusterIds.contains(r.getClusterId())
                        && !lower.isRevisionNewer(r)
                        && newestRevs.containsKey(r.getClusterId())) {
                    clusterIds.remove(r.getClusterId());
                    if (clusterIds.isEmpty()) {
                        // all remaining revisions are older than
                        // the lower bound
                        break;
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
                    if (branch.getBase(changeRev).isRevisionNewer(r)) {
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
                            && head.isRevisionNewer(commitRevision)) {
                        // case 4)
                        collisions.add(r);
                    } else if (commitRevision != null // committed
                            && branch == null         // changeRev not on branch
                            && baseRev.isRevisionNewer(r)) {
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
            newestRev = Utils.max(newestRev, r, StableRevisionComparator.INSTANCE);
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
    Iterable<Revision> getAllChanges() {
        RevisionVector empty = new RevisionVector();
        return Iterables.mergeSorted(ImmutableList.of(
                getChanges(REVISIONS, empty),
                getChanges(COMMIT_ROOT, empty)
        ), StableRevisionComparator.REVERSE);
    }
    Iterable<Revision> getChanges(@Nonnull final String property,
                                  @Nonnull final RevisionVector min) {
        Predicate<Revision> p = new Predicate<Revision>() {
            @Override
            public boolean apply(Revision input) {
                return min.isRevisionNewer(input);
            }
        };
        List<Iterable<Revision>> changes = Lists.newArrayList();
        changes.add(abortingIterable(getLocalMap(property).keySet(), p));
        for (Map.Entry<Revision, Range> e : getPreviousRanges().entrySet()) {
            if (min.isRevisionNewer(e.getKey())) {
                final NodeDocument prev = getPreviousDoc(e.getKey(), e.getValue());
                if (prev != null) {
                    changes.add(abortingIterable(prev.getValueMap(property).keySet(), p));
                }
            }
        }
        if (changes.size() == 1) {
            return changes.get(0);
        } else {
            return Iterables.mergeSorted(changes, StableRevisionComparator.REVERSE);
        }
    }
