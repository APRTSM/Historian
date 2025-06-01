    private synchronized void addArrivedRecordsInput(ProcessRecordsInput processRecordsInput) throws InterruptedException {
        getRecordsResultQueue.put(processRecordsInput);
        prefetchCounters.added(processRecordsInput);
    }
