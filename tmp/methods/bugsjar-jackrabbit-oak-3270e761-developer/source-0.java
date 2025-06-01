    void checkProtected() throws RepositoryException {
        ItemDefinition definition = (isNode()) ? ((Node) this).getDefinition() : ((Property) this).getDefinition();
        checkProtected(definition);
    }
