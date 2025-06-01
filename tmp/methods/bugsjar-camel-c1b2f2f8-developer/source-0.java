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
