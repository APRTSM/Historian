    private String copyWorkspaces(
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

        return name;
    }
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
            String workspaceName =
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
            List<CommitHook> hooks = newArrayList();
            hooks.add(new EditorHook(new CompositeEditorProvider(
                            new GroupEditorProvider(groupsPath),
                            new TypeEditorProvider(false),
                            new IndexUpdateProvider(new CompositeIndexEditorProvider(
                                    new ReferenceEditorProvider(),
                                    new PropertyIndexEditorProvider())))));
            hooks.addAll(new AuthorizationConfigurationImpl().getCommitHooks(workspaceName));
            target.merge(builder, CompositeHook.compose(hooks), CommitInfo.EMPTY);
        } catch (Exception e) {
            throw new RepositoryException("Failed to copy content", e);
        }
    }
