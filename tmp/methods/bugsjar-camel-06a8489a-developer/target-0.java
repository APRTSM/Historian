    public FileConsumer createConsumer(Processor processor) throws Exception {
        ObjectHelper.notNull(operations, "operations");
        ObjectHelper.notNull(file, "file");

        // auto create starting directory if needed
        if (!file.exists() && !file.isDirectory()) {
            if (isAutoCreate()) {
                log.debug("Creating non existing starting directory: {}", file);
                boolean absolute = FileUtil.isAbsolute(file);
                operations.buildDirectory(file.getPath(), absolute);
            } else if (isStartingDirectoryMustExist()) {
                throw new FileNotFoundException("Starting directory does not exist: " + file);
            }
        }

        FileConsumer result = new FileConsumer(this, processor, operations);

        if (isDelete() && getMove() != null) {
            throw new IllegalArgumentException("You cannot set both delete=true and move options");
        }

        // if noop=true then idempotent should also be configured
        if (isNoop() && !isIdempotentSet()) {
            log.info("Endpoint is configured with noop=true so forcing endpoint to be idempotent as well");
            setIdempotent(true);
        }

        // if idempotent and no repository set then create a default one
        if (isIdempotentSet() && isIdempotent() && idempotentRepository == null) {
            log.info("Using default memory based idempotent repository with cache max size: " + DEFAULT_IDEMPOTENT_CACHE_SIZE);
            idempotentRepository = MemoryIdempotentRepository.memoryIdempotentRepository(DEFAULT_IDEMPOTENT_CACHE_SIZE);
        }

        // set max messages per poll
        result.setMaxMessagesPerPoll(getMaxMessagesPerPoll());

        configureConsumer(result);
        return result;
    }
