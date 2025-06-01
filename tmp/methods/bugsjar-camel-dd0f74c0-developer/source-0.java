    private boolean rejectExchange(final Exchange exchange, final AsyncCallback callback) {
        exchange.setException(new RejectedExecutionException("CircuitBreaker Open: failures: " + failures + ", lastFailure: " + lastFailure));
        /*
         * If the circuit opens, we have to prevent the execution of any
         * processor. The failures count can be set to 0.
         */
        failures.set(0);
        callback.done(true);
        return true;
    }
