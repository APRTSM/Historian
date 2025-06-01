    protected static LRUCache<String, PollingConsumer> createLRUCache(int cacheSize) {
        // We use a soft reference cache to allow the JVM to re-claim memory if it runs low on memory.
        return new LRUSoftCache<String, PollingConsumer>(cacheSize);
    }
