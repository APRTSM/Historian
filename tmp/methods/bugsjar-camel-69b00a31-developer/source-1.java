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
        public void process(Exchange exchange) throws Exception {
            // copy the original exchange to avoid side effects on it
            Exchange resultExchange = exchange.copy();
            // remove any existing exception in case we do OGNL on the exception
            resultExchange.setException(null);
            // force to use InOut to retrieve the result on the OUT message
            resultExchange.setPattern(ExchangePattern.InOut);
            // do not propagate any method name when using OGNL, as with OGNL we
            // compute and provide the method name to explicit to invoke
            resultExchange.getIn().removeHeader(Exchange.BEAN_METHOD_NAME);

            // current ognl path as we go along
            String ognlPath = "";

            // loop and invoke each method
            Object beanToCall = beanHolder.getBean();
            // there must be a bean to call with, we currently does not support OGNL expressions on using purely static methods
            if (beanToCall == null) {
                throw new IllegalArgumentException("Bean instance is null. OGNL bean expressions requires bean instances.");
            }

            // Split ognl except when this is not a Map, Array
            // and we would like to keep the dots within the key name
            List<String> methods = OgnlHelper.splitOgnl(ognl);

            for (String methodName : methods) {
                BeanHolder holder = new ConstantBeanHolder(beanToCall, exchange.getContext());

                // support the null safe operator
                boolean nullSafe = OgnlHelper.isNullSafeOperator(methodName);

                // keep up with how far are we doing
                ognlPath += methodName;

                // get rid of leading ?. or . as we only needed that to determine if null safe was enabled or not
                methodName = OgnlHelper.removeLeadingOperators(methodName);

                // are we doing an index lookup (eg in Map/List/array etc)?
                String key = null;
                KeyValueHolder<String, String> index = OgnlHelper.isOgnlIndex(methodName);
                if (index != null) {
                    methodName = index.getKey();
                    key = index.getValue();
                }

                // only invoke if we have a method name to use to invoke
                if (methodName != null) {
                    InvokeProcessor invoke = new InvokeProcessor(holder, methodName);
                    invoke.process(resultExchange);

                    // check for exception and rethrow if we failed
                    if (resultExchange.getException() != null) {
                        throw new RuntimeBeanExpressionException(exchange, beanName, methodName, resultExchange.getException());
                    }

                    result = invoke.getResult();
                }

                // if there was a key then we need to lookup using the key
                if (key != null) {
                    result = lookupResult(resultExchange, key, result, nullSafe, ognlPath, holder.getBean());
                }

                // check null safe for null results
                if (result == null && nullSafe) {
                    return;
                }

                // prepare for next bean to invoke
                beanToCall = result;
                // we need to set the result to the exchange for further processing
                resultExchange.getIn().setBody(result);
            }
        }
