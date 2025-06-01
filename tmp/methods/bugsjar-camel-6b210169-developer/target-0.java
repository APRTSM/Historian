    protected Expression createParametersExpression() {
        final int size = parameters.size();
        LOG.trace("Creating parameters expression for {} parameters", size);

        final Expression[] expressions = new Expression[size];
        for (int i = 0; i < size; i++) {
            Expression parameterExpression = parameters.get(i).getExpression();
            expressions[i] = parameterExpression;
            LOG.trace("Parameter #{} has expression: {}", i, parameterExpression);
        }
        return new Expression() {
            @SuppressWarnings("unchecked")
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                Object[] answer = new Object[size];
                Object body = exchange.getIn().getBody();
                boolean multiParameterArray = false;
                if (exchange.getIn().getHeader(Exchange.BEAN_MULTI_PARAMETER_ARRAY) != null) {
                    multiParameterArray = exchange.getIn().getHeader(Exchange.BEAN_MULTI_PARAMETER_ARRAY, Boolean.class);
                }

                // if there was an explicit method name to invoke, then we should support using
                // any provided parameter values in the method name
                String methodName = exchange.getIn().getHeader(Exchange.BEAN_METHOD_NAME, "", String.class);
                // the parameter values is between the parenthesis
                String methodParameters = ObjectHelper.between(methodName, "(", ")");
                // use an iterator to walk the parameter values
                Iterator<?> it = null;
                if (methodParameters != null) {
                    // split the parameters safely separated by comma, but beware that we can have
                    // quoted parameters which contains comma as well, so do a safe quote split
                    String[] parameters = StringQuoteHelper.splitSafeQuote(methodParameters, ',', true);
                    it = ObjectHelper.createIterator(parameters, ",", true);
                }

                // remove headers as they should not be propagated
                // we need to do this before the expressions gets evaluated as it may contain
                // a @Bean expression which would by mistake read these headers. So the headers
                // must be removed at this point of time
                exchange.getIn().removeHeader(Exchange.BEAN_MULTI_PARAMETER_ARRAY);
                exchange.getIn().removeHeader(Exchange.BEAN_METHOD_NAME);

                for (int i = 0; i < size; i++) {
                    // grab the parameter value for the given index
                    Object parameterValue = it != null && it.hasNext() ? it.next() : null;
                    // and the expected parameter type
                    Class<?> parameterType = parameters.get(i).getType();
                    // the value for the parameter to use
                    Object value = null;

                    if (multiParameterArray) {
                        // get the value from the array
                        value = ((Object[])body)[i];
                    } else {
                        // prefer to use parameter value if given, as they override any bean parameter binding
                        // we should skip * as its a type placeholder to indicate any type
                        if (parameterValue != null && !parameterValue.equals("*")) {
                            // evaluate the parameter value binding
                            value = evaluateParameterValue(exchange, i, parameterValue, parameterType);
                        }

                        // use bean parameter binding, if still no value
                        Expression expression = expressions[i];
                        if (value == null && expression != null) {
                            value = evaluateParameterBinding(exchange, expression, i, parameterType);
                        }
                    }

                    // remember the value to use
                    if (value != Void.TYPE) {
                        answer[i] = value;
                    }
                }

                return (T) answer;
            }

            /**
             * Evaluate using parameter values where the values can be provided in the method name syntax.
             * <p/>
             * This methods returns accordingly:
             * <ul>
             *     <li><tt>null</tt> - if not a parameter value</li>
             *     <li><tt>Void.TYPE</tt> - if an explicit null, forcing Camel to pass in <tt>null</tt> for that given parameter</li>
             *     <li>a non <tt>null</tt> value - if the parameter was a parameter value, and to be used</li>
             * </ul>
             *
             * @since 2.9
             */
            private Object evaluateParameterValue(Exchange exchange, int index, Object parameterValue, Class<?> parameterType) {
                Object answer = null;

                // convert the parameter value to a String
                String exp = exchange.getContext().getTypeConverter().convertTo(String.class, exchange, parameterValue);
                if (exp != null) {
                    // check if its a valid parameter value
                    boolean valid = BeanHelper.isValidParameterValue(exp);

                    if (!valid) {
                        // it may be a parameter type instead, and if so, then we should return null,
                        // as this method is only for evaluating parameter values
                        Boolean isClass = BeanHelper.isAssignableToExpectedType(exchange.getContext().getClassResolver(), exp, parameterType);
                        // the method will return a non null value if exp is a class
                        if (isClass != null) {
                            return null;
                        }
                    }

                    // use simple language to evaluate the expression, as it may use the simple language to refer to message body, headers etc.
                    Expression expression = null;
                    try {
                        expression = exchange.getContext().resolveLanguage("simple").createExpression(exp);
                        parameterValue = expression.evaluate(exchange, Object.class);
                        // use "null" to indicate the expression returned a null value which is a valid response we need to honor
                        if (parameterValue == null) {
                            parameterValue = "null";
                        }
                    } catch (Exception e) {
                        throw new ExpressionEvaluationException(expression, "Cannot create/evaluate simple expression: " + exp
                                + " to be bound to parameter at index: " + index + " on method: " + getMethod(), exchange, e);
                    }

                    // special for explicit null parameter values (as end users can explicit indicate they want null as parameter)
                    // see method javadoc for details
                    if ("null".equals(parameterValue)) {
                        return Void.TYPE;
                    }

                    // the parameter value may match the expected type, then we use it as-is
                    if (parameterType.isAssignableFrom(parameterValue.getClass())) {
                        valid = true;
                    } else {
                        // the parameter value was not already valid, but since the simple language have evaluated the expression
                        // which may change the parameterValue, so we have to check it again to see if its now valid
                        exp = exchange.getContext().getTypeConverter().tryConvertTo(String.class, parameterValue);
                        // String values from the simple language is always valid
                        if (!valid) {
                            // re validate if the parameter was not valid the first time (String values should be accepted)
                            valid = parameterValue instanceof String || BeanHelper.isValidParameterValue(exp);
                        }
                    }

                    if (valid) {
                        // we need to unquote String parameters, as the enclosing quotes is there to denote a parameter value
                        if (parameterValue instanceof String) {
                            parameterValue = StringHelper.removeLeadingAndEndingQuotes((String) parameterValue);
                        }
                        if (parameterValue != null) {
                            try {
                                // its a valid parameter value, so convert it to the expected type of the parameter
                                answer = exchange.getContext().getTypeConverter().mandatoryConvertTo(parameterType, exchange, parameterValue);
                                if (LOG.isTraceEnabled()) {
                                    LOG.trace("Parameter #{} evaluated as: {} type: ", new Object[]{index, answer, ObjectHelper.type(answer)});
                                }
                            } catch (Exception e) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Cannot convert from type: {} to type: {} for parameter #{}", new Object[]{ObjectHelper.type(parameterValue), parameterType, index});
                                }
                                throw new ParameterBindingException(e, method, index, parameterType, parameterValue);
                            }
                        }
                    }
                }

                return answer;
            }

            /**
             * Evaluate using classic parameter binding using the pre compute expression
             */
            private Object evaluateParameterBinding(Exchange exchange, Expression expression, int index, Class<?> parameterType) {
                Object answer = null;

                // use object first to avoid type conversion so we know if there is a value or not
                Object result = expression.evaluate(exchange, Object.class);
                if (result != null) {
                    // we got a value now try to convert it to the expected type
                    try {
                        answer = exchange.getContext().getTypeConverter().mandatoryConvertTo(parameterType, result);
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Parameter #{} evaluated as: {} type: ", new Object[]{index, answer, ObjectHelper.type(answer)});
                        }
                    } catch (NoTypeConversionAvailableException e) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Cannot convert from type: {} to type: {} for parameter #{}", new Object[]{ObjectHelper.type(result), parameterType, index});
                        }
                        throw new ParameterBindingException(e, method, index, parameterType, result);
                    }
                } else {
                    LOG.trace("Parameter #{} evaluated as null", index);
                }

                return answer;
            }

            @Override
            public String toString() {
                return "ParametersExpression: " + Arrays.asList(expressions);
            }

        };
    }
