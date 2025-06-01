    private MethodInvocation createInvocation(Object pojo, Exchange exchange, Method explicitMethod)
        throws AmbiguousMethodCallException, MethodNotFoundException {
        MethodInfo methodInfo = null;
        
        // find the explicit method to invoke
        if (explicitMethod != null) {
            Iterator<List<MethodInfo>> it = operations.values().iterator();
            while (it.hasNext()) {
                List<MethodInfo> infos = it.next();
                for (MethodInfo info : infos) {
                    if (explicitMethod.equals(info.getMethod())) {
                        return info.createMethodInvocation(pojo, exchange);
                    }
                }
            }
            throw new MethodNotFoundException(exchange, pojo, explicitMethod.getName());
        }

        String methodName = exchange.getIn().getHeader(Exchange.BEAN_METHOD_NAME, String.class);
        if (methodName != null) {

            // do not use qualifier for name
            String name = methodName;
            if (methodName.contains("(")) {
                name = ObjectHelper.before(methodName, "(");
            }
            boolean emptyParameters = methodName.endsWith("()");

            // special for getClass, as we want the user to be able to invoke this method
            // for example to log the class type or the likes
            if ("class".equals(name) || "getClass".equals(name)) {
                try {
                    Method method = pojo.getClass().getMethod("getClass");
                    methodInfo = new MethodInfo(exchange.getContext(), pojo.getClass(), method, Collections.<ParameterInfo>emptyList(), Collections.<ParameterInfo>emptyList(), false, false);
                } catch (NoSuchMethodException e) {
                    throw new MethodNotFoundException(exchange, pojo, "getClass");
                }
            // special for length on an array type
            } else if ("length".equals(name) && pojo.getClass().isArray()) {
                try {
                    // need to use arrayLength method from ObjectHelper as Camel's bean OGNL support is method invocation based
                    // and not for accessing fields. And hence we need to create a MethodInfo instance with a method to call
                    // and therefore use arrayLength from ObjectHelper to return the array length field.
                    Method method = ObjectHelper.class.getMethod("arrayLength", Object[].class);
                    ParameterInfo pi = new ParameterInfo(0, Object[].class, null, ExpressionBuilder.mandatoryBodyExpression(Object[].class, true));
                    List<ParameterInfo> lpi = new ArrayList<ParameterInfo>(1);
                    lpi.add(pi);
                    methodInfo = new MethodInfo(exchange.getContext(), pojo.getClass(), method, lpi, lpi, false, false);
                } catch (NoSuchMethodException e) {
                    throw new MethodNotFoundException(exchange, pojo, "getClass");
                }
            } else {
                List<MethodInfo> methods = getOperations(name);
                if (methods != null && methods.size() == 1) {
                    // only one method then choose it
                    methodInfo = methods.get(0);

                    // validate that if we want an explict no-arg method, then that's what we get
                    if (emptyParameters && methodInfo.hasParameters()) {
                        throw new MethodNotFoundException(exchange, pojo, methodName, "(with no parameters)");
                    }
                } else if (methods != null) {
                    // there are more methods with that name so we cannot decide which to use

                    // but first let's try to choose a method and see if that complies with the name
                    // must use the method name which may have qualifiers
                    methodInfo = chooseMethod(pojo, exchange, methodName);

                    // validate that if we want an explicit no-arg method, then that's what we get
                    if (emptyParameters) {
                        if (methodInfo == null || methodInfo.hasParameters()) {
                            // we could not find a no-arg method with that name
                            throw new MethodNotFoundException(exchange, pojo, methodName, "(with no parameters)");
                        }
                    }

                    if (methodInfo == null || !name.equals(methodInfo.getMethod().getName())) {
                        throw new AmbiguousMethodCallException(exchange, methods);
                    }
                } else {
                    // a specific method was given to invoke but not found
                    throw new MethodNotFoundException(exchange, pojo, methodName);
                }
            }
        }

        if (methodInfo == null) {
            // no name or type
            methodInfo = chooseMethod(pojo, exchange, null);
        }
        if (methodInfo == null) {
            methodInfo = defaultMethod;
        }
        if (methodInfo != null) {
            LOG.trace("Chosen method to invoke: {} on bean: {}", methodInfo, pojo);
            return methodInfo.createMethodInvocation(pojo, exchange);
        }

        LOG.debug("Cannot find suitable method to invoke on bean: {}", pojo);
        return null;
    }
