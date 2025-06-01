    protected Iterable<ProcessorExchangePair> createProcessorExchangePairs(Exchange exchange) throws Exception {
        List<ProcessorExchangePair> result = new ArrayList<ProcessorExchangePair>(processors.size());

        int index = 0;
        for (Processor processor : processors) {
            // copy exchange, and do not share the unit of work
            Exchange copy = ExchangeHelper.createCorrelatedCopy(exchange, false);

            // if we share unit of work, we need to prepare the child exchange
            if (isShareUnitOfWork()) {
                prepareSharedUnitOfWork(copy, exchange);
            }

            // and add the pair
            RouteContext routeContext = exchange.getUnitOfWork() != null ? exchange.getUnitOfWork().getRouteContext() : null;
            result.add(createProcessorExchangePair(index++, processor, copy, routeContext));
        }

        return result;
    }
    protected Iterable<ProcessorExchangePair> createProcessorExchangePairs(Exchange exchange) {
        Object value = expression.evaluate(exchange, Object.class);

        if (isStreaming()) {
            return createProcessorExchangePairsIterable(exchange, value);
        } else {
            return createProcessorExchangePairsList(exchange, value);
        }
    }
