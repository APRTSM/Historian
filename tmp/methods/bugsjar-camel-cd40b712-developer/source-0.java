    public boolean process(final Exchange exchange, final AsyncCallback callback) {
        Iterator<Processor> processors = next().iterator();

        // callback to restore existing FILTER_MATCHED property on the Exchange
        final Object existing = exchange.getProperty(Exchange.FILTER_MATCHED);
        final AsyncCallback choiceCallback = new AsyncCallback() {
            @Override
            public void done(boolean doneSync) {
                if (existing != null) {
                    exchange.setProperty(Exchange.FILTER_MATCHED, existing);
                } else {
                    exchange.removeProperty(Exchange.FILTER_MATCHED);
                }
                callback.done(doneSync);
            }
        };

        // as we only pick one processor to process, then no need to have async callback that has a while loop as well
        // as this should not happen, eg we pick the first filter processor that matches, or the otherwise (if present)
        // and if not, we just continue without using any processor
        while (processors.hasNext()) {
            // get the next processor
            Processor processor = processors.next();

            // evaluate the predicate on filter predicate early to be faster
            // and avoid issues when having nested choices
            // as we should only pick one processor
            boolean matches = true;
            if (processor instanceof FilterProcessor) {
                FilterProcessor filter = (FilterProcessor) processor;
                try {
                    matches = filter.getPredicate().matches(exchange);
                    exchange.setProperty(Exchange.FILTER_MATCHED, matches);
                } catch (Throwable e) {
                    exchange.setException(e);
                    choiceCallback.done(true);
                    return true;
                }
                // as we have pre evaluated the predicate then use its processor directly when routing
                processor = filter.getProcessor();
            }

            // if we did not match then continue to next filter
            if (!matches) {
                continue;
            }

            // okay we found a filter or its the otherwise we are processing
            AsyncProcessor async = AsyncProcessorConverterHelper.convert(processor);
            return async.process(exchange, choiceCallback);
        }

        // when no filter matches and there is no otherwise, then just continue
        choiceCallback.done(true);
        return true;
    }
