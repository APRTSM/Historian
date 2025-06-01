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

        if (exchange.getException() != null) {
            // force any exceptions occurred during creation of exchange paris to be thrown
            // before returning the answer;
            throw exchange.getException();
        }

        return result;
    }
    protected Iterable<ProcessorExchangePair> createProcessorExchangePairs(Exchange exchange) throws Exception {
        Object value = expression.evaluate(exchange, Object.class);
        if (exchange.getException() != null) {
            // force any exceptions occurred during evaluation to be thrown
            throw exchange.getException();
        }

        Iterable<ProcessorExchangePair> answer;
        if (isStreaming()) {
            answer = createProcessorExchangePairsIterable(exchange, value);
        } else {
            answer = createProcessorExchangePairsList(exchange, value);
        }
        if (exchange.getException() != null) {
            // force any exceptions occurred during creation of exchange paris to be thrown
            // before returning the answer;
            throw exchange.getException();
        }

        return answer;
    }
