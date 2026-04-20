    protected void processStrategyCommit(GenericFileProcessStrategy<T> processStrategy,
                                         Exchange exchange, GenericFile<T> file) {
        if (endpoint.isIdempotent()) {

            // use absolute file path as default key, but evaluate if an expression key was configured
            String key = absoluteFileName;
            if (endpoint.getIdempotentKey() != null) {
                Exchange dummy = endpoint.createExchange(file);
                key = endpoint.getIdempotentKey().evaluate(dummy, String.class);
            }

            // only add to idempotent repository if we could process the file
            if (key != null) {
                endpoint.getIdempotentRepository().add(key);
            }
        }

        // must be last in batch to delete the done file name
        // delete done file if used (and not noop=true)
        boolean complete = exchange.getProperty(Exchange.BATCH_COMPLETE, false, Boolean.class);
        if (endpoint.getDoneFileName() != null && !endpoint.isNoop()) {
            // done file must be in same path as the original input file
            String doneFileName = endpoint.createDoneFileName(absoluteFileName);
            ObjectHelper.notEmpty(doneFileName, "doneFileName", endpoint);
            // we should delete the dynamic done file 
            if (endpoint.getDoneFileName().indexOf("{file:name") > 0 || complete) { 
                try {
                    // delete done file
                    boolean deleted = operations.deleteFile(doneFileName);
                    log.trace("Done file: {} was deleted: {}", doneFileName, deleted);
                    if (!deleted) {
                        log.warn("Done file: " + doneFileName + " could not be deleted");
                    }
                } catch (Exception e) {
                    handleException("Error deleting done file: " + doneFileName, exchange, e);
                }
            }
        }

        try {
            log.trace("Commit file strategy: {} for file: {}", processStrategy, file);
            processStrategy.commit(operations, endpoint, exchange, file);
        } catch (Exception e) {
            handleException("Error during commit", exchange, e);
        }
    }
    protected void processStrategyRollback(GenericFileProcessStrategy<T> processStrategy,
                                           Exchange exchange, GenericFile<T> file) {

        if (log.isWarnEnabled()) {
            log.warn("Rollback file strategy: " + processStrategy + " for file: " + file);
        }
        try {
            processStrategy.rollback(operations, endpoint, exchange, file);
        } catch (Exception e) {
            handleException("Error during rollback", exchange, e);
        }
    }
