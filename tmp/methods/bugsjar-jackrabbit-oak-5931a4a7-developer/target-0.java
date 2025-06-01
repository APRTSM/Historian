    public ContentRepository createContentRepository() {
        final List<Registration> regs = Lists.newArrayList();
        regs.add(whiteboard.register(Executor.class, getExecutor(), Collections.emptyMap()));

        IndexEditorProvider indexEditors = CompositeIndexEditorProvider.compose(indexEditorProviders);
        OakInitializer.initialize(store, new CompositeInitializer(initializers), indexEditors);

        QueryIndexProvider indexProvider = CompositeQueryIndexProvider.compose(queryIndexProviders);

        List<CommitHook> initHooks = new ArrayList<CommitHook>(commitHooks);
        initHooks.add(new EditorHook(CompositeEditorProvider
                .compose(editorProviders)));

        if (asyncIndexing) {
            String name = "async";
            AsyncIndexUpdate task = new AsyncIndexUpdate(name, store,
                    indexEditors);
            regs.add(scheduleWithFixedDelay(whiteboard, task, 5, true));
            regs.add(registerMBean(whiteboard, IndexStatsMBean.class,
                    task.getIndexStats(), IndexStatsMBean.TYPE, name));

            PropertyIndexAsyncReindex asyncPI = new PropertyIndexAsyncReindex(
                    new AsyncIndexUpdate(IndexConstants.ASYNC_REINDEX_VALUE,
                            store, indexEditors, true), getExecutor());
            regs.add(registerMBean(whiteboard,
                    PropertyIndexAsyncReindexMBean.class, asyncPI,
                    PropertyIndexAsyncReindexMBean.TYPE, name));
        }

        regs.add(registerMBean(whiteboard, QueryEngineSettingsMBean.class,
                queryEngineSettings, QueryEngineSettingsMBean.TYPE, "settings"));

        // FIXME: OAK-810 move to proper workspace initialization
        // initialize default workspace
        Iterable<WorkspaceInitializer> workspaceInitializers =
                Iterables.transform(securityProvider.getConfigurations(),
                        new Function<SecurityConfiguration, WorkspaceInitializer>() {
                            @Override
                            public WorkspaceInitializer apply(SecurityConfiguration sc) {
                                return sc.getWorkspaceInitializer();
                            }
                        });
        OakInitializer.initialize(
                workspaceInitializers, store, defaultWorkspaceName, indexEditors);

        // add index hooks later to prevent the OakInitializer to do excessive indexing
        with(new IndexUpdateProvider(indexEditors));
        withEditorHook();

        // Register observer last to prevent sending events while initialising
        for (Observer observer : observers) {
            regs.add(registerObserver(whiteboard, observer));
        }

        RepositoryManager repositoryManager = new RepositoryManager(whiteboard);
        regs.add(registerMBean(whiteboard, RepositoryManagementMBean.class, repositoryManager,
                RepositoryManagementMBean.TYPE, repositoryManager.getName()));

        return new ContentRepositoryImpl(
                store,
                CompositeHook.compose(commitHooks),
                defaultWorkspaceName,
                queryEngineSettings,
                indexProvider,
                securityProvider) {
            @Override
            public void close() throws IOException {
                super.close();
                new CompositeRegistration(regs).unregister();
            }
        };
    }
