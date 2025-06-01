    public PropertyIterator getProperties() throws RepositoryException {
        checkStatus();

        Iterator<PropertyDelegate> properties = dlg.getProperties();
        long size = dlg.getPropertyCount();
        return new PropertyIteratorAdapter(propertyIterator(properties), size);
    }
    public NodeIterator getNodes() throws RepositoryException {
        checkStatus();

        Iterator<NodeDelegate> children = dlg.getChildren();
        long size = dlg.getChildCount();
        return new NodeIteratorAdapter(nodeIterator(children), size);
    }
