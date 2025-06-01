    public Property getPropertyOrNull(final String absPath) throws RepositoryException {
        if (absPath.equals("/")) {
            return null;
        } else {
            final String oakPath;
            try {
                oakPath = getOakPathOrThrow(absPath);
            } catch (PathNotFoundException e) {
                return null;
            }
            return perform(new ReadOperation<Property>() {
                @Override
                public Property perform() throws RepositoryException {
                    PropertyDelegate pd = sd.getProperty(oakPath);
                    if (pd != null) {
                        return new PropertyImpl(pd, sessionContext);
                    } else {
                        return null;
                    }
                }
            });
        }
    }
    public Node getNodeOrNull(final String absPath) throws RepositoryException {
        return perform(new ReadOperation<Node>() {
            @Override
            public Node perform() throws RepositoryException {
                try {
                    return NodeImpl.createNodeOrNull(sd.getNode(getOakPathOrThrow(absPath)), sessionContext);
                } catch (PathNotFoundException e) {
                    return null;
                }
            }
        });
    }
