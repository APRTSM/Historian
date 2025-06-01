    public void loadContent(Exchange exchange, GenericFile<?> file) throws IOException {
        if (content == null) {
            try {
                content = exchange.getContext().getTypeConverter().mandatoryConvertTo(byte[].class, file.getFile());
            } catch (NoTypeConversionAvailableException e) {
                throw new IOException("Cannot load file content: " + file.getAbsoluteFilePath(), e);
            }
        }
    }
    private boolean writeFileByLocalWorkPath(File source, File file) throws IOException {
        LOG.trace("Using local work file being renamed from: {} to: {}", source, file);

        return FileUtil.renameFile(source, file, endpoint.isCopyAndDeleteOnRenameFail());
    }
    public boolean storeFile(String fileName, Exchange exchange) throws GenericFileOperationFailedException {
        ObjectHelper.notNull(endpoint, "endpoint");

        File file = new File(fileName);

        // if an existing file already exists what should we do?
        if (file.exists()) {
            if (endpoint.getFileExist() == GenericFileExist.Ignore) {
                // ignore but indicate that the file was written
                LOG.trace("An existing file already exists: {}. Ignore and do not override it.", file);
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
            InputStream in = exchange.getIn().getMandatoryBody(InputStream.class);
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
    public static InputStream genericFileToInputStream(GenericFile<?> file, Exchange exchange) throws IOException {
        if (exchange != null) {
            // use a file input stream if its a java.io.File
            if (file.getFile() instanceof java.io.File) {
                return IOHelper.buffered(new FileInputStream((File) file.getFile()));
            }
            // otherwise ensure the body is loaded as we want the input stream of the body
            file.getBinding().loadContent(exchange, file);
            return exchange.getContext().getTypeConverter().convertTo(InputStream.class, exchange, file.getBody());
        } else {
            // should revert to fallback converter if we don't have an exchange
            return null;
        }
    }
