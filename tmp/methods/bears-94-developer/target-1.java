	private static Type flattenTypeVariable(Type source, Map<TypeVariable<?>, Type> variables) {

		if (!(source instanceof TypeVariable)) {
			return source;
		}

		Type value = variables.get(source);

		return value == null ? source : flattenTypeVariable(value, variables);
	}
	private static Map<TypeVariable<?>, Type> calculateTypeVariables(ParameterizedType type, Class<?> resolvedType,
			TypeDiscoverer<?> parent) {

		TypeVariable<?>[] typeParameters = resolvedType.getTypeParameters();
		Type[] arguments = type.getActualTypeArguments();

		Map<TypeVariable<?>, Type> localTypeVariables = new HashMap<TypeVariable<?>, Type>(parent.getTypeVariableMap());

		for (int i = 0; i < typeParameters.length; i++) {
			localTypeVariables.put(typeParameters[i], flattenTypeVariable(arguments[i], localTypeVariables));
		}

		return localTypeVariables;
	}
	protected TypeInformation<?> createInfo(Type fieldType) {

		if (fieldType.equals(this.type)) {
			return this;
		}

		if (fieldType instanceof Class) {
			return ClassTypeInformation.from((Class<?>) fieldType);
		}

		Class<S> resolveType = resolveType(fieldType);

		if (fieldType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) fieldType;
			return new ParameterizedTypeInformation(parameterizedType, resolveType, this);
		}

		if (fieldType instanceof TypeVariable) {
			TypeVariable<?> variable = (TypeVariable<?>) fieldType;
			return new TypeVariableTypeInformation(variable, this);
		}

		if (fieldType instanceof GenericArrayType) {
			return new GenericArrayTypeInformation((GenericArrayType) fieldType, this);
		}

		if (fieldType instanceof WildcardType) {

			WildcardType wildcardType = (WildcardType) fieldType;
			Type[] bounds = wildcardType.getLowerBounds();

			if (bounds.length > 0) {
				return createInfo(bounds[0]);
			}

			bounds = wildcardType.getUpperBounds();

			if (bounds.length > 0) {
				return createInfo(bounds[0]);
			}
		}

		throw new IllegalArgumentException();
	}
