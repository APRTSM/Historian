    public Directory wrapForWrite(IndexDefinition definition, Directory remote, boolean reindexMode) throws IOException {
        Directory local = createLocalDirForIndexWriter(definition);
        return new CopyOnWriteDirectory(remote, local, reindexMode,
                getIndexPathForLogging(definition), getSharedWorkingSet(definition.getIndexPathFromConfig()));
    }
    private Set<String> getSharedWorkingSet(String indexPath){
        Set<String> sharedSet;
        synchronized (sharedWorkingSetMap){
            sharedSet = sharedWorkingSetMap.get(indexPath);
            if (sharedSet == null){
                sharedSet = Sets.newConcurrentHashSet();
                sharedWorkingSetMap.put(indexPath, sharedSet);
            }
        }
        return sharedSet;
    }
    public Directory wrapForRead(String indexPath, IndexDefinition definition,
            Directory remote) throws IOException {
        Directory local = createLocalDirForIndexReader(indexPath, definition);
        return new CopyOnReadDirectory(remote, local, prefetchEnabled, indexPath, getSharedWorkingSet(indexPath));
    }
