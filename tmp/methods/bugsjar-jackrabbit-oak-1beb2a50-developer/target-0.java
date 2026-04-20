    private void copyNodeTypes(NodeBuilder root) throws RepositoryException {
        NodeTypeRegistry sourceRegistry = source.getNodeTypeRegistry();
        NodeBuilder system = root.child(JCR_SYSTEM);
        NodeBuilder types = system.child(JCR_NODE_TYPES);

        logger.info("Copying registered node types");
        for (Name name : sourceRegistry.getRegisteredNodeTypes()) {
            // skip built-in nodetypes (OAK-1235)
            String oakName = getOakName(name);
            if (BUILT_IN_NODE_TYPES.contains(oakName)) {
                logger.info("skipping built-on nodetype: {}", name);
                continue;
            }
            QNodeTypeDefinition def = sourceRegistry.getNodeTypeDef(name);
            NodeBuilder type = types.child(oakName);
            copyNodeType(def, type);
        }
    }
    private Map<Integer, String> copyNamespaces(NodeBuilder root)
            throws RepositoryException {
        Map<Integer, String> idxToPrefix = newHashMap();

        NodeBuilder system = root.child(JCR_SYSTEM);
        NodeBuilder namespaces = system.child(NamespaceConstants.REP_NAMESPACES);

        Properties registry = loadProperties("/namespaces/ns_reg.properties");
        Properties indexes  = loadProperties("/namespaces/ns_idx.properties");

        for (String prefixHint : registry.stringPropertyNames()) {
            String uri = registry.getProperty(prefixHint);
            if (".empty.key".equals(prefixHint)) {
                prefixHint = "";
            }

            String prefix =
                    Namespaces.addCustomMapping(namespaces, uri, prefixHint);

            String index = null;
            if (uri.isEmpty()) {
                index = indexes.getProperty(".empty.key");
            }
            if (index == null) {
                index = indexes.getProperty(uri);
            }

            Integer idx;
            if (index != null) {
                idx = Integer.decode(index);
            } else {
                int i = 0;
                do {
                    idx = (uri.hashCode() + i++) & 0x00ffffff;
                } while (idxToPrefix.containsKey(idx));
            }

            checkState(idxToPrefix.put(idx, prefix) == null);
        }

        Namespaces.buildIndexNode(namespaces);

        return idxToPrefix;
    }
    public void copy() throws RepositoryException {
        logger.info(
                "Copying repository content from {} to Oak",
                source.getRepositoryConfig().getHomeDir());
        try {
            NodeBuilder builder = target.getRoot().builder();

            // init target repository first
            new InitialContent().initialize(builder);

            Map<Integer, String> idxToPrefix = copyNamespaces(builder);
            copyNodeTypes(builder);
            copyVersionStore(builder, idxToPrefix);
            copyWorkspaces(builder, idxToPrefix);

            // TODO: default hooks?
            CommitHook hook = new CompositeHook(
                    new EditorHook(new RegistrationEditorProvider()),
                    new EditorHook(new ReferenceEditorProvider()),
                    new EditorHook(new GroupEditorProvider())
            );
            target.merge(builder, hook, null);
        } catch (Exception e) {
            throw new RepositoryException("Failed to copy content", e);
        }
    }
