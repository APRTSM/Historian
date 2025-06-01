    protected boolean isInProgress(GenericFile<T> file) {
        String key = file.getAbsoluteFilePath();
        // must use add, to have operation as atomic
        return !endpoint.getInProgressRepository().add(key);
    }
    protected boolean isValidFile(GenericFile<T> file, boolean isDirectory, List<T> files) {
        if (!isMatched(file, isDirectory, files)) {
            log.trace("File did not match. Will skip this file: {}", file);
            return false;
        }

        // if its a file then check if its already in progress
        if (!isDirectory && isInProgress(file)) {
            if (log.isTraceEnabled()) {
                log.trace("Skipping as file is already in progress: {}", file.getFileName());
            }
            return false;
        }

        boolean answer = true;
        String key = null;
        try {
            // if its a file then check we have the file in the idempotent registry already
            if (!isDirectory && endpoint.isIdempotent()) {
                // use absolute file path as default key, but evaluate if an expression key was configured
                key = file.getAbsoluteFilePath();
                if (endpoint.getIdempotentKey() != null) {
                    Exchange dummy = endpoint.createExchange(file);
                    key = endpoint.getIdempotentKey().evaluate(dummy, String.class);
                }
                if (key != null && endpoint.getIdempotentRepository().contains(key)) {
                    log.trace("This consumer is idempotent and the file has been consumed before. Will skip this file: {}", file);
                    answer = false;
                }
            }
        } finally {
            // ensure to run this in finally block in case of runtime exceptions being thrown
            if (!answer) {
                // remove file from the in progress list as its no longer in progress
                endpoint.getInProgressRepository().remove(key);
            }
        }

        // file matched
        return answer;
    }
