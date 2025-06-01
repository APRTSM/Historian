    public void addExceptionPolicy(OnExceptionDefinition exceptionType) {
        Processor processor = exceptionType.getErrorHandler();
        addChildService(processor);

        List<Class> list = exceptionType.getExceptionClasses();

        for (Class clazz : list) {
            RouteDefinition route = ProcessorDefinitionHelper.getRoute(exceptionType);
            String routeId = route != null ? route.getId() : null;
            ExceptionPolicyKey key = new ExceptionPolicyKey(routeId, clazz, exceptionType.getOnWhen());
            exceptionPolicies.put(key, exceptionType);
        }
    }
