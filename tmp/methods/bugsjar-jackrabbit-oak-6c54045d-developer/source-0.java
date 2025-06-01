    private boolean exists() {
        // No need to check the base state if write state is null. The fact that we have this
        // builder instance proofs that this child existed at some point as it must have been
        // retrieved from the base state.
        return isRoot() || parent.writeState == null || parent.writeState.hasChildNode(name);
    }
    private MutableNodeState write(long newRevision, boolean reconnect) {
        // make sure that all revision numbers up to the root gets updated
        if (!isRoot()) {
            checkState(reconnect || exists(), "This node has been removed");
            parent.write(newRevision, reconnect);
        }

        if (writeState == null || revision != root.revision) {
            assert(!isRoot()); // root never gets here since revision == root.revision

            // The builder could have been reset, need to re-get base state
            baseState = parent.getBaseState(name);

            writeState = parent.getWriteState(name);
            if (writeState == null) {
                if (exists()) {
                    assert baseState != null;
                    writeState = new MutableNodeState(baseState);
                }
                else {
                    writeState = new MutableNodeState(null);
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
    private boolean updateReadState() {
        if (revision != root.revision) {
            assert(!isRoot()); // root never gets here since revision == root.revision
            if (!exists()) {
                return false;
            }
            parent.updateReadState();

            // The builder could have been reset, need to re-get base state
            baseState = parent.getBaseState(name);

            // ... same for the write state
            writeState = parent.getWriteState(name);

            revision = root.revision;
        }
        return writeState != null || baseState != null;
    }
