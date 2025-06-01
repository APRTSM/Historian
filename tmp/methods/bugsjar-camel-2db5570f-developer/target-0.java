    protected void processStrategyCommit(GenericFileProcessStrategy<T> processStrategy,
                                         Exchange exchange, GenericFile<T> file) {
        if (endpoint.isIdempotent()) {
            // only add to idempotent repository if we could process the file
            endpoint.getIdempotentRepository().add(absoluteFileName);
        }

        // delete done file if used (and not noop=true)
        if (endpoint.getDoneFileName() != null && !endpoint.isNoop()) {
            // done file must be in same path as the original input file
            String doneFileName = endpoint.createDoneFileName(absoluteFileName);
            ObjectHelper.notEmpty(doneFileName, "doneFileName", endpoint);

            try {
                // delete done file
                boolean deleted = operations.deleteFile(doneFileName);
                log.trace("Done file: {} was deleted: {}", doneFileName, deleted);
                if (!deleted) {
                    log.warn("Done file: " + doneFileName + " could not be deleted");
                }
            } catch (Exception e) {
                handleException(e);
            }
        }

        try {
            log.trace("Commit file strategy: {} for file: {}", processStrategy, file);
            processStrategy.commit(operations, endpoint, exchange, file);
        } catch (Exception e) {
            handleException(e);
        }
    }
