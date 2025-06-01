    public void process(Exchange exchange) throws Exception {
        // compute correlation expression
        String key = correlationExpression.evaluate(exchange, String.class);
        if (ObjectHelper.isEmpty(key)) {
            // we have a bad correlation key
            if (isIgnoreInvalidCorrelationKeys()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Invalid correlation key. This Exchange will be ignored: " + exchange);
                }
                return;
            } else {
                throw new CamelExchangeException("Invalid correlation key", exchange);
            }
        }

        // is the correlation key closed?
        if (closedCorrelationKeys != null && closedCorrelationKeys.containsKey(key)) {
            throw new ClosedCorrelationKeyException(key, exchange);
        }

        // when memory based then its fast using synchronized, but if the aggregation repository is IO
        // bound such as JPA etc then concurrent aggregation per correlation key could
        // improve performance as we can run aggregation repository get/add in parallel
        lock.lock();
        try {
            doAggregation(key, exchange);
        } finally {
            lock.unlock();
        }
    }
