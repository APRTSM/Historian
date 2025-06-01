	private boolean cacheAndReturn(boolean resolved) {

		this.resolved = resolved;
		return resolved;
	}
	private static Map<TypeVariable<?>, Type> calculateTypeVariables(ParameterizedType type, Class<?> resolvedType,
			TypeDiscoverer<?> parent) {

		TypeVariable<?>[] typeParameters = resolvedType.getTypeParameters();
		Type[] arguments = type.getActualTypeArguments();

		Map<TypeVariable<?>, Type> localTypeVariables = new HashMap<TypeVariable<?>, Type>(parent.getTypeVariableMap());

		for (int i = 0; i < typeParameters.length; i++) {

			Type value = arguments[i];

			if (!(value instanceof TypeVariable)) {
				localTypeVariables.put(typeParameters[i], value);
			}
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
			return new TypeVariableTypeInformation(variable, type, this);
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
