    public Node addNode(final String relPath, final String primaryNodeTypeName) throws RepositoryException {
        checkStatus();
        checkProtected();

        return sessionDelegate.perform(new SessionOperation<Node>() {
            @Override
            public Node perform() throws RepositoryException {
                String oakPath = sessionDelegate.getOakPathKeepIndexOrThrowNotFound(relPath);
                String oakName = PathUtils.getName(oakPath);
                String parentPath = sessionDelegate.getOakPath(PathUtils.getParentPath(oakPath));

                // handle index
                if (oakName.contains("[")) {
                    throw new RepositoryException("Cannot create a new node using a name including an index");
                }

                NodeDelegate parent = dlg.getChild(parentPath);
                if (parent == null) {
                    // is it a property?
                    String grandParentPath = PathUtils.getParentPath(parentPath);
                    NodeDelegate grandParent = dlg.getChild(grandParentPath);
                    if (grandParent != null) {
                        String propName = PathUtils.getName(parentPath);
                        if (grandParent.getProperty(propName) != null) {
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
                    DefinitionProvider dp = sessionDelegate.getDefinitionProvider();
                    String childName = sessionDelegate.getOakName(PathUtils.getName(relPath));
                    NodeDefinition def = dp.getDefinition(new NodeImpl<NodeDelegate>(parent), childName);
                    ntName = def.getDefaultPrimaryTypeName();
                    if (ntName == null) {
                        throw new ConstraintViolationException(
                                "no matching child node definition found for " + relPath);
                    }
                }

                // TODO: figure out the right place for this check
                NodeTypeManager ntm = sessionDelegate.getNodeTypeManager();
                NodeType nt = ntm.getNodeType(ntName); // throws on not found
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

                NodeImpl<?> childNode = new NodeImpl<NodeDelegate>(added);
                childNode.internalSetPrimaryType(ntName);
                childNode.autoCreateItems();
                return childNode;
            }
        });
    }
