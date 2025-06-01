    public NodeDelegate getChild(String relPath) throws InvalidItemStateException {
        return create(sessionDelegate, getChildLocation(relPath));
    }
    public PropertyDelegate getProperty(String relPath) throws InvalidItemStateException {
        TreeLocation propertyLocation = getChildLocation(relPath);
        PropertyState propertyState = propertyLocation.getProperty();
        return propertyState == null
                ? null
                : new PropertyDelegate(sessionDelegate, propertyLocation);
    }
    private TreeLocation getChildLocation(String relPath) throws InvalidItemStateException {
        return getLocation().getChild(relPath);
    }
