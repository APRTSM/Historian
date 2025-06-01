    public void mockEndpoints() throws Exception {
        getContext().removeEndpoints("*");
        getContext().addRegisterEndpointCallback(new InterceptSendToMockEndpointStrategy(null));
    }
    public AdviceWithBuilder weaveByToString(String pattern) {
        ObjectHelper.notNull(originalRoute, "originalRoute", this);

        return new AdviceWithBuilder(this, null, pattern);
    }
    public AdviceWithBuilder weaveById(String pattern) {
        ObjectHelper.notNull(originalRoute, "originalRoute", this);

        return new AdviceWithBuilder(this, pattern, null);
    }
    public void mockEndpoints(String pattern) throws Exception {
        getContext().removeEndpoints(pattern);
        getContext().addRegisterEndpointCallback(new InterceptSendToMockEndpointStrategy(pattern));
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
