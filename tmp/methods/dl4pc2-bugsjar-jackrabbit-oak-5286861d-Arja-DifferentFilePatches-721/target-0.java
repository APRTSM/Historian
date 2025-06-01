    public Id /* new revId */ doCommit(boolean createBranch) throws Exception {
        if (stagedTree.isEmpty() && !createBranch) {
            // nothing to commit
            return baseRevId;
        }

        StoredCommit baseCommit = store.getCommit(baseRevId);
        if (createBranch && baseCommit.getBranchRootId() != null) {
            throw new Exception("cannot branch off a private branch");
        }

        boolean privateCommit = createBranch || baseCommit.getBranchRootId() != null;

        if (!privateCommit) {
            Id currentHead = store.getHeadCommitId();
            if (!currentHead.equals(baseRevId)) {
                // todo gracefully handle certain conflicts (e.g. changes on moved sub-trees, competing deletes etc)
                // update base revision to more recent current head
                baseRevId = currentHead;
                // reset staging area
                stagedTree.reset(baseRevId);
                // replay change log on new base revision
                for (Change change : changeLog) {
                    change.apply();
                }
            }
        }

        RevisionStore.PutToken token = store.createPutToken();
        Id rootNodeId =
                changeLog.isEmpty() ? baseCommit.getRootNodeId() : stagedTree.persist(token);

        Id newRevId;

        if (!privateCommit) {
            store.lockHead();
            try {
                Id currentHead = store.getHeadCommitId();
                if (!currentHead.equals(baseRevId)) {
                    // there's a more recent head revision
                    // perform a three-way merge
                    rootNodeId = stagedTree.merge(store.getNode(rootNodeId), currentHead, baseRevId, token);
                    // update base revision to more recent current head
                    baseRevId = currentHead;
                }

                if (store.getCommit(currentHead).getRootNodeId().equals(rootNodeId)) {
                    // the commit didn't cause any changes,
                    // no need to create new commit object/update head revision
                    return currentHead;
                }
                // persist new commit
                MutableCommit newCommit = new MutableCommit();
                newCommit.setParentId(baseRevId);
                newCommit.setCommitTS(System.currentTimeMillis());
                newCommit.setMsg(msg);
                StringBuilder diff = new StringBuilder();
                for (Change change : changeLog) {
                    if (diff.length() > 0) {
                        diff.append('\n');
                    }
                    diff.append(change.asDiff());
                }
                newCommit.setChanges(diff.toString());
                newCommit.setRootNodeId(rootNodeId);
                newCommit.setBranchRootId(null);
                newRevId = store.putHeadCommit(token, newCommit, null, null);
            } finally {
                store.unlockHead();
            }
        } else {
            // private commit/branch
            MutableCommit newCommit = new MutableCommit();
            newCommit.setParentId(baseCommit.getId());
            newCommit.setMsg(msg);
            StringBuilder diff = new StringBuilder();
            for (Change change : changeLog) {
                if (diff.length() > 0) {
                    diff.append('\n');
                }
                diff.append(change.asDiff());
            }
            newCommit.setChanges(diff.toString());
            newCommit.setRootNodeId(rootNodeId);
            newRevId = store.putCommit(token, newCommit);
        }

        // reset instance
        stagedTree.reset(newRevId);
        changeLog.clear();

        return newRevId;
    }
