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
