    public OnExceptionDefinition onException(Class exception) {
        // is only allowed at the top currently
        if (!routeCollection.getRoutes().isEmpty()) {
            throw new IllegalArgumentException("onException must be defined before any routes in the RouteBuilder");
        }
        routeCollection.setCamelContext(getContext());
        return routeCollection.onException(exception);
    }
    public InterceptFromDefinition interceptFrom(String uri) {
        if (!routeCollection.getRoutes().isEmpty()) {
            throw new IllegalArgumentException("interceptFrom must be defined before any routes in the RouteBuilder");
        }
        routeCollection.setCamelContext(getContext());
        return routeCollection.interceptFrom(uri);
    }
    public OnCompletionDefinition onCompletion() {
        // is only allowed at the top currently
        if (!routeCollection.getRoutes().isEmpty()) {
            throw new IllegalArgumentException("onCompletion must be defined before any routes in the RouteBuilder");
        }
        routeCollection.setCamelContext(getContext());
        return routeCollection.onCompletion();
    }
    public RouteBuilder errorHandler(ErrorHandlerBuilder errorHandlerBuilder) {
        if (!routeCollection.getRoutes().isEmpty()) {
            throw new IllegalArgumentException("errorHandler must be defined before any routes in the RouteBuilder");
        }
        routeCollection.setCamelContext(getContext());
        setErrorHandlerBuilder(errorHandlerBuilder);
        return this;
    }
    public InterceptDefinition intercept() {
        if (!routeCollection.getRoutes().isEmpty()) {
            throw new IllegalArgumentException("intercept must be defined before any routes in the RouteBuilder");
        }
        routeCollection.setCamelContext(getContext());
        return routeCollection.intercept();
    }
    public InterceptSendToEndpointDefinition interceptSendToEndpoint(String uri) {
        if (!routeCollection.getRoutes().isEmpty()) {
            throw new IllegalArgumentException("interceptSendToEndpoint must be defined before any routes in the RouteBuilder");
        }
        routeCollection.setCamelContext(getContext());
        return routeCollection.interceptSendToEndpoint(uri);
    }
    public InterceptFromDefinition interceptFrom() {
        if (!routeCollection.getRoutes().isEmpty()) {
            throw new IllegalArgumentException("interceptFrom must be defined before any routes in the RouteBuilder");
        }
        routeCollection.setCamelContext(getContext());
        return routeCollection.interceptFrom();
    }
