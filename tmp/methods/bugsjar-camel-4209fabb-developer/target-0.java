        public void begin() {
            // we have already acquired and prepare the producer
            LOG.trace("RecipientProcessorExchangePair #{} begin: {}", index, exchange);
            exchange.setProperty(Exchange.RECIPIENT_LIST_ENDPOINT, endpoint.getEndpointUri());
            // ensure stream caching is reset
            MessageHelper.resetStreamCache(exchange.getIn());
        }
