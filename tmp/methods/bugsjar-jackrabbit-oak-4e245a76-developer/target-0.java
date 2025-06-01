    public PropertyDelegate getProperty(String relPath) throws RepositoryException {
        TreeLocation propertyLocation = getChildLocation(relPath);
        PropertyState propertyState = propertyLocation.getProperty();
        return propertyState == null
                ? null
                : new PropertyDelegate(sessionDelegate, propertyLocation);
    }
    private TreeLocation getChildLocation(String relPath) throws RepositoryException {
        if (PathUtils.isAbsolute(relPath)) {
            throw new RepositoryException("Not a relative path: " + relPath);
        }

        TreeLocation loc = getLocation();
        for (String element : PathUtils.elements(relPath)) {
            if (PathUtils.denotesParent(element)) {
                loc = loc.getParent();
            } else if (!PathUtils.denotesCurrent(element)) {
                loc = loc.getChild(element);
            }  // else . -> skip to next element
        }
        return loc;
    }
    public NodeDelegate getChild(String relPath) throws RepositoryException {
        return create(sessionDelegate, getChildLocation(relPath));
    }
