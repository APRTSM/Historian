    protected AggregationStrategyMethodInfo createMethodInfo() {
        Class<?>[] parameterTypes = method.getParameterTypes();

        int size = parameterTypes.length;
        if (LOG.isTraceEnabled()) {
            LOG.trace("Creating MethodInfo for class: {} method: {} having {} parameters", new Object[]{type, method, size});
        }

        // must have equal number of parameters
        if (size < 2) {
            throw new IllegalArgumentException("The method " + method.getName() + " must have at least two parameters, has: " + size);
        } else if (size % 2 != 0) {
            throw new IllegalArgumentException("The method " + method.getName() + " must have equal number of parameters, has: " + size);
        }

        // must not have annotations as they are not supported (yet)
        for (int i = 0; i < size; i++) {
            Class<?> type = parameterTypes[i];
            if (type.getAnnotations().length > 0) {
                throw new IllegalArgumentException("Parameter annotations at index " + i + " is not supported on method: " + method);
            }
        }

        List<ParameterInfo> oldParameters = new ArrayList<ParameterInfo>();
        List<ParameterInfo> newParameters = new ArrayList<ParameterInfo>();

        for (int i = 0; i < size / 2; i++) {
            Class<?> oldType = parameterTypes[i];
            if (oldParameters.size() == 0) {
                // the first parameter is the body
                Expression oldBody = ExpressionBuilder.mandatoryBodyExpression(oldType);
                ParameterInfo info = new ParameterInfo(i, oldType, null, oldBody);
                oldParameters.add(info);
            } else if (oldParameters.size() == 1) {
                // the 2nd parameter is the headers
                Expression oldHeaders = ExpressionBuilder.headersExpression();
                ParameterInfo info = new ParameterInfo(i, oldType, null, oldHeaders);
                oldParameters.add(info);
            } else if (oldParameters.size() == 2) {
                // the 3rd parameter is the properties
                Expression oldProperties = ExpressionBuilder.propertiesExpression();
                ParameterInfo info = new ParameterInfo(i, oldType, null, oldProperties);
                oldParameters.add(info);
            }
        }

        for (int i = size / 2; i < size; i++) {
            Class<?> newType = parameterTypes[i];
            if (newParameters.size() == 0) {
                // the first parameter is the body
                Expression newBody = ExpressionBuilder.mandatoryBodyExpression(newType);
                ParameterInfo info = new ParameterInfo(i, newType, null, newBody);
                newParameters.add(info);
            } else if (newParameters.size() == 1) {
                // the 2nd parameter is the headers
                Expression newHeaders = ExpressionBuilder.headersExpression();
                ParameterInfo info = new ParameterInfo(i, newType, null, newHeaders);
                newParameters.add(info);
            } else if (newParameters.size() == 2) {
                // the 3rd parameter is the properties
                Expression newProperties = ExpressionBuilder.propertiesExpression();
                ParameterInfo info = new ParameterInfo(i, newType, null, newProperties);
                newParameters.add(info);
            }
        }

        return new AggregationStrategyMethodInfo(camelContext, type, method, oldParameters, newParameters);
    }
