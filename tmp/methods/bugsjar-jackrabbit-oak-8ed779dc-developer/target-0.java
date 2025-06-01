    public Node addNode(final String relPath, final String primaryNodeTypeName) throws RepositoryException {
        return perform(new ItemWriteOperation<Node>() {
            @Override
            protected void checkPreconditions() throws RepositoryException {
                super.checkPreconditions();
                SessionImpl.checkIndexOnName(sessionContext, relPath);
            }

            @Override
            public Node perform() throws RepositoryException {
                String oakPath = sessionContext.getOakPathOrThrowNotFound(relPath);
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

                String ntName = primaryNodeTypeName;
                if (ntName == null) {
                    DefinitionProvider dp = getDefinitionProvider();
                    NodeDefinition def = dp.getDefinition(parent.getTree(), oakName);
                    ntName = def.getDefaultPrimaryTypeName();
                    if (ntName == null) {
                        throw new ConstraintViolationException(
                                "no matching child node definition found for " + relPath);
                    }
                }

                // TODO: figure out the right place for this check
                NodeType nt = getNodeTypeManager().getNodeType(ntName); // throws on not found
                if (nt.isAbstract() || nt.isMixin()) {
                    throw new ConstraintViolationException();
                }
                // TODO: END

                NodeDelegate added = parent.addChild(oakName);
                if (added == null) {
                    throw new ItemExistsException();
                }

                if (getPrimaryNodeType().hasOrderableChildNodes()) {
                    dlg.setOrderableChildren(true);
                }

                NodeImpl<?> childNode = new NodeImpl<NodeDelegate>(added, sessionContext);
                childNode.internalSetPrimaryType(ntName);
                childNode.autoCreateItems();
                return childNode;
            }
        });
    }
