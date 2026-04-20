    public ChoiceDefinition endChoice() {
        // are we already a choice?
        ProcessorDefinition<?> def = this;
        if (def instanceof ChoiceDefinition) {
            return (ChoiceDefinition) def;
        }

        // okay end this and get back to the choice
        def = end();
        if (def instanceof WhenDefinition) {
            return (ChoiceDefinition) def.getParent();
        } else if (def instanceof OtherwiseDefinition) {
            return (ChoiceDefinition) def.getParent();
        } else {
            return (ChoiceDefinition) def;
        }
    }
    public boolean process(Exchange exchange, AsyncCallback callback) {
        Iterator<Processor> processors = next().iterator();

        exchange.setProperty(Exchange.FILTER_MATCHED, false);
        while (continueRouting(processors, exchange)) {
            // get the next processor
            Processor processor = processors.next();

            AsyncProcessor async = AsyncProcessorConverterHelper.convert(processor);
            boolean sync = process(exchange, callback, processors, async);

            // continue as long its being processed synchronously
            if (!sync) {
                LOG.trace("Processing exchangeId: {} is continued being processed asynchronously", exchange.getExchangeId());
                // the remainder of the CBR will be completed async
                // so we break out now, then the callback will be invoked which then continue routing from where we left here
                return false;
            }

            LOG.trace("Processing exchangeId: {} is continued being processed synchronously", exchange.getExchangeId());

            // check for error if so we should break out
            if (!continueProcessing(exchange, "so breaking out of content based router", LOG)) {
                break;
            }
        }

        LOG.trace("Processing complete for exchangeId: {} >>> {}", exchange.getExchangeId(), exchange);

        callback.done(true);
        return true;
    }
    private boolean process(final Exchange exchange, final AsyncCallback callback,
                            final Iterator<Processor> processors, final AsyncProcessor asyncProcessor) {
        // this does the actual processing so log at trace level
        LOG.trace("Processing exchangeId: {} >>> {}", exchange.getExchangeId(), exchange);

        // implement asynchronous routing logic in callback so we can have the callback being
        // triggered and then continue routing where we left
        boolean sync = asyncProcessor.process(exchange, new AsyncCallback() {
            public void done(boolean doneSync) {
                // we only have to handle async completion of the pipeline
                if (doneSync) {
                    return;
                }

                // continue processing the pipeline asynchronously
                while (continueRouting(processors, exchange)) {
                    AsyncProcessor processor = AsyncProcessorConverterHelper.convert(processors.next());

                    // check for error if so we should break out
                    if (!continueProcessing(exchange, "so breaking out of pipeline", LOG)) {
                        break;
                    }

                    doneSync = process(exchange, callback, processors, processor);
                    if (!doneSync) {
                        LOG.trace("Processing exchangeId: {} is continued being processed asynchronously", exchange.getExchangeId());
                        return;
                    }
                }

                LOG.trace("Processing complete for exchangeId: {} >>> {}", exchange.getExchangeId(), exchange);
                callback.done(false);
            }
        });

        return sync;
    }
    protected boolean continueRouting(Iterator<Processor> it, Exchange exchange) {
        boolean answer = it.hasNext();
        if (answer) {
            Object matched = exchange.getProperty(Exchange.FILTER_MATCHED);
            if (matched != null) {
                boolean hasMatched = exchange.getContext().getTypeConverter().convertTo(Boolean.class, matched);
                if (hasMatched) {
                    LOG.debug("ExchangeId: {} has been matched: {}", exchange.getExchangeId(), exchange);
                    answer = false;
                }
            }
        }
        LOG.trace("ExchangeId: {} should continue matching: {}", exchange.getExchangeId(), answer);
        return answer;
    }
