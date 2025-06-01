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
    private void updateIndex(
            NodeState before, String beforeCheckpoint,
            NodeState after, String afterCheckpoint, String afterTime)
            throws CommitFailedException {
        // start collecting runtime statistics
        preAsyncRunStatsStats(indexStats);

        // create an update callback for tracking index updates
        // and maintaining the update lease
        AsyncUpdateCallback callback =
                new AsyncUpdateCallback(beforeCheckpoint, afterCheckpoint);
        try {
            NodeBuilder builder = store.getRoot().builder();

            IndexUpdate indexUpdate =
                    new IndexUpdate(provider, name, after, builder, callback);
            CommitFailedException exception =
                    EditorDiff.process(VisibleEditor.wrap(indexUpdate), before, after);
            if (exception != null) {
                throw exception;
            }

            builder.child(ASYNC).setProperty(name, afterCheckpoint);
            builder.child(ASYNC).setProperty(PropertyStates.createProperty(lastIndexedTo, afterTime, Type.DATE));
            if (callback.isDirty() || before == MISSING_NODE) {
                if (switchOnSync) {
                    reindexedDefinitions.addAll(
                            indexUpdate.getReindexedDefinitions());
                } else {
                    postAsyncRunStatsStatus(indexStats);
                }
            } else {
                if (switchOnSync) {
                    log.debug(
                            "No changes detected after diff; will try to switch to synchronous updates on {}",
                            reindexedDefinitions);

                    // no changes after diff, switch to sync on the async defs
                    for (String path : reindexedDefinitions) {
                        NodeBuilder c = builder;
                        for (String p : elements(path)) {
                            c = c.getChildNode(p);
                        }
                        if (c.exists() && !c.getBoolean(REINDEX_PROPERTY_NAME)) {
                            c.removeProperty(ASYNC_PROPERTY_NAME);
                        }
                    }
                    reindexedDefinitions.clear();
                }
                postAsyncRunStatsStatus(indexStats);
            }
            mergeWithConcurrencyCheck(builder, beforeCheckpoint, callback.lease);
        } finally {
            callback.close();
        }
    }
