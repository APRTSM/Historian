    public void copy(RepositoryInitializer initializer) throws RepositoryException {
        RepositoryConfig config = source.getRepositoryConfig();
        logger.info(
                "Copying repository content from {} to Oak", config.getHomeDir());
        try {
            NodeState base = target.getRoot();
            NodeBuilder builder = base.builder();

            String workspaceName =
                    source.getRepositoryConfig().getDefaultWorkspaceName();
            SecurityProviderImpl security = new SecurityProviderImpl(
                    mapSecurityConfig(config.getSecurityConfig()));

            // init target repository first
            new InitialContent().initialize(builder);
            if (initializer != null) {
                initializer.initialize(builder);
            }
            for (SecurityConfiguration sc : security.getConfigurations()) {
                sc.getWorkspaceInitializer().initialize(builder, workspaceName);
            }

            HashBiMap<String, String> uriToPrefix = HashBiMap.create();
            Map<Integer, String> idxToPrefix = newHashMap();
            copyNamespaces(builder, uriToPrefix, idxToPrefix);
            copyNodeTypes(builder, uriToPrefix.inverse());
            copyPrivileges(builder);

            // Triggers compilation of type information, which we need for
            // the type predicates used by the bulk  copy operations below.
            new TypeEditorProvider(false).getRootEditor(
                    base, builder.getNodeState(), builder, null);

            Map<String, String> versionablePaths = newHashMap();
            NodeState root = builder.getNodeState();
            copyWorkspace(builder, root, workspaceName, uriToPrefix, idxToPrefix, versionablePaths);
            copyVersionStore(builder, root, workspaceName, uriToPrefix, idxToPrefix, versionablePaths);

            logger.info("Applying default commit hooks");
            // TODO: default hooks?
            List<CommitHook> hooks = newArrayList();

            UserConfiguration userConf =
                    security.getConfiguration(UserConfiguration.class);
            String groupsPath = userConf.getParameters().getConfigValue(
                    UserConstants.PARAM_GROUP_PATH,
                    UserConstants.DEFAULT_GROUP_PATH);

            // hooks specific to the upgrade, need to run first
            hooks.add(new EditorHook(new CompositeEditorProvider(
                    new RestrictionEditorProvider(),
                    new GroupEditorProvider(groupsPath))));

            // security-related hooks
            for (SecurityConfiguration sc : security.getConfigurations()) {
                hooks.addAll(sc.getCommitHooks(workspaceName));
            }

            // type validation, reference and indexing hooks
            hooks.add(new EditorHook(new CompositeEditorProvider(
                createTypeEditorProvider(),
                createIndexEditorProvider()
            )));

            target.merge(builder, CompositeHook.compose(hooks), CommitInfo.EMPTY);
        } catch (Exception e) {
            throw new RepositoryException("Failed to copy content", e);
        }
    }
