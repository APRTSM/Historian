    public void copy() throws RepositoryException {
        RepositoryConfig config = source.getRepositoryConfig();
        logger.info(
                "Copying repository content from {} to Oak", config.getHomeDir());
        try {
            NodeBuilder builder = target.getRoot().builder();

            // init target repository first
            new InitialContent().initialize(builder);

            Map<String, String> uriToPrefix = newHashMap();
            Map<Integer, String> idxToPrefix = newHashMap();
            copyNamespaces(builder, uriToPrefix, idxToPrefix);
            copyNodeTypes(builder);
            copyPrivileges(builder);

            NodeState root = builder.getNodeState();
            copyVersionStore(builder, root, uriToPrefix, idxToPrefix);
            copyWorkspaces(builder, root, uriToPrefix, idxToPrefix);

            logger.info("Applying default commit hooks");
            String groupsPath;
            UserManagerConfig userConfig = config.getSecurityConfig().getSecurityManagerConfig().getUserManagerConfig();
            if (userConfig != null) {
                groupsPath = userConfig.getParameters().getProperty(UserManagerImpl.PARAM_GROUPS_PATH, UserConstants.DEFAULT_GROUP_PATH);
            } else {
                groupsPath = UserConstants.DEFAULT_GROUP_PATH;
            }
            // TODO: default hooks?
            CommitHook hook = new CompositeHook(
                    new EditorHook(new GroupEditorProvider(groupsPath)),
                    new EditorHook(new CompositeEditorProvider(
                            new TypeEditorProvider(false),
                            new IndexUpdateProvider(new CompositeIndexEditorProvider(
                                    new ReferenceEditorProvider(),
                                    new PropertyIndexEditorProvider())))));
            target.merge(builder, hook, CommitInfo.EMPTY);
        } catch (Exception e) {
            throw new RepositoryException("Failed to copy content", e);
        }
    }
    private void copyWorkspaces(
            NodeBuilder builder, NodeState root,
            Map<String, String> uriToPrefix, Map<Integer, String> idxToPrefix)
            throws RepositoryException, IOException {
        logger.info("Copying default workspace");

        // Copy all the default workspace content
        RepositoryConfig config = source.getRepositoryConfig();
        String name = config.getDefaultWorkspaceName();

        PersistenceManager pm =
                source.getWorkspaceInfo(name).getPersistenceManager();

        NodeState state = new JackrabbitNodeState(
                pm, root, uriToPrefix, ROOT_NODE_ID, "/", copyBinariesByReference);
        for (PropertyState property : state.getProperties()) {
            builder.setProperty(property);
        }
        for (ChildNodeEntry child : state.getChildNodeEntries()) {
            String childName = child.getName();
            if (!JCR_SYSTEM.equals(childName)) {
                builder.setChildNode(childName, child.getNodeState());
            }
        }

        // TODO: Copy all the active open-scoped locks
    }
