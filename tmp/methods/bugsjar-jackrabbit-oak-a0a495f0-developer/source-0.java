    private void copyPrivileges(NodeBuilder root) throws RepositoryException {
        PrivilegeRegistry registry = source.getPrivilegeRegistry();
        NodeBuilder privileges = root.child(JCR_SYSTEM).child(REP_PRIVILEGES);
        privileges.setProperty(JCR_PRIMARYTYPE, NT_REP_PRIVILEGES, NAME);

        PrivilegeBits next = PrivilegeBits.NEXT_AFTER_BUILT_INS;

        logger.info("Copying registered privileges");
        for (Privilege privilege : registry.getRegisteredPrivileges()) {
            String name = privilege.getName();
            NodeBuilder def = privileges.child(name);
            def.setProperty(JCR_PRIMARYTYPE, NT_REP_PRIVILEGE, NAME);

            if (privilege.isAbstract()) {
                def.setProperty(REP_IS_ABSTRACT, true);
            }

            Privilege[] aggregate = privilege.getDeclaredAggregatePrivileges();
            if (aggregate.length > 0) {
                List<String> names = newArrayListWithCapacity(aggregate.length);
                for (Privilege p : aggregate) {
                    names.add(p.getName());
                }
                def.setProperty(REP_AGGREGATES, names, NAMES);
            }

            PrivilegeBits bits = PrivilegeBits.BUILT_IN.get(name);
            if (bits != null) {
                def.setProperty(bits.asPropertyState(REP_BITS));
            } else if (aggregate.length == 0) {
                bits = next;
                next = next.nextBits();
                def.setProperty(bits.asPropertyState(REP_BITS));
            }
        }

        privileges.setProperty(next.asPropertyState(REP_NEXT));

        // resolve privilege bits also for all aggregates
        for (String name : privileges.getChildNodeNames()) {
            resolvePrivilegeBits(privileges, name);
        }
    }
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
                sc.getRepositoryInitializer().initialize(builder);
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
    private PrivilegeBits resolvePrivilegeBits(
            NodeBuilder privileges, String name) {
        NodeBuilder def = privileges.getChildNode(name);

        PropertyState b = def.getProperty(REP_BITS);
        if (b != null) {
            return PrivilegeBits.getInstance(b);
        }

        PrivilegeBits bits = PrivilegeBits.getInstance();
        for (String n : def.getNames(REP_AGGREGATES)) {
            bits.add(resolvePrivilegeBits(privileges, n));
        }
        def.setProperty(bits.asPropertyState(REP_BITS));
        return bits;
    }
