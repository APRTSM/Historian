        CircuitBreakerCallback(Exchange exchange, AsyncCallback callback) {
            this.callback = callback;
            this.exchange = exchange;
        }
    public boolean process(final Exchange exchange, final AsyncCallback callback) {

        // can we still run
        if (!isRunAllowed()) {
            log.trace("Run not allowed, will reject executing exchange: {}", exchange);
            if (exchange.getException() == null) {
                exchange.setException(new RejectedExecutionException("Run is not allowed"));
            }
            callback.done(true);
            return true;
        }

        if (failures.get() >= threshold && System.currentTimeMillis() - lastFailure < halfOpenAfter) {
            exchange.setException(new RejectedExecutionException("CircuitBreaker Open: failures: " + failures + ", lastFailure: " + lastFailure));
            /*
             * If the circuit opens, we have to prevent the execution of any processor.
             * The failures count can be set to 0.
             */
            failures.set(0);
            callback.done(true);
            return true;
        }
        Processor processor = getProcessors().get(0);
        if (processor == null) {
            throw new IllegalStateException("No processors could be chosen to process CircuitBreaker");
        }

        AsyncProcessor albp = AsyncProcessorConverterHelper.convert(processor);
        // Added a callback for processing the exchange in the callback
        boolean sync = albp.process(exchange, new CircuitBreakerCallback(exchange, callback));

        // We need to check the exception here as albp is use sync call
        if (sync) {
            boolean failed = hasFailed(exchange);
            if (!failed) {
                failures.set(0);
            } else {
                failures.incrementAndGet();
                lastFailure = System.currentTimeMillis();
            }
        } else {
            // CircuitBreakerCallback can take care of failure check of the exchange
            log.trace("Processing exchangeId: {} is continued being processed asynchronously", exchange.getExchangeId());
            return false;
        }

        log.trace("Processing exchangeId: {} is continued being processed synchronously", exchange.getExchangeId());
        callback.done(true);
        return true;
    }
        public void done(boolean doneSync) {
            if (!doneSync) {
                boolean failed = hasFailed(exchange);
                if (!failed) {
                    failures.set(0);
                } else {
                    failures.incrementAndGet();
                    lastFailure = System.currentTimeMillis();
                }
            }
            callback.done(doneSync);
        }
