    public boolean process(final Exchange exchange, final AsyncCallback callback) {
        // clear exception so finally block can be executed
        final Exception e = exchange.getException();
        exchange.setException(null);
        // but store the caught exception as a property
        if (e != null) {
            exchange.setProperty(Exchange.EXCEPTION_CAUGHT, e);
        }
        // store the last to endpoint as the failure endpoint
        if (exchange.getProperty(Exchange.FAILURE_ENDPOINT) == null) {
            exchange.setProperty(Exchange.FAILURE_ENDPOINT, exchange.getProperty(Exchange.TO_ENDPOINT));
        }

        boolean sync = processor.process(exchange, new AsyncCallback() {
            public void done(boolean doneSync) {
                if (e == null) {
                    exchange.removeProperty(Exchange.FAILURE_ENDPOINT);
                } else {
                    // set exception back on exchange
                    exchange.setException(e);
                    exchange.setProperty(Exchange.EXCEPTION_CAUGHT, e);
                }

                if (!doneSync) {
                    // signal callback to continue routing async
                    ExchangeHelper.prepareOutToIn(exchange);
                    LOG.trace("Processing complete for exchangeId: {} >>> {}", exchange.getExchangeId(), exchange);
                }
                callback.done(doneSync);
            }
        });
        return sync;
    }
    public void setId(String id) {
        this.id = id;
    }
