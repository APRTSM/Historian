    public Status getStatus() {
        if (nodeBuilder.isNew() || nodeBuilder.isReplaced()) {
            return NEW;
        } else if (nodeBuilder.isModified()) {
            return MODIFIED;
        } else {
            return UNCHANGED;
        }
    }
