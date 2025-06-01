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
        } else if (!noParameters && localOperationsWithBody.size() == 1 && localOperationsWithCustomAnnotation.isEmpty()) {
            // if there is one method with body then use that one
            return localOperationsWithBody.get(0);
        }

        Collection<MethodInfo> possibleOperations = new ArrayList<MethodInfo>();
        possibleOperations.addAll(localOperationsWithBody);
        possibleOperations.addAll(localOperationsWithCustomAnnotation);

        if (!possibleOperations.isEmpty()) {
            // multiple possible operations so find the best suited if possible
            MethodInfo answer = chooseMethodWithMatchingBody(exchange, possibleOperations, localOperationsWithCustomAnnotation);

            if (answer == null && name != null) {
                // do we have hardcoded parameters values provided from the method name then fallback and try that
                String parameters = ObjectHelper.between(name, "(", ")");
                if (parameters != null) {
                    // special as we have hardcoded parameters, so we need to choose method that matches those parameters the best
                    answer = chooseMethodWithMatchingParameters(exchange, parameters, possibleOperations);
                }
            }
            
            if (answer == null && possibleOperations.size() > 1) {
                answer = getSingleCovariantMethod(possibleOperations);
            }
            
            if (answer == null) {
                throw new AmbiguousMethodCallException(exchange, possibleOperations);
            } else {
                return answer;
            }
        }

        // not possible to determine
        return null;
    }
    protected boolean isValidMethod(Class<?> clazz, Method method) {
        // must not be in the excluded list
        for (Method excluded : EXCLUDED_METHODS) {
            if (ObjectHelper.isOverridingMethod(excluded, method)) {
                // the method is overriding an excluded method so its not valid
                return false;
            }
        }

        // must be a public method
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }

        // must not be abstract
        if (Modifier.isAbstract(method.getModifiers())) {
            return false;
        }

        // return type must not be an Exchange and it should not be a bridge method
        if ((method.getReturnType() != null && Exchange.class.isAssignableFrom(method.getReturnType())) || method.isBridge()) {
            return false;
        }

        return true;
    }
