    public static GenericMetadataSupport inferFrom(Type type) {
        Checks.checkNotNull(type, "type");
        if (type instanceof Class) {
            if (type instanceof ParameterizedType) {
				return new FromParameterizedTypeGenericMetadataSupport(
						(ParameterizedType) type);
			}
			return new FromClassGenericMetadataSupport((Class<?>) type);
        }
        if (type instanceof ParameterizedType) {
            return new FromParameterizedTypeGenericMetadataSupport((ParameterizedType) type);
        }

        throw new MockitoException("Type meta-data for this Type (" + type.getClass().getCanonicalName() + ") is not supported : " + type);
    }
        private void readActualTypeParametersOnDeclaringClass(Class<?> clazz) {
            registerTypeParametersOn(clazz.getTypeParameters());
            registerTypeVariablesOn(clazz.getGenericSuperclass());
            registerTypeParametersOn(clazz.getTypeParameters());
			for (Type genericInterface : clazz.getGenericInterfaces()) {
                registerTypeVariablesOn(genericInterface);
            }
        }
