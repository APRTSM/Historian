    public void copy(RepositoryInitializer initializer) throws RepositoryException {
        RepositoryConfig config = source.getRepositoryConfig();
        logger.info("Copying repository content from {} to Oak", config.getHomeDir());
        try {
            NodeState base = target.getRoot();
            NodeBuilder builder = base.builder();
            final Root upgradeRoot = new UpgradeRoot(builder);

            String workspaceName =
                    source.getRepositoryConfig().getDefaultWorkspaceName();
            SecurityProviderImpl security = new SecurityProviderImpl(
                    mapSecurityConfig(config.getSecurityConfig()));

            // init target repository first
            logger.info("Initializing initial repository content from {}", config.getHomeDir());
            new InitialContent().initialize(builder);
            if (initializer != null) {
                initializer.initialize(builder);
            }
            logger.debug("InitialContent completed from {}", config.getHomeDir());

            for (SecurityConfiguration sc : security.getConfigurations()) {
                RepositoryInitializer ri = sc.getRepositoryInitializer();
                ri.initialize(builder);
                logger.debug("Repository initializer '" + ri.getClass().getName() + "' completed", config.getHomeDir());
            }
            for (SecurityConfiguration sc : security.getConfigurations()) {
                WorkspaceInitializer wi = sc.getWorkspaceInitializer();
                wi.initialize(builder, workspaceName);
                logger.debug("Workspace initializer '" + wi.getClass().getName() + "' completed", config.getHomeDir());
            }

            HashBiMap<String, String> uriToPrefix = HashBiMap.create();
            logger.info("Copying registered namespaces");
            copyNamespaces(builder, uriToPrefix);
            logger.debug("Namespace registration completed.");

            logger.info("Copying registered node types");
            NodeTypeManager ntMgr = new ReadWriteNodeTypeManager() {
                @Override
                protected Tree getTypes() {
                    return upgradeRoot.getTree(NODE_TYPES_PATH);
                }

                @Nonnull
                @Override
                protected Root getWriteRoot() {
                    return upgradeRoot;
                }
            };
            copyNodeTypes(ntMgr, new ValueFactoryImpl(upgradeRoot, NamePathMapper.DEFAULT));
            logger.debug("Node type registration completed.");

            // migrate privileges
            logger.info("Copying registered privileges");
            PrivilegeConfiguration privilegeConfiguration = security.getConfiguration(PrivilegeConfiguration.class);
            copyCustomPrivileges(privilegeConfiguration.getPrivilegeManager(upgradeRoot, NamePathMapper.DEFAULT));
            logger.debug("Privilege registration completed.");

            // Triggers compilation of type information, which we need for
            // the type predicates used by the bulk  copy operations below.
            new TypeEditorProvider(false).getRootEditor(
                    base, builder.getNodeState(), builder, null);

            NodeState root = builder.getNodeState();

            final NodeState sourceState = JackrabbitNodeState.createRootNodeState(
                    source, workspaceName, root, uriToPrefix, copyBinariesByReference, skipOnError);

            final Stopwatch watch = Stopwatch.createStarted();

            logger.info("Copying workspace content");
            copyWorkspace(sourceState, builder, workspaceName);
            builder.getNodeState(); // on TarMK this does call triggers the actual copy
            logger.info("Upgrading workspace content completed in {}s ({})", watch.elapsed(TimeUnit.SECONDS), watch);

            if (!versionCopyConfiguration.skipOrphanedVersionsCopy()) {
                logger.info("Copying version storage");
                watch.reset().start();
                copyVersionStorage(sourceState, builder, versionCopyConfiguration);
                builder.getNodeState(); // on TarMK this does call triggers the actual copy
                logger.info("Version storage copied in {}s ({})", watch.elapsed(TimeUnit.SECONDS), watch);
            } else {
                logger.info("Skipping the version storage as the copyOrphanedVersions is set to false");
            }

            watch.reset().start();
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
                    new GroupEditorProvider(groupsPath),
                    // copy referenced version histories
                    new VersionableEditor.Provider(sourceState, workspaceName, versionCopyConfiguration)
            )));

            // security-related hooks
            for (SecurityConfiguration sc : security.getConfigurations()) {
                hooks.addAll(sc.getCommitHooks(workspaceName));
            }

            if (customCommitHooks != null) {
                hooks.addAll(customCommitHooks);
            }

            // type validation, reference and indexing hooks
            hooks.add(new EditorHook(new CompositeEditorProvider(
                createTypeEditorProvider(),
                createIndexEditorProvider()
            )));

            target.merge(builder, new LoggingCompositeHook(hooks, source, overrideEarlyShutdown()), CommitInfo.EMPTY);
            logger.info("Processing commit hooks completed in {}s ({})", watch.elapsed(TimeUnit.SECONDS), watch);
            logger.debug("Repository upgrade completed.");
        } catch (Exception e) {
            throw new RepositoryException("Failed to copy content", e);
        }
    }
    private boolean overrideEarlyShutdown() {
        if (earlyShutdown == false) {
            return false;
        }

        final VersionCopyConfiguration c = this.versionCopyConfiguration;
        if (c.isCopyVersions() && c.skipOrphanedVersionsCopy()) {
            logger.info("Overriding early shutdown to false because of the copy versions settings");
            return false;
        }
        if (c.isCopyVersions() && !c.skipOrphanedVersionsCopy()
                && c.getOrphanedMinDate().after(c.getVersionsMinDate())) {
            logger.info("Overriding early shutdown to false because of the copy versions settings");
            return false;
        }
        return true;
    }
