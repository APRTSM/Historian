    protected boolean deliverToFailureProcessor(final Processor processor, final Exchange exchange,
                                                final RedeliveryData data, final AsyncCallback callback) {
        boolean sync = true;

        Exception caught = exchange.getException();

        // we did not success with the redelivery so now we let the failure processor handle it
        // clear exception as we let the failure processor handle it
        exchange.setException(null);

        boolean handled = false;
        // regard both handled or continued as being handled
        if (shouldHandled(exchange, data) || shouldContinue(exchange, data)) {
            // its handled then remove traces of redelivery attempted
            exchange.getIn().removeHeader(Exchange.REDELIVERED);
            exchange.getIn().removeHeader(Exchange.REDELIVERY_COUNTER);
            exchange.getIn().removeHeader(Exchange.REDELIVERY_MAX_COUNTER);
            handled = true;
        } else {
            // must decrement the redelivery counter as we didn't process the redelivery but is
            // handling by the failure handler. So we must -1 to not let the counter be out-of-sync
            decrementRedeliveryCounter(exchange);
        }

        // is the a failure processor to process the Exchange
        if (processor != null) {

            // reset cached streams so they can be read again
            MessageHelper.resetStreamCache(exchange.getIn());

            // prepare original IN body if it should be moved instead of current body
            if (data.useOriginalInMessage) {
                if (log.isTraceEnabled()) {
                    log.trace("Using the original IN message instead of current");
                }

                Message original = exchange.getUnitOfWork().getOriginalInMessage();
                exchange.setIn(original);
            }

            if (log.isTraceEnabled()) {
                log.trace("Failure processor " + processor + " is processing Exchange: " + exchange);
            }

            // store the last to endpoint as the failure endpoint
            exchange.setProperty(Exchange.FAILURE_ENDPOINT, exchange.getProperty(Exchange.TO_ENDPOINT));

            // the failure processor could also be asynchronous
            AsyncProcessor afp = AsyncProcessorTypeConverter.convert(processor);
            sync = AsyncProcessorHelper.process(afp, exchange, new AsyncCallback() {
                public void done(boolean sync) {
                    if (log.isTraceEnabled()) {
                        log.trace("Failure processor done: " + processor + " processing Exchange: " + exchange);
                    }
                    try {
                        prepareExchangeAfterFailure(exchange, data);
                        // fire event as we had a failure processor to handle it, which there is a event for
                        boolean deadLetterChannel = processor == data.deadLetterProcessor && data.deadLetterProcessor != null;
                        EventHelper.notifyExchangeFailureHandled(exchange.getContext(), exchange, processor, deadLetterChannel);
                    } finally {
                        // if the fault was handled asynchronously, this should be reflected in the callback as well
                        data.sync &= sync;
                        callback.done(data.sync);
                    }
                }
            });
        } else {
            try {
                // no processor but we need to prepare after failure as well
                prepareExchangeAfterFailure(exchange, data);
            } finally {
                // callback we are done
                callback.done(data.sync);
            }
        }

        // create log message
        String msg = "Failed delivery for exchangeId: " + exchange.getExchangeId();
        msg = msg + ". Exhausted after delivery attempt: " + data.redeliveryCounter + " caught: " + caught;
        if (processor != null) {
            msg = msg + ". Processed by failure processor: " + processor;
        }

        // log that we failed delivery as we are exhausted
        logFailedDelivery(false, handled, false, exchange, msg, data, null);

        return sync;
    }
