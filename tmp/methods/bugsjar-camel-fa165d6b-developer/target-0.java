    public Processor createProcessor(final RouteContext routeContext) throws Exception {
        // create the detour
        final Processor detour = this.createChildProcessor(routeContext, true);

        // register endpoint callback so we can proxy the endpoint
        routeContext.getCamelContext().addRegisterEndpointCallback(new EndpointStrategy() {
            public Endpoint registerEndpoint(String uri, Endpoint endpoint) {
                if (endpoint instanceof InterceptSendToEndpoint) {
                    // endpoint already decorated
                    return endpoint;
                } else if (getUri() == null || matchPattern(routeContext.getCamelContext(), uri, getUri())) {
                    // only proxy if the uri is matched decorate endpoint with our proxy
                    // should be false by default
                    boolean skip = isSkipSendToOriginalEndpoint();
                    InterceptSendToEndpoint proxy = new InterceptSendToEndpoint(endpoint, skip);
                    proxy.setDetour(detour);
                    return proxy;
                } else {
                    // no proxy so return regular endpoint
                    return endpoint;
                }
            }
        });


        // remove the original intercepted route from the outputs as we do not intercept as the regular interceptor
        // instead we use the proxy endpoints producer do the triggering. That is we trigger when someone sends
        // an exchange to the endpoint, see InterceptSendToEndpoint for details.
        RouteDefinition route = routeContext.getRoute();
        List<ProcessorDefinition<?>> outputs = route.getOutputs();
        outputs.remove(this);

        return new InterceptEndpointProcessor(uri, detour);
    }
    protected boolean matchPattern(CamelContext camelContext, String uri, String pattern) {
        // match using the pattern as-is
        boolean match = EndpointHelper.matchEndpoint(camelContext, uri, pattern);
        if (!match) {
            try {
                // the pattern could be an uri, so we need to normalize it before matching again
                pattern = URISupport.normalizeUri(pattern);
                match = EndpointHelper.matchEndpoint(camelContext, uri, pattern);
            } catch (Exception e) {
                // ignore
            }
        }
        return match;
    }
