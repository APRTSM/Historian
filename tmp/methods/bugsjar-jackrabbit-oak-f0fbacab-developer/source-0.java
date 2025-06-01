    public boolean isStale() {
        Status status = getLocationOrNull().getStatus();
        return status == Status.DISCONNECTED || status == null;
    }
    private synchronized TreeLocation getLocationOrNull() {
        if (location.exists() && sessionDelegate.getRevision() != revision) {
            location = sessionDelegate.getLocation(location.getPath());
            revision = sessionDelegate.getRevision();
        }
        return location;
    }
    public TreeLocation getLocation() throws InvalidItemStateException {
        TreeLocation location = getLocationOrNull();
        if (!location.exists()) {
            throw new InvalidItemStateException("Item is stale");
        }
        return location;
    }
