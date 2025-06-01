    public void addExceptionPolicy(OnExceptionDefinition exceptionType) {
        Processor processor = exceptionType.getErrorHandler();
        addChildService(processor);

        List<Class> list = exceptionType.getExceptionClasses();

        for (Class clazz : list) {
            ExceptionPolicyKey key = new ExceptionPolicyKey(clazz, exceptionType.getOnWhen());
            exceptionPolicies.put(key, exceptionType);
        }
    }
