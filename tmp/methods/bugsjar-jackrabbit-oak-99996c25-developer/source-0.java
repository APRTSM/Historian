    public OakDirectory(NodeBuilder builder, String dataNodeName, IndexDefinition definition, boolean readOnly) {
        this.lockFactory = NoLockFactory.getNoLockFactory();
        this.builder = builder;
        this.directoryBuilder = builder.child(dataNodeName);
        this.definition = definition;
        this.readOnly = readOnly;
        this.fileNames.addAll(getListing());
        this.activeDeleteEnabled = definition.getActiveDeleteEnabled();
        this.indexName = definition.getIndexName();
    }
