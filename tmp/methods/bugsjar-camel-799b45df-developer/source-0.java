    public String getTraceLabel() {
        return "circuitbreaker";
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
        }
        Processor processor = getProcessors().get(0);
        if (processor == null) {
            throw new IllegalStateException("No processors could be chosen to process CircuitBreaker");
        }

        AsyncProcessor albp = AsyncProcessorConverterHelper.convert(processor);
        boolean sync = albp.process(exchange, callback);

        boolean failed = hasFailed(exchange);

        if (!failed) {
            failures.set(0);
        } else {
            failures.incrementAndGet();
            lastFailure = System.currentTimeMillis();
        }

        if (!sync) {
            log.trace("Processing exchangeId: {} is continued being processed asynchronously", exchange.getExchangeId());
            return false;
        }

        log.trace("Processing exchangeId: {} is continued being processed synchronously", exchange.getExchangeId());
        callback.done(true);
        return true;
    }
