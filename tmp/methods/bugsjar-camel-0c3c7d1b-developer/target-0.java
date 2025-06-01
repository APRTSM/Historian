    protected static LRUCache<String, PollingConsumer> createLRUCache(int cacheSize) {
        // Use a regular cache as we want to ensure that the lifecycle of the consumers
        // being cache is properly handled, such as they are stopped when being evicted
        // or when this cache is stopped. This is needed as some consumers requires to
        // be stopped so they can shutdown internal resources that otherwise may cause leaks
        return new LRUCache<String, PollingConsumer>(cacheSize);
    }
