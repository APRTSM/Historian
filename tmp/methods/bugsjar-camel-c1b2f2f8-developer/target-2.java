    public void mockEndpoints(String pattern) throws Exception {
        getContext().addRegisterEndpointCallback(new InterceptSendToMockEndpointStrategy(pattern));
    }
    public AdviceWithBuilder weaveByToString(String pattern) {
        ObjectHelper.notNull(originalRoute, "originalRoute", this);
        return new AdviceWithBuilder(this, null, pattern);
    }
    public AdviceWithBuilder weaveById(String pattern) {
        ObjectHelper.notNull(originalRoute, "originalRoute", this);
        return new AdviceWithBuilder(this, pattern, null);
    }
    public void mockEndpoints() throws Exception {
        getContext().addRegisterEndpointCallback(new InterceptSendToMockEndpointStrategy(null));
    }
    public Producer createProducer() throws Exception {
        producer = delegate.createProducer();
        return new Producer() {

            public Endpoint getEndpoint() {
                return producer.getEndpoint();
            }

            public Exchange createExchange() {
                return producer.createExchange();
            }

            public Exchange createExchange(ExchangePattern pattern) {
                return producer.createExchange(pattern);
            }

            public Exchange createExchange(Exchange exchange) {
                return producer.createExchange(exchange);
            }

            public void process(Exchange exchange) throws Exception {
                // process the detour so we do the detour routing
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Sending to endpoint: " + getEndpointUri() + " is intercepted and detoured to: " + detour + " for exchange: " + exchange);
                }
                LOG.info("Sending to endpoint: " + getEndpointUri() + " is intercepted and detoured to: " + detour + " for exchange: " + exchange);
                // add header with the real endpoint uri
                exchange.getIn().setHeader(Exchange.INTERCEPTED_ENDPOINT, delegate.getEndpointUri());

                try {
                    detour.process(exchange);
                } catch (Exception e) {
                    exchange.setException(e);
                }

                // Decide whether to continue or not; similar logic to the Pipeline
                // check for error if so we should break out
                if (!continueProcessing(exchange, "skip sending to original intended destination: " + getEndpointUri(), LOG)) {
                    return;
                }

                if (!skip) {
                    if (exchange.hasOut()) {
                        // replace OUT with IN as detour changed something
                        exchange.setIn(exchange.getOut());
                        exchange.setOut(null);
                    }

                    // route to original destination
                    producer.process(exchange);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Stop() means skip sending exchange to original intended destination: " + getEndpointUri() + " for exchange: " + exchange);
                    }
                }
            }

            public boolean isSingleton() {
                return producer.isSingleton();
            }

            public void start() throws Exception {
                ServiceHelper.startService(detour);
            }

            public void stop() throws Exception {
                ServiceHelper.stopService(detour);
            }
        };
    }
    public Endpoint registerEndpoint(String uri, Endpoint endpoint) {
        if (endpoint instanceof InterceptSendToEndpoint) {
            // endpoint already decorated
            return endpoint;
        } else if (endpoint instanceof MockEndpoint) {
            // we should not intercept mock endpoints
            return endpoint;
        } else if (uri == null || pattern == null || EndpointHelper.matchEndpoint(uri, pattern)) {
            // if pattern is null then it mean to match all

            // only proxy if the uri is matched decorate endpoint with our proxy
            // should be false by default
            InterceptSendToEndpoint proxy = new InterceptSendToEndpoint(endpoint, false);

            // create mock endpoint which we will use as interceptor
            // replace :// from scheme to make it easy to lookup the mock endpoint without having double :// in uri
            String key = "mock:" + endpoint.getEndpointKey().replaceFirst("://", ":");
            // strip off parameters as well
            if (key.contains("?")) {
                key = ObjectHelper.before(key, "?");
            }
            LOG.info("Adviced endpoint [" + uri + "] with mock endpoint [" + key + "]");

            MockEndpoint mock = endpoint.getCamelContext().getEndpoint(key, MockEndpoint.class);
            Processor producer;
            try {
                producer = mock.createProducer();
            } catch (Exception e) {
                throw wrapRuntimeCamelException(e);
            }

            proxy.setDetour(producer);
            return proxy;
        } else {
            // no proxy so return regular endpoint
            return endpoint;
        }
    }
