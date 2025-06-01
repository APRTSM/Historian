        public String toString() {
            return "FinallyAsyncCallback";
        }
    public boolean process(final Exchange exchange, final AsyncCallback callback) {
        // clear exception and fault so finally block can be executed
        final boolean fault;
        if (exchange.hasOut()) {
            fault = exchange.getOut().isFault();
            exchange.getOut().setFault(false);
        } else {
            fault = exchange.getIn().isFault();
            exchange.getIn().setFault(false);
        }

        final Exception exception = exchange.getException();
        exchange.setException(null);
        // but store the caught exception as a property
        if (exception != null) {
            exchange.setProperty(Exchange.EXCEPTION_CAUGHT, exception);
        }

        // store the last to endpoint as the failure endpoint
        if (exchange.getProperty(Exchange.FAILURE_ENDPOINT) == null) {
            exchange.setProperty(Exchange.FAILURE_ENDPOINT, exchange.getProperty(Exchange.TO_ENDPOINT));
        }

        // continue processing
        return processor.process(exchange, new FinallyAsyncCallback(exchange, callback, exception, fault));
    }
        public FinallyAsyncCallback(Exchange exchange, AsyncCallback callback, Exception exception, boolean fault) {
            this.exchange = exchange;
            this.callback = callback;
            this.exception = exception;
            this.fault = fault;
        }
        public void done(boolean doneSync) {
            try {
                if (exception == null) {
                    exchange.removeProperty(Exchange.FAILURE_ENDPOINT);
                } else {
                    // set exception back on exchange
                    exchange.setException(exception);
                    exchange.setProperty(Exchange.EXCEPTION_CAUGHT, exception);
                }
                // set fault flag back
                if (fault) {
                    if (exchange.hasOut()) {
                        exchange.getOut().setFault(true);
                    } else {
                        exchange.getIn().setFault(true);
                    }
                }

                if (!doneSync) {
                    // signal callback to continue routing async
                    ExchangeHelper.prepareOutToIn(exchange);
                    LOG.trace("Processing complete for exchangeId: {} >>> {}", exchange.getExchangeId(), exchange);
                }
            } finally {
                // callback must always be called
                callback.done(doneSync);
            }
        }
