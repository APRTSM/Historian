    public boolean process(Exchange exchange, final AsyncCallback callback) {
        if (!isStarted()) {
            exchange.setException(new IllegalStateException("SendProcessor has not been started: " + this));
            callback.done(true);
            return true;
        }

        // we should preserve existing MEP so remember old MEP
        // if you want to permanently to change the MEP then use .setExchangePattern in the DSL
        final ExchangePattern existingPattern = exchange.getPattern();

        // if we have a producer then use that as its optimized
        if (producer != null) {

            // record timing for sending the exchange using the producer
            final StopWatch watch = new StopWatch();

            final Exchange target = configureExchange(exchange, pattern);

            EventHelper.notifyExchangeSending(exchange.getContext(), target, destination);
            LOG.debug(">>>> {} {}", destination, exchange);
            return producer.process(exchange, new AsyncCallback() {
                @Override
                public void done(boolean doneSync) {
                    try {
                        // restore previous MEP
                        target.setPattern(existingPattern);
                        // emit event that the exchange was sent to the endpoint
                        long timeTaken = watch.stop();
                        EventHelper.notifyExchangeSent(target.getContext(), target, destination, timeTaken);
                    } finally {
                        callback.done(doneSync);
                    }
                }
            });
        }

        // send the exchange to the destination using the producer cache for the non optimized producers
        return producerCache.doInAsyncProducer(destination, exchange, pattern, callback, new AsyncProducerCallback() {
            public boolean doInAsyncProducer(Producer producer, AsyncProcessor asyncProducer, final Exchange exchange,
                                             ExchangePattern pattern, final AsyncCallback callback) {
                final Exchange target = configureExchange(exchange, pattern);
                LOG.debug(">>>> {} {}", destination, exchange);
                return asyncProducer.process(target, new AsyncCallback() {
                    public void done(boolean doneSync) {
                        // restore previous MEP
                        target.setPattern(existingPattern);
                        // signal we are done
                        callback.done(doneSync);
                    }
                });
            }
        });
    }
