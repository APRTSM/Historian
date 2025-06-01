    public InterceptFromDefinition interceptFrom(String uri) {
        routeCollection.setCamelContext(getContext());
        return routeCollection.interceptFrom(uri);
    }
    public InterceptFromDefinition interceptFrom() {
        routeCollection.setCamelContext(getContext());
        return routeCollection.interceptFrom();
    }
    public InterceptSendToEndpointDefinition interceptSendToEndpoint(String uri) {
        routeCollection.setCamelContext(getContext());
        return routeCollection.interceptSendToEndpoint(uri);
    }
    public RouteBuilder errorHandler(ErrorHandlerBuilder errorHandlerBuilder) {
        routeCollection.setCamelContext(getContext());
        setErrorHandlerBuilder(errorHandlerBuilder);
        return this;
    }
    public OnExceptionDefinition onException(Class exception) {
        routeCollection.setCamelContext(getContext());
        return routeCollection.onException(exception);
    }
    public InterceptDefinition intercept() {
        routeCollection.setCamelContext(getContext());
        return routeCollection.intercept();
    }
    public OnCompletionDefinition onCompletion() {
        routeCollection.setCamelContext(getContext());
        return routeCollection.onCompletion();
    }
