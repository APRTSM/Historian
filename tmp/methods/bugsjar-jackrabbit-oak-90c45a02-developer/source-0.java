    private MutableNodeState write(long newRevision, boolean skipRemovedCheck) {
        // make sure that all revision numbers up to the root gets updated
        if (!isRoot()) {
            checkState(skipRemovedCheck || !parent.removed(name));
            parent.write(newRevision, skipRemovedCheck);
        }

        if (writeState == null || revision != root.revision) {
            assert(!isRoot()); // root never gets here since revision == root.revision

            // The builder could have been reset, need to re-get base state
            baseState = parent.getBaseState(name);

            writeState = parent.getWriteState(name);
            if (writeState == null) {
                if (parent.removed(name)) {
                    writeState = new MutableNodeState(null);
                }
                else {
                    writeState = new MutableNodeState(baseState);
                }
                assert parent.writeState != null; // guaranteed by called parent.write()
                parent.writeState.nodes.put(name, writeState);
            }
        }

        revision = newRevision;
        assert classInvariants();
        assert writeState != null;
        return writeState;
    }
    private boolean removed(String name) {
        return writeState != null && writeState.isRemoved(name);
    }
    private NodeState read() {
        if (revision != root.revision) {
            assert(!isRoot()); // root never gets here since revision == root.revision
            checkState(!parent.removed(name), "This node has already been removed");
            parent.read();

            // The builder could have been reset, need to re-get base state
            baseState = parent.getBaseState(name);

            // ... same for the write state
            writeState = parent.getWriteState(name);

            revision = root.revision;
        }

        assert classInvariants();

        if (writeState != null) {
            return writeState;
        } else {
            return baseState;
        }
    }
