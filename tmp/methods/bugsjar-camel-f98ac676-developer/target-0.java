    private boolean doRoutingSlip(final Exchange exchange, final AsyncCallback callback) {
        Exchange current = exchange;
        RoutingSlipIterator iter;
        try {
            iter = createRoutingSlipIterator(exchange);
        } catch (Exception e) {
            exchange.setException(e);
            callback.done(true);
            return true;
        }

        // ensure the slip is empty when we start
        if (current.hasProperties()) {
            current.setProperty(Exchange.SLIP_ENDPOINT, null);
        }

        while (iter.hasNext(current)) {
            Endpoint endpoint;
            try {
                endpoint = resolveEndpoint(iter, exchange);
                // if no endpoint was resolved then try the next
                if (endpoint == null) {
                    continue;
                }
            } catch (Exception e) {
                // error resolving endpoint so we should break out
                current.setException(e);
                break;
            }

            // prepare and process the routing slip
            Exchange copy = prepareExchangeForRoutingSlip(current, endpoint);
            boolean sync = processExchange(endpoint, copy, exchange, callback, iter);
            current = copy;

            if (!sync) {
                log.trace("Processing exchangeId: {} is continued being processed asynchronously", exchange.getExchangeId());
                // the remainder of the routing slip will be completed async
                // so we break out now, then the callback will be invoked which then continue routing from where we left here
                return false;
            }

            log.trace("Processing exchangeId: {} is continued being processed synchronously", exchange.getExchangeId());

            // we ignore some kind of exceptions and allow us to continue
            if (isIgnoreInvalidEndpoints()) {
                FailedToCreateProducerException e = current.getException(FailedToCreateProducerException.class);
                if (e != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Endpoint uri is invalid: " + endpoint + ". This exception will be ignored.", e);
                    }
                    current.setException(null);
                }
            }

            // Decide whether to continue with the recipients or not; similar logic to the Pipeline
            // check for error if so we should break out
            if (!continueProcessing(current, "so breaking out of the routing slip", log)) {
                break;
            }
        }

        // logging nextExchange as it contains the exchange that might have altered the payload and since
        // we are logging the completion if will be confusing if we log the original instead
        // we could also consider logging the original and the nextExchange then we have *before* and *after* snapshots
        log.trace("Processing complete for exchangeId: {} >>> {}", exchange.getExchangeId(), current);

        // copy results back to the original exchange
        ExchangeHelper.copyResults(exchange, current);

        callback.done(true);
        return true;
    }
