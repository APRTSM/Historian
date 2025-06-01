    public Status getStatus() {
        if (nodeBuilder.isNew()) {
            return NEW;
        } else if (nodeBuilder.isModified()) {
            return MODIFIED;
        } else {
            return UNCHANGED;
        }
    }
