    private void fixFrozenUuid() {
        // OAK-1789: Convert the jcr:frozenUuid of a non-referenceable
        // frozen node from UUID to a path identifier
        PropertyState frozenUuid = properties.get(JCR_FROZENUUID);
        if (frozenUuid != null
                && frozenUuid.getType() == STRING
                && isFrozenNode.apply(this)) {
            String frozenPrimary = NT_BASE;
            Set<String> frozenMixins = newHashSet();

            PropertyState property = properties.get(JCR_FROZENPRIMARYTYPE);
            if (property != null && property.getType() == NAME) {
                frozenPrimary = property.getValue(NAME);
            }
            property = properties.get(JCR_FROZENMIXINTYPES);
            if (property != null && property.getType() == NAMES) {
                addAll(frozenMixins, property.getValue(NAMES));
            }

            if (!isReferenceable.apply(frozenPrimary, frozenMixins)) {
                frozenUuid = PropertyStates.createProperty(
                        JCR_FROZENUUID,
                        parent.getString(JCR_FROZENUUID) + "/" + name);
                properties.put(JCR_FROZENUUID, frozenUuid);
            }
        }
    }
    public void copy(RepositoryInitializer initializer) throws RepositoryException {
        RepositoryConfig config = source.getRepositoryConfig();
        logger.info(
                "Copying repository content from {} to Oak", config.getHomeDir());
        try {
            NodeBuilder builder = target.getRoot().builder();

            String workspace =
                    source.getRepositoryConfig().getDefaultWorkspaceName();
            SecurityProviderImpl security = new SecurityProviderImpl(
                    mapSecurityConfig(config.getSecurityConfig()));

            // init target repository first
            new InitialContent().initialize(builder);
            if (initializer != null) {
                initializer.initialize(builder);
            }
            for (SecurityConfiguration sc : security.getConfigurations()) {
                sc.getWorkspaceInitializer().initialize(builder, workspace);
            }

            HashBiMap<String, String> uriToPrefix = HashBiMap.create();
            Map<Integer, String> idxToPrefix = newHashMap();
            copyNamespaces(builder, uriToPrefix, idxToPrefix);
            copyNodeTypes(builder, uriToPrefix.inverse());
            copyPrivileges(builder);

            NodeState root = builder.getNodeState();
            copyVersionStore(builder, root, uriToPrefix, idxToPrefix);
            copyWorkspace(builder, root, workspace, uriToPrefix, idxToPrefix);

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
                hooks.addAll(sc.getCommitHooks(workspace));
            }

            // type validation, reference and indexing hooks
            hooks.add(new EditorHook(new CompositeEditorProvider(
                            new TypeEditorProvider(false),
                            new IndexUpdateProvider(new CompositeIndexEditorProvider(
                                    new ReferenceEditorProvider(),
                                    new PropertyIndexEditorProvider())))));

            target.merge(builder, CompositeHook.compose(hooks), CommitInfo.EMPTY);
        } catch (Exception e) {
            throw new RepositoryException("Failed to copy content", e);
        }
    }
