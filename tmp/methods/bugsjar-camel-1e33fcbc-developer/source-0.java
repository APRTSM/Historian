    protected void doDone(Exchange original, Exchange subExchange, final Iterable<ProcessorExchangePair> pairs,
                          AsyncCallback callback, boolean doneSync, boolean forceExhaust) {

        // we are done so close the pairs iterator
        if (pairs != null && pairs instanceof Closeable) {
            IOHelper.close((Closeable) pairs, "pairs", LOG);
        }

        // cleanup any per exchange aggregation strategy
        removeAggregationStrategyFromExchange(original);

        // we need to know if there was an exception, and if the stopOnException option was enabled
        // also we would need to know if any error handler has attempted redelivery and exhausted
        boolean stoppedOnException = false;
        boolean exception = false;
        boolean exhaust = forceExhaust || subExchange != null && (subExchange.getException() != null || ExchangeHelper.isRedeliveryExhausted(subExchange));
        if (original.getException() != null || subExchange != null && subExchange.getException() != null) {
            // there was an exception and we stopped
            stoppedOnException = isStopOnException();
            exception = true;
        }

        // must copy results at this point
        if (subExchange != null) {
            if (stoppedOnException) {
                // if we stopped due an exception then only propagte the exception
                original.setException(subExchange.getException());
            } else {
                // copy the current result to original so it will contain this result of this eip
                ExchangeHelper.copyResults(original, subExchange);
            }
        }

        // .. and then if there was an exception we need to configure the redelivery exhaust
        // for example the noErrorHandler will not cause redelivery exhaust so if this error
        // handled has been in use, then the exhaust would be false (if not forced)
        if (exception) {
            // multicast uses error handling on its output processors and they have tried to redeliver
            // so we shall signal back to the other error handlers that we are exhausted and they should not
            // also try to redeliver as we will then do that twice
            original.setProperty(Exchange.REDELIVERY_EXHAUSTED, exhaust);
        }

        callback.done(doneSync);
    }
