    private boolean rejectExchange(final Exchange exchange, final AsyncCallback callback) {
        exchange.setException(new RejectedExecutionException("CircuitBreaker Open: failures: " + failures + ", lastFailure: " + lastFailure));
        callback.done(true);
        return true;
    }
