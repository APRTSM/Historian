        public void process(Exchange exchange) throws Exception {
            // copy the original exchange to avoid side effects on it
            Exchange resultExchange = exchange.copy();
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
            }
        }
        public void process(Exchange exchange) throws Exception {
            BeanProcessor processor = new BeanProcessor(beanHolder);
            if (methodName != null) {
                processor.setMethod(methodName);
                // enable OGNL like invocation
                processor.setShorthandMethod(true);
            }
            try {
                // copy the original exchange to avoid side effects on it
                Exchange resultExchange = exchange.copy();
                // force to use InOut to retrieve the result on the OUT message
                resultExchange.setPattern(ExchangePattern.InOut);
                processor.process(resultExchange);
                result = resultExchange.getOut().getBody();

                // propagate exceptions
                if (resultExchange.getException() != null) {
                    exchange.setException(resultExchange.getException());
                }
            } catch (Exception e) {
                throw new RuntimeBeanExpressionException(exchange, beanName, methodName, e);
            }
        }
