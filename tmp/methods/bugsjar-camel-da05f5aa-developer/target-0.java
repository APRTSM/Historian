    protected void processEmptyMessage() throws Exception {
        Exchange exchange = getEndpoint().createExchange();
        // enrich exchange, so we send an empty message with the batch details
        exchange.setProperty(Exchange.BATCH_INDEX, 0);
        exchange.setProperty(Exchange.BATCH_SIZE, 1);
        exchange.setProperty(Exchange.BATCH_COMPLETE, true);
        log.debug("Sending empty message as there were no messages from polling: {}", this.getEndpoint());
        getProcessor().process(exchange);
    }
