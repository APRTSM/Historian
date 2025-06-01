    public boolean remove() {
        if (isRemoved()) {
            throw new IllegalStateException("Cannot remove removed tree");
        }

        if (!isRoot() && parent.hasChild(name)) {
            NodeBuilder builder = parent.getNodeBuilder();
            builder.removeNode(name);
            parent.children.remove(name);
            removed = true;
            root.purge();
            return true;
        } else {
            return false;
        }
    }
    private boolean isRemoved() {
        return removed || (parent != null && parent.isRemoved());
    }
    private Status internalGetPropertyStatus(String name) {
        if (isRemoved()) {
            return Status.REMOVED;
        }

        NodeState baseState = getBaseState();
        boolean exists = internalGetProperty(name) != null;
        if (baseState == null) {
            // This instance is NEW...
            if (exists) {
                // ...so all children are new
                return Status.NEW;
            } else {
                // ...unless they don't exist.
                return null;
            }
        } else {
            if (exists) {
                // We have the property...
                if (baseState.getProperty(name) == null) {
                    // ...but didn't have it before. So its NEW.
                    return Status.NEW;
                } else {
                    // ... and did have it before. So...
                    PropertyState base = baseState.getProperty(name);
                    PropertyState head = getProperty(name);
                    if (base == null ? head == null : base.equals(head)) {
                        // ...it's EXISTING if it hasn't changed
                        return Status.EXISTING;
                    } else {
                        // ...and MODIFIED otherwise.
                        return Status.MODIFIED;
                    }
                }
            } else {
                // We don't have the property
                if (baseState.getProperty(name) == null) {
                    // ...and didn't have it before. So it doesn't exist.
                    return null;
                } else {
                    // ...but did have it before. So it's REMOVED
                    return Status.REMOVED;
                }
            }
        }
    }
