    protected ExchangePattern resolveExchangePattern(Exchange exchange, Object recipient) throws UnsupportedEncodingException, URISyntaxException {
        // trim strings as end users might have added spaces between separators
        if (recipient instanceof String) {
            String s = ((String) recipient).trim();
            // see if exchangePattern is a parameter in the url
            s = URISupport.normalizeUri(s);
            URI url = new URI(s);
            Map<String, Object> parameters = URISupport.parseParameters(url);
            String pattern = (String) parameters.get("exchangePattern");
            if (pattern != null) {
                return ExchangePattern.asEnum(pattern);
            }
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
                pattern = resolveExchangePattern(exchange, recipient);
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
