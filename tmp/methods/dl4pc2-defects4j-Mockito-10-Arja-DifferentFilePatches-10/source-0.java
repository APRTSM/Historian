        private Class superClassOf(Class currentExploredClass) {
            Type genericSuperclass = currentExploredClass.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) genericSuperclass).getRawType();
                return (Class) rawType;
            }
            return (Class) genericSuperclass;
        }
    public GenericMetadataSupport resolveGenericReturnType(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        // logger.log("Method '" + method.toGenericString() + "' has return type : " + genericReturnType.getClass().getInterfaces()[0].getSimpleName() + " : " + genericReturnType);

        if (genericReturnType instanceof Class) {
            return new NotGenericReturnTypeSupport(genericReturnType);
        }
        if (genericReturnType instanceof ParameterizedType) {
            return new ParameterizedReturnType(this, method.getTypeParameters(), (ParameterizedType) method.getGenericReturnType());
        }
        if (genericReturnType instanceof TypeVariable) {
            return new TypeVariableReturnType(this, method.getTypeParameters(), (TypeVariable) genericReturnType);
        }

        throw new MockitoException("Ouch, it shouldn't happen, type '" + genericReturnType.getClass().getCanonicalName() + "' on method : '" + method.toGenericString() + "' is not supported : " + genericReturnType);
    }
