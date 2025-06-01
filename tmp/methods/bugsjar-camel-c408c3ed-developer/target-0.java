    public Expression createExpression(CamelContext camelContext) {
        Expression answer;

        if (beanType == null && beanTypeName != null) {
            try {
                beanType = camelContext.getClassResolver().resolveMandatoryClass(beanTypeName);
            } catch (ClassNotFoundException e) {
                throw ObjectHelper.wrapRuntimeCamelException(e);
            }
        }

        if (beanType != null) {
            // create a bean if there is a default public no-arg constructor
            if (ObjectHelper.hasDefaultPublicNoArgConstructor(beanType)) {
                instance = camelContext.getInjector().newInstance(beanType);
                answer = new BeanExpression(instance, getMethod());
            } else {
                answer = new BeanExpression(beanType, getMethod());
            }
        } else if (instance != null) {
            answer = new BeanExpression(instance, getMethod());
        } else {
            String ref = beanName();
            // if its a ref then check that the ref exists
            BeanHolder holder = new RegistryBean(camelContext, ref);
            // get the bean which will check that it exists
            instance = holder.getBean();
            answer = new BeanExpression(instance, getMethod());
        }

        validateHasMethod(camelContext, instance, beanType, getMethod());
        return answer;
    }
