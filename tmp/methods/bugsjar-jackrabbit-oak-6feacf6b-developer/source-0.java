    private boolean removed() {
        return !isRoot() && parent.writeState != null &&
                parent.hasBaseState(name) && !parent.writeState.hasChildNode(name);
    }
    public NodeBuilder setNode(String name, NodeState state) {
        write();

        MutableNodeState childState = getWriteState(name);
        if (childState == null) {
            writeState.nodes.remove(name);
            childState = createChildBuilder(name).write();
        }
        childState.reset(state);

        updated();
        return this;
    }
    private MutableNodeState write(long newRevision, boolean skipRemovedCheck) {
        // make sure that all revision numbers up to the root gets updated
        if (!isRoot()) {
            checkState(skipRemovedCheck || !removed());
            parent.write(newRevision, skipRemovedCheck);
        }

        if (writeState == null || revision != root.revision) {
            assert(!isRoot()); // root never gets here since revision == root.revision

            // The builder could have been reset, need to re-get base state
            baseState = parent.getBaseState(name);

            writeState = parent.getWriteState(name);
            if (writeState == null) {
                if (removed()) {
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
    private NodeState read() {
        if (revision != root.revision) {
            assert(!isRoot()); // root never gets here since revision == root.revision
            checkState(!removed(), "This node has already been removed");
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
