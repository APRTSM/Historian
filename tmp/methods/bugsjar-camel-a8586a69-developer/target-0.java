    private MethodInfo introspect(Class<?> clazz, Method method) {
        LOG.trace("Introspecting class: {}, method: {}", clazz, method);
        String opName = method.getName();

        MethodInfo methodInfo = createMethodInfo(clazz, method);

        // methods already registered should be preferred to use instead of super classes of existing methods
        // we want to us the method from the sub class over super classes, so if we have already registered
        // the method then use it (we are traversing upwards: sub (child) -> super (farther) )
        MethodInfo existingMethodInfo = overridesExistingMethod(methodInfo);
        if (existingMethodInfo != null) {
            LOG.trace("This method is already overridden in a subclass, so the method from the sub class is preferred: {}", existingMethodInfo);
            return existingMethodInfo;
        }

        LOG.trace("Adding operation: {} for method: {}", opName, methodInfo);

        if (hasMethod(opName)) {
            // we have an overloaded method so add the method info to the same key
            List<MethodInfo> existing = getOperations(opName);
            existing.add(methodInfo);
        } else {
            // its a new method we have not seen before so wrap it in a list and add it
            List<MethodInfo> methods = new ArrayList<MethodInfo>();
            methods.add(methodInfo);
            operations.put(opName, methods);
        }

        if (methodInfo.hasCustomAnnotation()) {
            operationsWithCustomAnnotation.add(methodInfo);
        } else if (methodInfo.hasBodyParameter()) {
            operationsWithBody.add(methodInfo);
        } else {
            operationsWithNoBody.add(methodInfo);
        }

        if (methodInfo.hasHandlerAnnotation()) {
            operationsWithHandlerAnnotation.add(methodInfo);
        }

        // must add to method map last otherwise we break stuff
        methodMap.put(method, methodInfo);

        return methodInfo;
    }
    protected MethodInfo chooseMethod(Object pojo, Exchange exchange, String name) throws AmbiguousMethodCallException {
        // @Handler should be select first
        // then any single method that has a custom @annotation
        // or any single method that has a match parameter type that matches the Exchange payload
        // and last then try to select the best among the rest

        // must use defensive copy, to avoid altering the shared lists
        // and we want to remove unwanted operations from these local lists
        final List<MethodInfo> localOperationsWithBody = new ArrayList<MethodInfo>(operationsWithBody);
        final List<MethodInfo> localOperationsWithNoBody = new ArrayList<MethodInfo>(operationsWithNoBody);
        final List<MethodInfo> localOperationsWithCustomAnnotation = new ArrayList<MethodInfo>(operationsWithCustomAnnotation);
        final List<MethodInfo> localOperationsWithHandlerAnnotation = new ArrayList<MethodInfo>(operationsWithHandlerAnnotation);

        if (name != null) {
            // filter all lists to only include methods with this name
            removeNonMatchingMethods(localOperationsWithHandlerAnnotation, name);
            removeNonMatchingMethods(localOperationsWithCustomAnnotation, name);
            removeNonMatchingMethods(localOperationsWithBody, name);
            removeNonMatchingMethods(localOperationsWithNoBody, name);
        } else {
            // remove all getter/setter as we do not want to consider these methods
            removeAllSetterOrGetterMethods(localOperationsWithHandlerAnnotation);
            removeAllSetterOrGetterMethods(localOperationsWithCustomAnnotation);
            removeAllSetterOrGetterMethods(localOperationsWithBody);
            removeAllSetterOrGetterMethods(localOperationsWithNoBody);
        }

        if (localOperationsWithHandlerAnnotation.size() > 1) {
            // if we have more than 1 @Handler then its ambiguous
            throw new AmbiguousMethodCallException(exchange, localOperationsWithHandlerAnnotation);
        }

        if (localOperationsWithHandlerAnnotation.size() == 1) {
            // methods with handler should be preferred
            return localOperationsWithHandlerAnnotation.get(0);
        } else if (localOperationsWithCustomAnnotation.size() == 1) {
            // if there is one method with an annotation then use that one
            return localOperationsWithCustomAnnotation.get(0);
        }

        // named method and with no parameters
        boolean noParameters = name != null && name.endsWith("()");
        if (noParameters && localOperationsWithNoBody.size() == 1) {
            // if there was a method name configured and it has no parameters, then use the method with no body (eg no parameters)
            return localOperationsWithNoBody.get(0);
        } else if (localOperationsWithBody.size() == 1) {
            // if there is one method with body then use that one
            return localOperationsWithBody.get(0);
        }

        Collection<MethodInfo> possibleOperations = new ArrayList<MethodInfo>();
        possibleOperations.addAll(localOperationsWithBody);
        possibleOperations.addAll(localOperationsWithCustomAnnotation);

        if (!possibleOperations.isEmpty()) {
             // multiple possible operations so find the best suited if possible
            MethodInfo answer = chooseMethodWithMatchingBody(exchange, possibleOperations, localOperationsWithCustomAnnotation);
            if (answer == null) {
                throw new AmbiguousMethodCallException(exchange, possibleOperations);
            } else {
                return answer;
            }
        }

        // not possible to determine
        return null;
    }
    public BeanInfo(CamelContext camelContext, Class<?> type, Method explicitMethod, ParameterMappingStrategy strategy) {
        this.camelContext = camelContext;
        this.type = type;
        this.strategy = strategy;

        if (explicitMethod != null) {
            // must be a valid method
            if (!isValidMethod(type, explicitMethod)) {
                throw new IllegalArgumentException("The method " + explicitMethod + " is not valid (for example the method must be public)");
            }
            introspect(getType(), explicitMethod);
        } else {
            introspect(getType());
        }

        // if there are only 1 method with 1 operation then select it as a default/fallback method
        MethodInfo method = null;
        if (operations.size() == 1) {
            List<MethodInfo> methods = operations.values().iterator().next();
            if (methods.size() == 1) {
                method = methods.get(0);
            }
        }
        defaultMethod = method;

        // mark the operations lists as unmodifiable, as they should not change during runtime
        // to keep this code thread safe
        operations = Collections.unmodifiableMap(operations);
        operationsWithBody = Collections.unmodifiableList(operationsWithBody);
        operationsWithNoBody = Collections.unmodifiableList(operationsWithNoBody);
        operationsWithCustomAnnotation = Collections.unmodifiableList(operationsWithCustomAnnotation);
        operationsWithHandlerAnnotation = Collections.unmodifiableList(operationsWithHandlerAnnotation);
        methodMap = Collections.unmodifiableMap(methodMap);
    }
