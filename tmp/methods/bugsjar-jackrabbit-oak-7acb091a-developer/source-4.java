    private static boolean markCommitRoot(@Nonnull Map<String, Object> document,
                                          @Nonnull String revision,
                                          @Nonnull DocumentStore store) {
        @SuppressWarnings("unchecked")
        Map<String, Integer> commitRoots = (Map<String, Integer>) document.get(UpdateOp.COMMIT_ROOT);
        if (commitRoots != null) {
            Integer depth = commitRoots.get(revision);
            if (depth != null) {
                String p = Utils.getPathFromId((String) document.get(UpdateOp.ID));
                String commitRootPath = PathUtils.getAncestorPath(p, PathUtils.getDepth(p) - depth);
                UpdateOp op = new UpdateOp(commitRootPath,
                        Utils.getIdFromPath(commitRootPath), false);
                op.setMapEntry(UpdateOp.COLLISIONS, revision, true);
                // TODO: detect concurrent commit of previously un-merged changes
                // TODO: check _commitRoot for revision is not 'true'
                store.createOrUpdate(DocumentStore.Collection.NODES, op);
                LOG.debug("Marked collision on: {} for {} ({})",
                        new Object[]{commitRootPath, p, revision});
                return true;
            }
        }
        return false;
    }
    boolean mark(DocumentStore store) {
        if (markCommitRoot(document, theirRev, store)) {
            return true;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> revisions = (Map<String, String>) document.get(UpdateOp.REVISIONS);
        if (revisions.containsKey(theirRev)) {
            String value = revisions.get(theirRev);
            if ("true".equals(value)) {
                // their commit wins, we have to mark ourRev
                Map<String, Object> newDoc = Utils.newMap();
                Utils.deepCopyMap(document, newDoc);
                MemoryDocumentStore.applyChanges(newDoc, ourOp);
                if (markCommitRoot(newDoc, ourRev, store)) {
                    return true;
                }
            }
        }
        return true;
    }
    abstract void uncommittedModification(Revision uncommitted);
    private void createOrUpdateNode(DocumentStore store, UpdateOp op) {
        Map<String, Object> map = store.createOrUpdate(Collection.NODES, op);
        if (baseRevision != null) {
            final AtomicReference<List<Revision>> collisions = new AtomicReference<List<Revision>>();
            Revision newestRev = mk.getNewestRevision(map, revision,
                    new CollisionHandler() {
                @Override
                void uncommittedModification(Revision uncommitted) {
                    if (collisions.get() == null) {
                        collisions.set(new ArrayList<Revision>());
                    }
                    collisions.get().add(uncommitted);
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
                        "; document:\n" + map.toString().replaceAll(", _", ",\n_").replaceAll("}, ", "},\n") + 
                        ",\nrevision order:\n" + mk.getRevisionComparator();
                throw new MicroKernelException(conflictMessage);
            }
            // if we get here the modification was successful
            // -> check for collisions and conflict (concurrent updates
            // on a node are possible if property updates do not overlap)
            if (collisions.get() != null && isConflicting(map, op)) {
                for (Revision r : collisions.get()) {
                    // mark collisions on commit root
                    Collision c = new Collision(map, r, op, revision);
                    boolean success = c.mark(store);
                    if (!success) {
                        // TODO: fail this commit
                    }
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
                        handler.uncommittedModification(propRev);
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
    public static <K> void deepCopyMap(Map<K, Object> source, Map<K, Object> target) {
        for (Entry<K, Object> e : source.entrySet()) {
            Object value = e.getValue();
            if (value instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> old = (Map<Object, Object>) value;
                Map<Object, Object> c = newMap();
                deepCopyMap(old, c);
                value = c;
            }
            target.put(e.getKey(), value);
        }
    }
