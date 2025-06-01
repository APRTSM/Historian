    private MethodInfo chooseBestPossibleMethodInfo(Exchange exchange, Collection<MethodInfo> operationList, Object body,
                                                    List<MethodInfo> possibles, List<MethodInfo> possiblesWithException,
                                                    List<MethodInfo> possibleWithCustomAnnotation)
        throws AmbiguousMethodCallException {

        Exception exception = ExpressionBuilder.exchangeExceptionExpression().evaluate(exchange, Exception.class);
        if (exception != null && possiblesWithException.size() == 1) {
            LOG.trace("Exchange has exception set so we prefer method that also has exception as parameter");
            // prefer the method that accepts exception in case we have an exception also
            return possiblesWithException.get(0);
        } else if (possibles.size() == 1) {
            return possibles.get(0);
        } else if (possibles.isEmpty()) {
            LOG.trace("No possible methods so now trying to convert body to parameter types");

            // let's try converting
            Object newBody = null;
            MethodInfo matched = null;
            int matchCounter = 0;
            for (MethodInfo methodInfo : operationList) {
                if (methodInfo.getBodyParameterType().isInstance(body)) {
                    return methodInfo;
                }
                
                Object value = convertToType(exchange, methodInfo.getBodyParameterType(), body);
                if (value != null) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Converted body from: {} to: {}",
                                body.getClass().getCanonicalName(), methodInfo.getBodyParameterType().getCanonicalName());
                    }
                    matchCounter++;
                    newBody = value;
                    matched = methodInfo;
                }
            }
            if (matchCounter > 1) {
                throw new AmbiguousMethodCallException(exchange, Arrays.asList(matched, matched));
            }
            if (matched != null) {
                LOG.trace("Setting converted body: {}", body);
                Message in = exchange.getIn();
                in.setBody(newBody);
                return matched;
            }
        } else {
            // if we only have a single method with custom annotations, let's use that one
            if (possibleWithCustomAnnotation.size() == 1) {
                MethodInfo answer = possibleWithCustomAnnotation.get(0);
                LOG.trace("There are only one method with annotations so we choose it: {}", answer);
                return answer;
            }
            // phew try to choose among multiple methods with annotations
            return chooseMethodWithCustomAnnotations(exchange, possibles);
        }

        // cannot find a good method to use
        return null;
    }
