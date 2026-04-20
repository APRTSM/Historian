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
