    private void keepLastModified(Exchange exchange, File file) {
        if (endpoint.isKeepLastModified()) {
            Long last;
            Date date = exchange.getIn().getHeader(Exchange.FILE_LAST_MODIFIED, Date.class);
            if (date != null) {
                last = date.getTime();
            } else {
                // fallback and try a long
                last = exchange.getIn().getHeader(Exchange.FILE_LAST_MODIFIED, Long.class);
            }
            if (last != null) {
                boolean result = file.setLastModified(last);
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Keeping last modified timestamp: " + last + " on file: " + file + " with result: " + result);
                }
            }
        }
    }
    public boolean buildDirectory(String directory, boolean absolute) throws GenericFileOperationFailedException {
        ObjectHelper.notNull(endpoint, "endpoint");

        // always create endpoint defined directory
        if (endpoint.isAutoCreate() && !endpoint.getFile().exists()) {
            LOG.trace("Building starting directory: {}", endpoint.getFile());
            endpoint.getFile().mkdirs();
        }

        if (ObjectHelper.isEmpty(directory)) {
            // no directory to build so return true to indicate ok
            return true;
        }

        File endpointPath = endpoint.getFile();
        File target = new File(directory);

        File path;
        if (absolute) {
            // absolute path
            path = target;
        } else if (endpointPath.equals(target)) {
            // its just the root of the endpoint path
            path = endpointPath;
        } else {
            // relative after the endpoint path
            String afterRoot = ObjectHelper.after(directory, endpointPath.getPath() + File.separator);
            if (ObjectHelper.isNotEmpty(afterRoot)) {
                // dir is under the root path
                path = new File(endpoint.getFile(), afterRoot);
            } else {
                // dir is relative to the root path
                path = new File(endpoint.getFile(), directory);
            }
        }

        // We need to make sure that this is thread-safe and only one thread tries to create the path directory at the same time.
        synchronized (this) {
            if (path.isDirectory() && path.exists()) {
                // the directory already exists
                return true;
            } else {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Building directory: " + path);
                }
                return path.mkdirs();
            }
        }
    }
    private boolean writeFileByLocalWorkPath(File source, File file) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Using local work file being renamed from: " + source + " to: " + file);
        }

        return FileUtil.renameFile(source, file);
    }
    private void writeFileByFile(File source, File target) throws IOException {
        FileChannel in = new FileInputStream(source).getChannel();
        FileChannel out = null;
        try {
            out = prepareOutputFileChannel(target, out);

            if (LOG.isTraceEnabled()) {
                LOG.trace("Using FileChannel to transfer from: " + in + " to: " + out);
            }

            long size = in.size();
            long position = 0;
            while (position < size) {
                position += in.transferTo(position, endpoint.getBufferSize(), out);
            }
        } finally {
            IOHelper.close(in, source.getName(), LOG);
            IOHelper.close(out, source.getName(), LOG);
        }
    }
    public boolean storeFile(String fileName, Exchange exchange) throws GenericFileOperationFailedException {
        ObjectHelper.notNull(endpoint, "endpoint");

        File file = new File(fileName);

        // if an existing file already exists what should we do?
        if (file.exists()) {
            if (endpoint.getFileExist() == GenericFileExist.Ignore) {
                // ignore but indicate that the file was written
                if (LOG.isTraceEnabled()) {
                    LOG.trace("An existing file already exists: " + file + ". Ignore and do not override it.");
                }
                return true;
            } else if (endpoint.getFileExist() == GenericFileExist.Fail) {
                throw new GenericFileOperationFailedException("File already exist: " + file + ". Cannot write new file.");
            }
        }

        // we can write the file by 3 different techniques
        // 1. write file to file
        // 2. rename a file from a local work path
        // 3. write stream to file
        try {

            // is the body file based
            File source = null;
            // get the File Object from in message
            source = exchange.getIn().getBody(File.class);

            if (source != null) {
                // okay we know the body is a file type

                // so try to see if we can optimize by renaming the local work path file instead of doing
                // a full file to file copy, as the local work copy is to be deleted afterwards anyway
                // local work path
                File local = exchange.getIn().getHeader(Exchange.FILE_LOCAL_WORK_PATH, File.class);
                if (local != null && local.exists()) {
                    boolean renamed = writeFileByLocalWorkPath(local, file);
                    if (renamed) {
                        // try to keep last modified timestamp if configured to do so
                        keepLastModified(exchange, file);
                        // clear header as we have renamed the file
                        exchange.getIn().setHeader(Exchange.FILE_LOCAL_WORK_PATH, null);
                        // return as the operation is complete, we just renamed the local work file
                        // to the target.
                        return true;
                    }
                } else if (source.exists()) {
                    // no there is no local work file so use file to file copy if the source exists
                    writeFileByFile(source, file);
                    // try to keep last modified timestamp if configured to do so
                    keepLastModified(exchange, file);
                    return true;
                }
            }

            // fallback and use stream based
            InputStream in = ExchangeHelper.getMandatoryInBody(exchange, InputStream.class);
            writeFileByStream(in, file);
            // try to keep last modified timestamp if configured to do so
            keepLastModified(exchange, file);
            return true;
        } catch (IOException e) {
            throw new GenericFileOperationFailedException("Cannot store file: " + file, e);
        } catch (InvalidPayloadException e) {
            throw new GenericFileOperationFailedException("Cannot store file: " + file, e);
        }
    }
    private void writeFileByStream(InputStream in, File target) throws IOException {
        FileChannel out = null;
        try {
            out = prepareOutputFileChannel(target, out);

            if (LOG.isTraceEnabled()) {
                LOG.trace("Using InputStream to transfer from: " + in + " to: " + out);
            }
            int size = endpoint.getBufferSize();
            byte[] buffer = new byte[size];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                if (bytesRead < size) {
                    byteBuffer.limit(bytesRead);
                }
                out.write(byteBuffer);
                byteBuffer.clear();
            }
        } finally {
            IOHelper.close(in, target.getName(), LOG);
            IOHelper.close(out, target.getName(), LOG);
        }
    }
    public boolean deleteFile(String name) throws GenericFileOperationFailedException {
        File file = new File(name);
        return FileUtil.deleteFile(file);
    }
    public boolean begin(GenericFileOperations<T> operations, GenericFileEndpoint<T> endpoint, Exchange exchange, GenericFile<T> file) throws Exception {

        // We need to invoke super, but to the file that we are going to use for processing, so we do super after renaming.
        GenericFile<T> to = file;

        if (beginRenamer != null) {
            GenericFile<T> newName = beginRenamer.renameFile(exchange, file);
            to = renameFile(operations, file, newName);
            if (to != null) {
                to.bindToExchange(exchange);
            }
        }
        // must invoke super
        boolean result = super.begin(operations, endpoint, exchange, to);
        if (!result) {
            return false;
        }

        return true;
    }
    public boolean begin(GenericFileOperations<T> operations, GenericFileEndpoint<T> endpoint, Exchange exchange, GenericFile<T> file) throws Exception {

        // We need to invoke super, but to the file that we are going to use for processing, so we do super after renaming.
        GenericFile<T> to = file;

        if (beginRenamer != null) {
            GenericFile<T> newName = beginRenamer.renameFile(exchange, file);
            to = renameFile(operations, file, newName);
            if (to != null) {
                to.bindToExchange(exchange);
            }
        }
        // must invoke super
        boolean result = super.begin(operations, endpoint, exchange, to);
        if (!result) {
            return false;
        }

        return true;
    }
    public void releaseExclusiveReadLock(GenericFileOperations<File> operations,
                                         GenericFile<File> file, Exchange exchange) throws Exception {
        String lockFileName = getLockFileName(file);
        File lock = new File(lockFileName);

        LOG.trace("Unlocking file: {}", lockFileName);

        boolean deleted = FileUtil.deleteFile(lock);
        LOG.trace("Lock file: {} was deleted: {}", lockFileName, deleted);
    }
    private static String getLockFileName(GenericFile<File> file) {
        return file.getAbsoluteFilePath() + FileComponent.DEFAULT_LOCK_FILE_POSTFIX;
    }
    public boolean acquireExclusiveReadLock(GenericFileOperations<File> operations,
                                            GenericFile<File> file, Exchange exchange) throws Exception {
        String lockFileName = getLockFileName(file);
        LOG.trace("Locking the file: {} using the lock file name: {}", file, lockFileName);

        // create a plain file as marker filer for locking (do not use FileLock)
        File lock = new File(lockFileName);
        boolean acquired = lock.createNewFile();

        return acquired;
    }
