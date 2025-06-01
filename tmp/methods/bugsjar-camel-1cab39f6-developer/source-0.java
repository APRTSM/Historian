    protected void loadStore() throws IOException {
        // auto create starting directory if needed
        if (!fileStore.exists()) {
            LOG.debug("Creating filestore: {}", fileStore);
            File parent = fileStore.getParentFile();
            parent.mkdirs();
            boolean created = FileUtil.createNewFile(fileStore);
            if (!created) {
                throw new IOException("Cannot create filestore: " + fileStore);
            }
        }

        LOG.trace("Loading to 1st level cache from idempotent filestore: {}", fileStore);

        cache.clear();
        Scanner scanner = null;
        try {
            scanner = new Scanner(fileStore);
            scanner.useDelimiter(STORE_DELIMITER);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                cache.put(line, line);
            }
        } catch (IOException e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        LOG.debug("Loaded {} to the 1st level cache from idempotent filestore: {}", cache.size(), fileStore);
    }
