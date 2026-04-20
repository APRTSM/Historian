    public Node addNode(final String relPath, String primaryNodeTypeName)
            throws RepositoryException {
        final String oakPath = getOakPathOrThrowNotFound(relPath);
        final String oakTypeName;
        if (primaryNodeTypeName != null) {
            oakTypeName = getOakName(primaryNodeTypeName);
        } else {
            oakTypeName = null;
        }

        checkIndexOnName(relPath);
        return perform(new ItemWriteOperation<Node>("addNode") {
            @Nonnull
            @Override
            public Node perform() throws RepositoryException {
                String oakName = PathUtils.getName(oakPath);
                String parentPath = PathUtils.getParentPath(oakPath);

                NodeDelegate parent = dlg.getChild(parentPath);
                if (parent == null) {
                    // is it a property?
                    String grandParentPath = PathUtils.getParentPath(parentPath);
                    NodeDelegate grandParent = dlg.getChild(grandParentPath);
                    if (grandParent != null) {
                        String propName = PathUtils.getName(parentPath);
                        if (grandParent.getPropertyOrNull(propName) != null) {
                            throw new ConstraintViolationException("Can't add new node to property.");
                        }
                    }

                    throw new PathNotFoundException(relPath);
                }

                if (parent.getChild(oakName) != null) {
                    throw new ItemExistsException(relPath);
                }

                // check for NODE_TYPE_MANAGEMENT permission here as we cannot
                // distinguish between user-supplied and system-generated
                // modification of that property in the PermissionValidator
                if (oakTypeName != null) {
                    PropertyState prop = PropertyStates.createProperty(JCR_PRIMARYTYPE, oakTypeName, NAME);
                    sessionContext.getAccessManager().checkPermissions(parent.getTree(), prop, Permissions.NODE_TYPE_MANAGEMENT);
                }

                NodeDelegate added = parent.addChild(oakName, oakTypeName);
                if (added == null) {
                    throw new ItemExistsException();
                }
                return createNode(added, sessionContext);
            }

            @Override
            public String toString() {
                return String.format("Adding node [%s/%s]", dlg.getPath(), relPath);
            }
        });
    }
