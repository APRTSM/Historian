    protected Directory createLocalDirForIndexWriter(IndexDefinition definition) throws IOException {
        String indexPath = definition.getIndexPathFromConfig();
        File indexWriterDir;
        if (indexPath == null){
            //If indexPath is not known create a unique directory for work
            indexWriterDir = new File(indexWorkDir, String.valueOf(UNIQUE_COUNTER.incrementAndGet()));
        } else {
            File indexDir = getIndexDir(indexPath);
            String newVersion = String.valueOf(definition.getReindexCount());
            indexWriterDir = getVersionedDir(indexPath, indexDir, newVersion);
        }
        Directory dir = FSDirectory.open(indexWriterDir);

        log.debug("IndexWriter would use {}", indexWriterDir);

        if (indexPath == null) {
            dir = new DeleteOldDirOnClose(dir, indexWriterDir);
            log.debug("IndexPath [{}] not configured in index definition {}. Writer would create index " +
                    "files in temporary dir {} which would be deleted upon close. For better performance do " +
                    "configure the 'indexPath' as part of your index definition", LuceneIndexConstants.INDEX_PATH,
                    definition, indexWriterDir);
        }
        return dir;
    }
