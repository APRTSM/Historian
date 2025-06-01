    public boolean doInAsyncProducer(Endpoint endpoint, Exchange exchange, ExchangePattern pattern, AsyncCallback callback, AsyncProducerCallback producerCallback) {
        boolean sync = true;

        // get the producer and we do not mind if its pooled as we can handle returning it back to the pool
        Producer producer = doGetProducer(endpoint, true);

        if (producer == null) {
            if (isStopped()) {
                LOG.warn("Ignoring exchange sent after processor is stopped: " + exchange);
                return false;
            } else {
                throw new IllegalStateException("No producer, this processor has not been started: " + this);
            }
        }

        StopWatch watch = null;
        if (exchange != null) {
            // record timing for sending the exchange using the producer
            watch = new StopWatch();
        }

        try {
            // invoke the callback
            AsyncProcessor asyncProcessor = AsyncProcessorTypeConverter.convert(producer);
            sync = producerCallback.doInAsyncProducer(producer, asyncProcessor, exchange, pattern, callback);
        } catch (Throwable e) {
            // ensure exceptions is caught and set on the exchange
            if (exchange != null) {
                exchange.setException(e);
            }
        } finally {
            if (exchange != null && exchange.getException() == null) {
                long timeTaken = watch.stop();
                // emit event that the exchange was sent to the endpoint
                EventHelper.notifyExchangeSent(exchange.getContext(), exchange, endpoint, timeTaken);
            }
            if (producer instanceof ServicePoolAware) {
                // release back to the pool
                pool.release(endpoint, producer);
            } else if (!producer.isSingleton()) {
                // stop non singleton producers as we should not leak resources
                try {
                    ServiceHelper.stopService(producer);
                } catch (Exception e) {
                    // ignore and continue
                    LOG.warn("Error stopping producer: " + producer, e);
                }
            }
        }

        return sync;
    }
