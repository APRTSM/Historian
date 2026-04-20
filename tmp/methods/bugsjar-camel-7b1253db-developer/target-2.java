    public String getCopyFromAbsoluteFilePath() {
        return copyFromAbsoluteFilePath;
    }
    public void setCopyFromAbsoluteFilePath(String copyFromAbsoluteFilePath) {
        this.copyFromAbsoluteFilePath = copyFromAbsoluteFilePath;
    }
    public GenericFile<T> copyFrom(GenericFile<T> source) {
        GenericFile<T> result;
        try {
            result = source.getClass().newInstance();
        } catch (Exception e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }
        result.setCopyFromAbsoluteFilePath(source.getAbsoluteFilePath());
        result.setEndpointPath(source.getEndpointPath());
        result.setAbsolute(source.isAbsolute());
        result.setDirectory(source.isDirectory());
        result.setAbsoluteFilePath(source.getAbsoluteFilePath());
        result.setRelativeFilePath(source.getRelativeFilePath());
        result.setFileName(source.getFileName());
        result.setFileNameOnly(source.getFileNameOnly());
        result.setFileLength(source.getFileLength());
        result.setLastModified(source.getLastModified());
        result.setFile(source.getFile());
        result.setBody(source.getBody());
        result.setBinding(source.getBinding());
        result.setCharset(source.getCharset());

        copyFromPopulateAdditional(source, result);
        return result;
    }
    private static String asReadLockKey(GenericFile file, String key) {
        // use the copy from absolute path as that was the original path of the file when the lock was acquired
        // for example if the file consumer uses preMove then the file is moved and therefore has another name
        // that would no longer match
        String path = file.getCopyFromAbsoluteFilePath() != null ? file.getCopyFromAbsoluteFilePath() : file.getAbsoluteFilePath();
        return path + "-" + key;
    }
    public boolean acquireExclusiveReadLock(GenericFileOperations<File> operations, GenericFile<File> file, Exchange exchange) throws Exception {
        // must call super
        if (!super.acquireExclusiveReadLock(operations, file, exchange)) {
            return false;
        }

        File target = new File(file.getAbsoluteFilePath());

        LOG.trace("Waiting for exclusive read lock to file: {}", target);

        FileChannel channel = null;
        RandomAccessFile randomAccessFile = null;

        boolean exclusive = false;
        FileLock lock = null;

        try {
            randomAccessFile = new RandomAccessFile(target, "rw");
            // try to acquire rw lock on the file before we can consume it
            channel = randomAccessFile.getChannel();

            StopWatch watch = new StopWatch();

            while (!exclusive) {
                // timeout check
                if (timeout > 0) {
                    long delta = watch.taken();
                    if (delta > timeout) {
                        CamelLogger.log(LOG, readLockLoggingLevel,
                                "Cannot acquire read lock within " + timeout + " millis. Will skip the file: " + target);
                        // we could not get the lock within the timeout period, so return false
                        return false;
                    }
                }

                // get the lock using either try lock or not depending on if we are using timeout or not
                try {
                    lock = timeout > 0 ? channel.tryLock() : channel.lock();
                } catch (IllegalStateException ex) {
                    // Also catch the OverlappingFileLockException here. Do nothing here                    
                }
                if (lock != null) {
                    LOG.trace("Acquired exclusive read lock: {} to file: {}", lock, target);
                    exclusive = true;
                } else {
                    boolean interrupted = sleep();
                    if (interrupted) {
                        // we were interrupted while sleeping, we are likely being shutdown so return false
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            // must handle IOException as some apps on Windows etc. will still somehow hold a lock to a file
            // such as AntiVirus or MS Office that has special locks for it's supported files
            if (timeout == 0) {
                // if not using timeout, then we cant retry, so return false
                return false;
            }
            LOG.debug("Cannot acquire read lock. Will try again.", e);
            boolean interrupted = sleep();
            if (interrupted) {
                // we were interrupted while sleeping, we are likely being shutdown so return false
                return false;
            }
        } finally {
            // close channels if we did not grab the lock
            if (!exclusive) {
                IOHelper.close(channel, "while acquiring exclusive read lock for file: " + target, LOG);
                IOHelper.close(randomAccessFile, "while acquiring exclusive read lock for file: " + target, LOG);

                // and also must release super lock
                super.releaseExclusiveReadLockOnAbort(operations, file, exchange);
            }
        }

        // store read-lock state
        exchange.setProperty(asReadLockKey(file, Exchange.FILE_LOCK_EXCLUSIVE_LOCK), lock);
        exchange.setProperty(asReadLockKey(file, Exchange.FILE_LOCK_RANDOM_ACCESS_FILE), randomAccessFile);

        // we grabbed the lock
        return true;
    }
    protected void doReleaseExclusiveReadLock(GenericFileOperations<File> operations,
                                              GenericFile<File> file, Exchange exchange) throws Exception {
        // must call super
        super.doReleaseExclusiveReadLock(operations, file, exchange);

        FileLock lock = exchange.getProperty(asReadLockKey(file, Exchange.FILE_LOCK_EXCLUSIVE_LOCK), FileLock.class);
        RandomAccessFile rac = exchange.getProperty(asReadLockKey(file, Exchange.FILE_LOCK_EXCLUSIVE_LOCK), RandomAccessFile.class);

        String target = file.getFileName();
        if (lock != null) {
            Channel channel = lock.acquiredBy();
            try {
                lock.release();
            } finally {
                // close channel as well
                IOHelper.close(channel, "while releasing exclusive read lock for file: " + target, LOG);
                IOHelper.close(rac, "while releasing exclusive read lock for file: " + target, LOG);
            }
        }
    }
    public boolean acquireExclusiveReadLock(GenericFileOperations<File> operations,
                                            GenericFile<File> file, Exchange exchange) throws Exception {

        if (!markerFile) {
            // if not using marker file then we assume acquired
            return true;
        }

        String lockFileName = getLockFileName(file);
        LOG.trace("Locking the file: {} using the lock file name: {}", file, lockFileName);

        // create a plain file as marker filer for locking (do not use FileLock)
        boolean acquired = FileUtil.createNewFile(new File(lockFileName));

        // store read-lock state
        exchange.setProperty(asReadLockKey(file, Exchange.FILE_LOCK_FILE_ACQUIRED), acquired);
        exchange.setProperty(asReadLockKey(file, Exchange.FILE_LOCK_FILE_NAME), lockFileName);

        return acquired;
    }
    private static String asReadLockKey(GenericFile file, String key) {
        // use the copy from absolute path as that was the original path of the file when the lock was acquired
        // for example if the file consumer uses preMove then the file is moved and therefore has another name
        // that would no longer match
        String path = file.getCopyFromAbsoluteFilePath() != null ? file.getCopyFromAbsoluteFilePath() : file.getAbsoluteFilePath();
        return path + "-" + key;
    }
    protected void doReleaseExclusiveReadLock(GenericFileOperations<File> operations,
                                              GenericFile<File> file, Exchange exchange) throws Exception {
        if (!markerFile) {
            // if not using marker file then nothing to release
            return;
        }

        boolean acquired = exchange.getProperty(asReadLockKey(file, Exchange.FILE_LOCK_FILE_ACQUIRED), false, Boolean.class);

        // only release the file if camel get the lock before
        if (acquired) {
            String lockFileName = exchange.getProperty(asReadLockKey(file, Exchange.FILE_LOCK_FILE_NAME), String.class);
            File lock = new File(lockFileName);

            if (lock.exists()) {
                LOG.trace("Unlocking file: {}", lockFileName);
                boolean deleted = FileUtil.deleteFile(lock);
                LOG.trace("Lock file: {} was deleted: {}", lockFileName, deleted);
            }
        }
    }
