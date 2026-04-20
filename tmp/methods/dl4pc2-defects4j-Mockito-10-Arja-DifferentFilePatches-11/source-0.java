    public static GenericMetadataSupport inferFrom(Type type) {
        Checks.checkNotNull(type, "type");
        if (type instanceof Class) {
            return new FromClassGenericMetadataSupport((Class<?>) type);
        }
        if (type instanceof ParameterizedType) {
            return new FromParameterizedTypeGenericMetadataSupport((ParameterizedType) type);
        }

        throw new MockitoException("Type meta-data for this Type (" + type.getClass().getCanonicalName() + ") is not supported : " + type);
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
