    public Directory wrapForWrite(IndexDefinition definition, Directory remote, boolean reindexMode) throws IOException {
        Directory local = createLocalDirForIndexWriter(definition);
        return new CopyOnWriteDirectory(remote, local, reindexMode,
                getIndexPathForLogging(definition), getSharedWorkingSet(definition));
    }
    private boolean deleteFile(Directory dir, String fileName, boolean copiedFromRemote){
        LocalIndexFile file = new LocalIndexFile(dir, fileName, getFileLength(dir, fileName), copiedFromRemote);
        boolean successFullyDeleted = false;
        try {
            boolean fileExisted = false;
            if (dir.fileExists(fileName)) {
                fileExisted = true;
                dir.deleteFile(fileName);
            }
            successfullyDeleted(file, fileExisted);
            successFullyDeleted = true;
        } catch (IOException e) {
            failedToDelete(file);
            log.debug("Error occurred while removing deleted file {} from Local {}. " +
                    "Attempt would be made to delete it on next run ", fileName, dir, e);
        }
        return successFullyDeleted;
    }
        public void close() throws IOException {
            int pendingCopies = queue.size();
            addTask(STOP);

            //Wait for all pending copy task to finish
            try {
                long start = PERF_LOGGER.start();

                //Loop untill queue finished or IndexCopier
                //found to be closed. Doing it with timeout to
                //prevent any bug causing the thread to wait indefinitely
                while (!copyDone.await(10, TimeUnit.SECONDS)) {
                    if (closed) {
                        throw new IndexCopierClosedException("IndexCopier found to be closed " +
                                "while processing copy task for" + remote.toString());
                    }
                }
                PERF_LOGGER.end(start, -1, "[COW][{}] Completed pending copying task {}", indexPathForLogging, pendingCopies);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            }

            Throwable t = errorInCopy.get();
            if (t != null){
                throw new IOException("Error occurred while copying files for " + indexPathForLogging, t);
            }

            //Sanity check
            checkArgument(queue.isEmpty(), "Copy queue still " +
                    "has pending task left [%d]. %s", queue.size(), queue);

            long skippedFilesSize = getSkippedFilesSize();

            for (String fileName : deletedFilesLocal){
                deleteLocalFile(fileName);
            }

            skippedFromUploadSize.addAndGet(skippedFilesSize);

            String msg = "[COW][{}] CopyOnWrite stats : Skipped copying {} files with total size {}";
            if (reindexMode || skippedFilesSize > 10 * FileUtils.ONE_MB){
                log.info(msg, indexPathForLogging, skippedFiles.size(), humanReadableByteCount(skippedFilesSize));
            } else {
                log.debug(msg,indexPathForLogging, skippedFiles.size(), humanReadableByteCount(skippedFilesSize));
            }

            if (log.isTraceEnabled()){
                log.trace("[COW][{}] File listing - Upon completion {}", indexPathForLogging, Arrays.toString(remote.listAll()));
            }

            local.close();
            remote.close();
            sharedWorkingSet.clear();
        }
        public CopyOnReadDirectory(Directory remote, Directory local, boolean prefetch,
                                   String indexPath, Set<String> sharedWorkingSet) throws IOException {
            super(remote);
            this.remote = remote;
            this.local = local;
            this.indexPath = indexPath;

            this.localFileNames.addAll(Arrays.asList(local.listAll()));
            //Remove files which are being worked upon by COW
            this.localFileNames.removeAll(sharedWorkingSet);

            if (prefetch) {
                prefetchIndexFiles();
            }
        }
    private Set<String> getSharedWorkingSet(IndexDefinition defn){
        String indexPath = defn.getIndexPathFromConfig();

        if (indexPath == null){
            //With indexPath null the working directory would not
            //be shared between COR and COW. So just return a new set
            return new HashSet<String>();
        }

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
        public CopyOnWriteDirectory(Directory remote, Directory local, boolean reindexMode,
                                    String indexPathForLogging, Set<String> sharedWorkingSet) throws IOException {
            super(local);
            this.remote = remote;
            this.local = local;
            this.indexPathForLogging = indexPathForLogging;
            this.reindexMode = reindexMode;
            this.sharedWorkingSet = sharedWorkingSet;
            initialize();
        }
    public Directory wrapForRead(String indexPath, IndexDefinition definition,
            Directory remote) throws IOException {
        Directory local = createLocalDirForIndexReader(indexPath, definition);
        return new CopyOnReadDirectory(remote, local, prefetchEnabled, indexPath, getSharedWorkingSet(definition));
    }
        public IndexOutput createOutput(String name, IOContext context) throws IOException {
            COWFileReference ref = fileMap.remove(name);
            if (ref != null) {
                ref.delete();
            }
            ref = new COWLocalFileReference(name);
            fileMap.put(name, ref);
            sharedWorkingSet.add(name);
            return ref.createOutput(context);
        }
