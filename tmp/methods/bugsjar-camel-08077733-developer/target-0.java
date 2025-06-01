    protected void doStop() throws Exception {
        // when stopping we intend to shutdown
        ServiceHelper.stopAndShutdownService(statistics);
        if (stopServicePool) {
            ServiceHelper.stopAndShutdownService(pool);
        }
        try {
            ServiceHelper.stopAndShutdownServices(producers.values());
        } finally {
            // ensure producers are removed, and also from JMX
            for (Producer producer : producers.values()) {
                getCamelContext().removeService(producer);
            }
        }
        producers.clear();
        if (statistics != null) {
            statistics.clear();
        }
    }
    public ProducerCache(Object source, CamelContext camelContext, ServicePool<Endpoint, Producer> producerServicePool, Map<String, Producer> cache) {
        this.source = source;
        this.camelContext = camelContext;
        if (producerServicePool == null) {
            // use shared producer pool which lifecycle is managed by CamelContext
            this.pool = camelContext.getProducerServicePool();
            this.stopServicePool = false;
        } else {
            this.pool = producerServicePool;
            this.stopServicePool = true;
        }
        this.producers = cache;
        if (producers instanceof LRUCache) {
            maxCacheSize = ((LRUCache) producers).getMaxCacheSize();
        }

        // only if JMX is enabled
        if (camelContext.getManagementStrategy().getManagementAgent() != null) {
            this.extendedStatistics = camelContext.getManagementStrategy().getManagementAgent().getStatisticsLevel().isExtended();
        } else {
            this.extendedStatistics = false;
        }
    }
    public ProducerCache(Object source, CamelContext camelContext, int cacheSize) {
        this(source, camelContext, null, createLRUCache(cacheSize));
    }
    public ProducerCache(Object source, CamelContext camelContext, Map<String, Producer> cache) {
        this(source, camelContext, null, cache);
    }
