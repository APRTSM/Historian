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
