    ErrorHandlerBuilder cloneBuilder();
    public void addErrorHandlers(RouteContext routeContext, OnExceptionDefinition exception) {
        ErrorHandlerBuilder handler = handlers.get(routeContext);
        if (handler != null) {
            handler.addErrorHandlers(routeContext, exception);
        }
        super.addErrorHandlers(routeContext, exception);
    }
    public void setExceptionPolicyStrategy(ExceptionPolicyStrategy exceptionPolicyStrategy) {
        ObjectHelper.notNull(exceptionPolicyStrategy, "ExceptionPolicyStrategy");
        this.exceptionPolicyStrategy = exceptionPolicyStrategy;
    }
