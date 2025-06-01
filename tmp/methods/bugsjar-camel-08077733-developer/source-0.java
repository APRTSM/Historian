    public ProducerCache(Object source, CamelContext camelContext, Map<String, Producer> cache) {
        this(source, camelContext, camelContext.getProducerServicePool(), cache);
    }
    protected void doStop() throws Exception {
        // when stopping we intend to shutdown
        ServiceHelper.stopAndShutdownServices(statistics, pool);
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
        this.pool = producerServicePool;
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
        this(source, camelContext, camelContext.getProducerServicePool(), createLRUCache(cacheSize));
    }
