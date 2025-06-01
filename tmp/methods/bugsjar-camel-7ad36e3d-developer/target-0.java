    public boolean doInAsyncProducer(final Endpoint endpoint, final Exchange exchange, final ExchangePattern pattern,
                                     final AsyncCallback callback, final AsyncProducerCallback producerCallback) {

        Producer target;
        try {
            // get the producer and we do not mind if its pooled as we can handle returning it back to the pool
            target = doGetProducer(endpoint, true);

            if (target == null) {
                if (isStopped()) {
                    LOG.warn("Ignoring exchange sent after processor is stopped: " + exchange);
                    callback.done(true);
                    return true;
                } else {
                    exchange.setException(new IllegalStateException("No producer, this processor has not been started: " + this));
                    callback.done(true);
                    return true;
                }
            }
        } catch (Throwable e) {
            exchange.setException(e);
            callback.done(true);
            return true;
        }

        final Producer producer = target;

        // record timing for sending the exchange using the producer
        final StopWatch watch = eventNotifierEnabled && exchange != null ? new StopWatch() : null;

        try {
            if (eventNotifierEnabled && exchange != null) {
                EventHelper.notifyExchangeSending(exchange.getContext(), exchange, endpoint);
            }
            // invoke the callback
            AsyncProcessor asyncProcessor = AsyncProcessorConverterHelper.convert(producer);
            return producerCallback.doInAsyncProducer(producer, asyncProcessor, exchange, pattern, new AsyncCallback() {
                @Override
                public void done(boolean doneSync) {
                    try {
                        if (eventNotifierEnabled && watch != null) {
                            long timeTaken = watch.stop();
                            // emit event that the exchange was sent to the endpoint
                            EventHelper.notifyExchangeSent(exchange.getContext(), exchange, endpoint, timeTaken);
                        }

                        if (producer instanceof ServicePoolAware) {
                            // release back to the pool
                            pool.release(endpoint, producer);
                        } else if (!producer.isSingleton()) {
                            // stop and shutdown non-singleton producers as we should not leak resources
                            try {
                                ServiceHelper.stopAndShutdownService(producer);
                            } catch (Exception e) {
                                // ignore and continue
                                LOG.warn("Error stopping/shutting down producer: " + producer, e);
                            }
                        }
                    } finally {
                        callback.done(doneSync);
                    }
                }
            });
        } catch (Throwable e) {
            // ensure exceptions is caught and set on the exchange
            if (exchange != null) {
                exchange.setException(e);
            }
            callback.done(true);
            return true;
        }
    }
