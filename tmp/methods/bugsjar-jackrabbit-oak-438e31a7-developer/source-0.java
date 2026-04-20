    public PropertyIterator getProperties() throws RepositoryException {
        checkStatus();

        Iterator<PropertyDelegate> properties = dlg.getProperties();
        return new PropertyIteratorAdapter(propertyIterator(properties));
    }
    public NodeIterator getNodes() throws RepositoryException {
        checkStatus();

        Iterator<NodeDelegate> children = dlg.getChildren();
        return new NodeIteratorAdapter(nodeIterator(children));
    }
