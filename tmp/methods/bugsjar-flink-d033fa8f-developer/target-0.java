	private static void validateInputType(Class<?> baseClass, Class<?> clazz, int inputParamPos, TypeInformation<?> inTypeInfo) {
		ArrayList<Type> typeHierarchy = new ArrayList<Type>();

		// try to get generic parameter
		Type inType;
		try {
			inType = getParameterType(baseClass, typeHierarchy, clazz, inputParamPos);
		}
		catch (IllegalArgumentException e) {
			return; // skip input validation e.g. for raw types
		}

		try {
			validateInfo(typeHierarchy, inType, inTypeInfo);
		}
		catch(InvalidTypesException e) {
			throw new InvalidTypesException("Input mismatch: " + e.getMessage());
		}
	}
