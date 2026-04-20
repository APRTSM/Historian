    void checkProtected() throws RepositoryException {
        ItemDefinition definition;
        try {
            definition = (isNode()) ? ((Node) this).getDefinition() : ((Property) this).getDefinition();
        }
        catch (RepositoryException ignore) {
            // No definition -> not protected but a different error which should be handled else where
            return;
        }
        checkProtected(definition);
    }
