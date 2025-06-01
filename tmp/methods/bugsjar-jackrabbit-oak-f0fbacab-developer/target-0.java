    public boolean isStale() {
        return !getLocationInternal().exists();
    }
    public TreeLocation getLocation() throws InvalidItemStateException {
        TreeLocation location = getLocationInternal();
        if (!location.exists()) {
            throw new InvalidItemStateException("Item is stale");
        }
        return location;
    }
    private synchronized TreeLocation getLocationInternal() {
        if (sessionDelegate.getRevision() != revision || !location.exists()) {
            location = sessionDelegate.getLocation(location.getPath());
            revision = sessionDelegate.getRevision();
        }
        return location;
    }
