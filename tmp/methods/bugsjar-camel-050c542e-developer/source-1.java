    public MethodNotFoundException(Exchange exchange, Object pojo, String methodName) {
        this(exchange, pojo, methodName, null);
    }
    protected void validateHasMethod(CamelContext context, Object bean, String method, Class parameterType) {
        if (method == null) {
            return;
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
    public Expression createExpression(CamelContext camelContext) {
        if (beanType != null) {
            instance = ObjectHelper.newInstance(beanType);
            return new BeanExpression(instance, getMethod(), parameterType);
        } else if (instance != null) {
            return new BeanExpression(instance, getMethod(), parameterType);
        } else {
            String ref = beanName();
            // if its a ref then check that the ref exists
            BeanHolder holder = new RegistryBean(camelContext, ref);
            // get the bean which will check that it exists
            instance = holder.getBean();
            // only validate when it was a ref for a bean, so we can eager check
            // this on startup of Camel
            validateHasMethod(camelContext, instance, getMethod(), parameterType);
            return new BeanExpression(ref, getMethod(), parameterType);
        }
    }
