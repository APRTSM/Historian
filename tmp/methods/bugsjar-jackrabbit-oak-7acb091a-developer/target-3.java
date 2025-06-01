    private static void throwNoCommitRootException(@Nonnull String revision,
                                                   @Nonnull Map<String, Object> document)
                                                           throws MicroKernelException {
        throw new MicroKernelException("No commit root for revision: "
                + revision + ", document: " + Utils.formatDocument(document));
    }
    void mark(DocumentStore store) throws MicroKernelException {
        // first try to mark their revision
        if (markCommitRoot(document, theirRev, store)) {
            return;
        }
        // their commit wins, we have to mark ourRev
        Map<String, Object> newDoc = Utils.newMap();
        Utils.deepCopyMap(document, newDoc);
        MemoryDocumentStore.applyChanges(newDoc, ourOp);
        if (!markCommitRoot(newDoc, ourRev, store)) {
            throw new MicroKernelException("Unable to annotate our revision "
                    + "with collision marker. Our revision: " + ourRev
                    + ", document:\n" + Utils.formatDocument(newDoc));
        }
    }
    private static boolean markCommitRoot(@Nonnull Map<String, Object> document,
                                          @Nonnull String revision,
                                          @Nonnull DocumentStore store) {
        String p = Utils.getPathFromId((String) document.get(UpdateOp.ID));
        String commitRootPath = null;
        // first check if we can mark the commit with the given revision
        @SuppressWarnings("unchecked")
        Map<String, String> revisions = (Map<String, String>) document.get(UpdateOp.REVISIONS);
        if (revisions != null && revisions.containsKey(revision)) {
            String value = revisions.get(revision);
            if ("true".equals(value)) {
                // already committed
                return false;
            } else {
                // node is also commit root, but not yet committed
                // i.e. a branch commit, which is not yet merged
                commitRootPath = p;
            }
        } else {
            // next look at commit root
            @SuppressWarnings("unchecked")
            Map<String, Integer> commitRoots = (Map<String, Integer>) document.get(UpdateOp.COMMIT_ROOT);
            if (commitRoots != null) {
                Integer depth = commitRoots.get(revision);
                if (depth != null) {
                    commitRootPath = PathUtils.getAncestorPath(p, PathUtils.getDepth(p) - depth);
                } else {
                    throwNoCommitRootException(revision, document);
                }
            } else {
                throwNoCommitRootException(revision, document);
            }
        }
        // at this point we have a commitRootPath
        UpdateOp op = new UpdateOp(commitRootPath,
                Utils.getIdFromPath(commitRootPath), false);
        document = store.find(Collection.NODES, op.getKey());
        // check commit status of revision
        if (isCommitted(revision, document)) {
            return false;
        }
        op.setMapEntry(UpdateOp.COLLISIONS, revision, true);
        document = store.createOrUpdate(DocumentStore.Collection.NODES, op);
        // check again on old document right before our update was applied
        if (isCommitted(revision, document)) {
            return false;
        }
        // otherwise collision marker was set successfully
        LOG.debug("Marked collision on: {} for {} ({})",
                new Object[]{commitRootPath, p, revision});
        return true;
    }
    private static boolean isCommitted(String revision, Map<String, Object> document) {
        @SuppressWarnings("unchecked")
        Map<String, String> revisions = (Map<String, String>) document.get(UpdateOp.REVISIONS);
        if (revisions != null && revisions.containsKey(revision)) {
            String value = revisions.get(revision);
            return "true".equals(value);
        }
        return false;
    }
    abstract void concurrentModification(Revision other);
    private void createOrUpdateNode(DocumentStore store, UpdateOp op) {
        Map<String, Object> map = store.createOrUpdate(Collection.NODES, op);
        if (baseRevision != null) {
            final AtomicReference<List<Revision>> collisions = new AtomicReference<List<Revision>>();
            Revision newestRev = mk.getNewestRevision(map, revision,
                    new CollisionHandler() {
                @Override
                void concurrentModification(Revision other) {
                    if (collisions.get() == null) {
                        collisions.set(new ArrayList<Revision>());
                    }
                    collisions.get().add(other);
                }
            });
            String conflictMessage = null;
            if (newestRev == null) {
                if (op.isDelete || !op.isNew) {
                    conflictMessage = "The node " + 
                            op.path + " does not exist or is already deleted";
                }
            } else {
                if (op.isNew) {
                    conflictMessage = "The node " + 
                            op.path + " was already added in revision\n" + 
                            newestRev;
                } else if (mk.isRevisionNewer(newestRev, baseRevision)
                        && (op.isDelete || isConflicting(map, op))) {
                    conflictMessage = "The node " + 
                            op.path + " was changed in revision\n" + newestRev +
                            ", which was applied after the base revision\n" + 
                            baseRevision;
                }
            }
            if (conflictMessage != null) {
                conflictMessage += ", before\n" + revision + 
                        "; document:\n" + Utils.formatDocument(map) +
                        ",\nrevision order:\n" + mk.getRevisionComparator();
                throw new MicroKernelException(conflictMessage);
            }
            // if we get here the modification was successful
            // -> check for collisions and conflict (concurrent updates
            // on a node are possible if property updates do not overlap)
            if (collisions.get() != null && isConflicting(map, op)) {
                for (Revision r : collisions.get()) {
                    // mark collisions on commit root
                    new Collision(map, r, op, revision).mark(store);
                }
            }
        }

        int size = Utils.estimateMemoryUsage(map);
        if (size > MAX_DOCUMENT_SIZE) {
            UpdateOp[] split = splitDocument(map);
            
            // TODO check if the new main document is actually smaller;
            // otherwise, splitting doesn't make sense
            
            // the old version
            UpdateOp old = split[0];
            if (old != null) {
                store.createOrUpdate(Collection.NODES, old);
            }
            
            // the (shrunken) main document
            UpdateOp main = split[1];
            if (main != null) {
                store.createOrUpdate(Collection.NODES, main);
            }
        }
    }
    @Nullable Revision getNewestRevision(Map<String, Object> nodeMap,
                                         Revision changeRev, CollisionHandler handler) {
        if (nodeMap == null) {
            return null;
        }
        SortedSet<String> revisions = new TreeSet<String>(Collections.reverseOrder());
        if (nodeMap.containsKey(UpdateOp.REVISIONS)) {
            revisions.addAll(((Map<String, String>) nodeMap.get(UpdateOp.REVISIONS)).keySet());
        }
        if (nodeMap.containsKey(UpdateOp.COMMIT_ROOT)) {
            revisions.addAll(((Map<String, Integer>) nodeMap.get(UpdateOp.COMMIT_ROOT)).keySet());
        }
        Map<String, String> deletedMap = (Map<String, String>) nodeMap
                .get(UpdateOp.DELETED);
        if (deletedMap != null) {
            revisions.addAll(deletedMap.keySet());
        }
        Revision newestRev = null;
        for (String r : revisions) {
            Revision propRev = Revision.fromString(r);
            if (isRevisionNewer(propRev, changeRev)) {
                // we have seen a previous change from another cluster node
                // (which might be conflicting or not) - we need to make
                // sure this change is visible from now on
                publishRevision(propRev, changeRev);
            }
            if (newestRev == null || isRevisionNewer(propRev, newestRev)) {
                if (!propRev.equals(changeRev)) {
                    if (!isValidRevision(
                            propRev, changeRev, nodeMap, new HashSet<Revision>())) {
                        handler.concurrentModification(propRev);
                    } else {
                        newestRev = propRev;
                    }
                }
            }
        }
        if (newestRev == null) {
            return null;
        }
        if (deletedMap != null) {
            String value = deletedMap.get(newestRev.toString());
            if ("true".equals(value)) {
                // deleted in the newest revision
                return null;
            }
        }
        return newestRev;
    }
