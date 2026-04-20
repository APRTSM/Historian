    public void releaseProducer(Endpoint endpoint, Producer producer) throws Exception {
        if (producer instanceof ServicePoolAware) {
            // release back to the pool
            pool.release(endpoint, producer);
        } else if (!producer.isSingleton()) {
            // stop non singleton producers as we should not leak resources
            producer.stop();
        }
    }
