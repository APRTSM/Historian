    protected Processor createErrorHandler(RouteContext routeContext, Exchange exchange, Processor processor) {
        Processor answer;

        boolean tryBlock = exchange.getProperty(Exchange.TRY_ROUTE_BLOCK, false, boolean.class);

        // do not wrap in error handler if we are inside a try block
        if (!tryBlock && routeContext != null) {
            // wrap the producer in error handler so we have fine grained error handling on
            // the output side instead of the input side
            // this is needed to support redelivery on that output alone and not doing redelivery
            // for the entire multicast block again which will start from scratch again

            // create key for cache
            final PreparedErrorHandler key = new PreparedErrorHandler(routeContext, processor);

            // lookup cached first to reuse and preserve memory
            answer = errorHandlers.get(key);
            if (answer != null) {
                LOG.trace("Using existing error handler for: {}", processor);
                return answer;
            }

            LOG.trace("Creating error handler for: {}", processor);
            ErrorHandlerFactory builder = routeContext.getRoute().getErrorHandlerBuilder();
            // create error handler (create error handler directly to keep it light weight,
            // instead of using ProcessorDefinition.wrapInErrorHandler)
            try {
                processor = builder.createErrorHandler(routeContext, processor);

                // and wrap in unit of work processor so the copy exchange also can run under UoW
                answer = createUnitOfWorkProcessor(routeContext, processor, exchange);

                boolean child = exchange.getProperty(Exchange.PARENT_UNIT_OF_WORK, UnitOfWork.class) != null;

                // must start the error handler
                ServiceHelper.startServices(answer);

                // here we don't cache the child unit of work
                if (!child) {
                    // add to cache
                    errorHandlers.putIfAbsent(key, answer);
                }

            } catch (Exception e) {
                throw ObjectHelper.wrapRuntimeCamelException(e);
            }
        } else {
            // and wrap in unit of work processor so the copy exchange also can run under UoW
            answer = createUnitOfWorkProcessor(routeContext, processor, exchange);
        }

        return answer;
    }
    public boolean process(Exchange exchange, AsyncCallback callback) {
        Iterator<Processor> processors = next().iterator();

        Object lastHandled = exchange.getProperty(Exchange.EXCEPTION_HANDLED);
        exchange.setProperty(Exchange.EXCEPTION_HANDLED, null);

        while (continueRouting(processors, exchange)) {
            exchange.setProperty(Exchange.TRY_ROUTE_BLOCK, true);
            ExchangeHelper.prepareOutToIn(exchange);

            // process the next processor
            Processor processor = processors.next();
            AsyncProcessor async = AsyncProcessorConverterHelper.convert(processor);
            boolean sync = process(exchange, callback, processors, async, lastHandled);

            // continue as long its being processed synchronously
            if (!sync) {
                LOG.trace("Processing exchangeId: {} is continued being processed asynchronously", exchange.getExchangeId());
                // the remainder of the try .. catch .. finally will be completed async
                // so we break out now, then the callback will be invoked which then continue routing from where we left here
                return false;
            }

            LOG.trace("Processing exchangeId: {} is continued being processed synchronously", exchange.getExchangeId());
        }

        ExchangeHelper.prepareOutToIn(exchange);
        exchange.removeProperty(Exchange.TRY_ROUTE_BLOCK);
        exchange.setProperty(Exchange.EXCEPTION_HANDLED, lastHandled);
        LOG.trace("Processing complete for exchangeId: {} >>> {}", exchange.getExchangeId(), exchange);
        callback.done(true);
        return true;
    }
    protected boolean process(final Exchange exchange, final AsyncCallback callback,
                              final Iterator<Processor> processors, final AsyncProcessor processor,
                              final Object lastHandled) {
        // this does the actual processing so log at trace level
        LOG.trace("Processing exchangeId: {} >>> {}", exchange.getExchangeId(), exchange);

        // implement asynchronous routing logic in callback so we can have the callback being
        // triggered and then continue routing where we left
        boolean sync = processor.process(exchange, new AsyncCallback() {
            public void done(boolean doneSync) {
                // we only have to handle async completion of the pipeline
                if (doneSync) {
                    return;
                }

                // continue processing the try .. catch .. finally asynchronously
                while (continueRouting(processors, exchange)) {
                    exchange.setProperty(Exchange.TRY_ROUTE_BLOCK, true);
                    ExchangeHelper.prepareOutToIn(exchange);

                    // process the next processor
                    AsyncProcessor processor = AsyncProcessorConverterHelper.convert(processors.next());
                    doneSync = process(exchange, callback, processors, processor, lastHandled);

                    if (!doneSync) {
                        LOG.trace("Processing exchangeId: {} is continued being processed asynchronously", exchange.getExchangeId());
                        // the remainder of the try .. catch .. finally will be completed async
                        // so we break out now, then the callback will be invoked which then continue routing from where we left here
                        return;
                    }
                }

                ExchangeHelper.prepareOutToIn(exchange);
                exchange.removeProperty(Exchange.TRY_ROUTE_BLOCK);
                exchange.setProperty(Exchange.EXCEPTION_HANDLED, lastHandled);
                LOG.trace("Processing complete for exchangeId: {} >>> {}", exchange.getExchangeId(), exchange);
                callback.done(false);
            }
        });

        return sync;
    }
