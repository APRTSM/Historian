    private boolean exists() {
        if (isRoot()) {
            return true;
        }
        else if (parent.writeState == null) {
            return parent.baseState != null && parent.baseState.hasChildNode(name);
        }
        else {
            return parent.writeState.hasChildNode(name);
        }
    }
    private boolean updateReadState() {
        if (revision != root.revision) {
            assert(!isRoot()); // root never gets here since revision == root.revision

            if (parent.updateReadState() && exists()) {
                // The builder could have been reset, need to re-get base state
                baseState = parent.getBaseState(name);

                // ... same for the write state
                writeState = parent.getWriteState(name);

                revision = root.revision;
                return true;
            }

            return false;
        }
        return writeState != null || baseState != null;
    }
    private MutableNodeState write(long newRevision, boolean reconnect) {
        // make sure that all revision numbers up to the root gets updated
        if (!isRoot()) {
            parent.write(newRevision, reconnect);
            checkState(reconnect || exists(), "This node has been removed");
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
