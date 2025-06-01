    public boolean hasProperty(String relPath) throws RepositoryException {
        try {
            final String oakPath = getOakPathOrThrow(relPath);
            return perform(new NodeOperation<Boolean>(dlg) {
                @Override
                public Boolean perform() throws RepositoryException {
                    return node.getPropertyOrNull(oakPath) != null;
                }
            });
        } catch (PathNotFoundException e) {
            return false;
        }
    }
    public boolean hasNode(String relPath) throws RepositoryException {
        try {
            final String oakPath = getOakPathOrThrow(relPath);
            return perform(new NodeOperation<Boolean>(dlg) {
                @Override
                public Boolean perform() throws RepositoryException {
                    return node.getChild(oakPath) != null;
                }
            });
        } catch (PathNotFoundException e) {
            return false;
        }
    }
