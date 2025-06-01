    protected ExchangePattern resolveExchangePattern(Object recipient) throws UnsupportedEncodingException, URISyntaxException, MalformedURLException {
        // trim strings as end users might have added spaces between separators
        if (recipient instanceof String) {
            String s = ((String) recipient).trim();
            // see if exchangePattern is a parameter in the url
            s = URISupport.normalizeUri(s);
            return EndpointHelper.resolveExchangePatternFromUrl(s);
        }
        return null;
    }
    protected Iterable<ProcessorExchangePair> createProcessorExchangePairs(Exchange exchange) throws Exception {
        // here we iterate the recipient lists and create the exchange pair for each of those
        List<ProcessorExchangePair> result = new ArrayList<ProcessorExchangePair>();

        // at first we must lookup the endpoint and acquire the producer which can send to the endpoint
        int index = 0;
        while (iter.hasNext()) {
            Object recipient = iter.next();
            Endpoint endpoint;
            Producer producer;
            ExchangePattern pattern;
            try {
                endpoint = resolveEndpoint(exchange, recipient);
                pattern = resolveExchangePattern(recipient);
                producer = producerCache.acquireProducer(endpoint);
            } catch (Exception e) {
                if (isIgnoreInvalidEndpoints()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Endpoint uri is invalid: " + recipient + ". This exception will be ignored.", e);
                    }
                    continue;
                } else {
                    // failure so break out
                    throw e;
                }
            }

            // then create the exchange pair
            result.add(createProcessorExchangePair(index++, endpoint, producer, exchange, pattern));
        }

        return result;
    }
    public SendProcessor(Endpoint destination, ExchangePattern pattern, boolean unhandleException) {
        ObjectHelper.notNull(destination, "destination");
        this.destination = destination;
        this.camelContext = destination.getCamelContext();
        this.pattern = pattern;
        this.unhandleException = unhandleException;
        try {
            this.destinationExchangePattern = null;
            this.destinationExchangePattern = EndpointHelper.resolveExchangePatternFromUrl(destination.getEndpointUri());
        } catch (URISyntaxException e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }
        ObjectHelper.notNull(this.camelContext, "camelContext");
    }
    protected Exchange configureExchange(Exchange exchange, ExchangePattern pattern) {
        // destination exchange pattern overrides pattern
        if (destinationExchangePattern != null) {
            exchange.setPattern(destinationExchangePattern);
        } else if (pattern != null) {
            exchange.setPattern(pattern);
        }
        // set property which endpoint we send to
        exchange.setProperty(Exchange.TO_ENDPOINT, destination.getEndpointUri());
        return exchange;
    }
    public boolean process(Exchange exchange, final AsyncCallback callback) {
        if (!isStarted()) {
            exchange.setException(new IllegalStateException("SendProcessor has not been started: " + this));
            callback.done(true);
            return true;
        }


        // we should preserve existing MEP so remember old MEP
        // if you want to permanently to change the MEP then use .setExchangePattern in the DSL
        final ExchangePattern existingPattern = exchange.getPattern();

        // if we have a producer then use that as its optimized
        if (producer != null) {

            // record timing for sending the exchange using the producer
            final StopWatch watch = new StopWatch();

            final Exchange target = configureExchange(exchange, pattern);

            EventHelper.notifyExchangeSending(exchange.getContext(), target, destination);
            LOG.debug(">>>> {} {}", destination, exchange);

            boolean sync = true;
            try {
                sync = producer.process(exchange, new AsyncCallback() {
                    @Override
                    public void done(boolean doneSync) {
                        try {
                            // restore previous MEP
                            target.setPattern(existingPattern);
                            // emit event that the exchange was sent to the endpoint
                            long timeTaken = watch.stop();
                            EventHelper.notifyExchangeSent(target.getContext(), target, destination, timeTaken);
                        } finally {
                            checkException(target);
                            callback.done(doneSync);
                        }
                    }
                });
            } catch (Throwable throwable) {
                exchange.setException(throwable);
                checkException(exchange);
                callback.done(sync);
            }

            return sync;
        }

        // send the exchange to the destination using the producer cache for the non optimized producers
        return producerCache.doInAsyncProducer(destination, exchange, pattern, callback, new AsyncProducerCallback() {
            public boolean doInAsyncProducer(Producer producer, AsyncProcessor asyncProducer, final Exchange exchange,
                                             ExchangePattern pattern, final AsyncCallback callback) {
                final Exchange target = configureExchange(exchange, pattern);
                LOG.debug(">>>> {} {}", destination, exchange);
                return asyncProducer.process(target, new AsyncCallback() {
                    public void done(boolean doneSync) {
                        // restore previous MEP
                        target.setPattern(existingPattern);
                        checkException(target);
                        // signal we are done
                        callback.done(doneSync);
                    }
                });
            }
        });
    }
