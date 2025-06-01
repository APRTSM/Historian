    public boolean hasNode(String relPath) throws RepositoryException {
        final String oakPath = getOakPathOrThrow(relPath);
        return perform(new NodeOperation<Boolean>(dlg) {
            @Override
            public Boolean perform() throws RepositoryException {
                return node.getChild(oakPath) != null;
            }
        });
    }
    public boolean hasProperty(String relPath) throws RepositoryException {
        final String oakPath = getOakPathOrThrow(relPath);
        return perform(new NodeOperation<Boolean>(dlg) {
            @Override
            public Boolean perform() throws RepositoryException {
                return node.getPropertyOrNull(oakPath) != null;
            }
        });
    }
