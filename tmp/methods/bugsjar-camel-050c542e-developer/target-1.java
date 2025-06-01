    public MethodNotFoundException(Exchange exchange, Object pojo, String methodName) {
        super("Method with name: " + methodName + " not found on bean: " + pojo, exchange);
        this.methodName = methodName;
        this.bean = pojo;
    }
    public MethodNotFoundException(Object pojo, String methodName, Throwable cause) {
        super("Method with name: " + methodName + " not found on bean: " + pojo, null, cause);
        this.methodName = methodName;
        this.bean = pojo;
    }
    public Expression createExpression(CamelContext camelContext) {
        Expression answer;
        if (beanType != null) {
            instance = ObjectHelper.newInstance(beanType);
            answer = new BeanExpression(instance, getMethod(), parameterType);
        } else if (instance != null) {
            answer = new BeanExpression(instance, getMethod(), parameterType);
        } else {
            String ref = beanName();
            // if its a ref then check that the ref exists
            BeanHolder holder = new RegistryBean(camelContext, ref);
            // get the bean which will check that it exists
            instance = holder.getBean();
            answer = new BeanExpression(ref, getMethod(), parameterType);
        }

        validateHasMethod(camelContext, instance, getMethod(), parameterType);
        return answer;
    }
    protected void validateHasMethod(CamelContext context, Object bean, String method, Class parameterType) {
        if (method == null) {
            return;
        }

        // do not try to validate ognl methods
        if (OgnlHelper.isValidOgnlExpression(method)) {
            return;
        }

        // if invalid OGNL then fail
        if (OgnlHelper.isInvalidValidOgnlExpression(method)) {
            ExpressionIllegalSyntaxException cause = new ExpressionIllegalSyntaxException(method);
            throw ObjectHelper.wrapRuntimeCamelException(new MethodNotFoundException(bean, method, cause));
        }

        BeanInfo info = new BeanInfo(context, bean.getClass());
        List<Class> parameterTypes = new ArrayList<Class>();
        if (parameterType != null) {
            parameterTypes.add(parameterType);
        }

        if (!info.hasMethod(method, parameterTypes)) {
            throw ObjectHelper.wrapRuntimeCamelException(new MethodNotFoundException(null, bean, method, parameterTypes));
        }
    }
