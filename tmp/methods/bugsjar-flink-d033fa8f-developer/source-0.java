	private static void validateInputType(Class<?> baseClass, Class<?> clazz, int inputParamPos, TypeInformation<?> inType) {
		ArrayList<Type> typeHierarchy = new ArrayList<Type>();
		try {
			validateInfo(typeHierarchy, getParameterType(baseClass, typeHierarchy, clazz, inputParamPos), inType);
		}
		catch(InvalidTypesException e) {
			throw new InvalidTypesException("Input mismatch: " + e.getMessage());
		}
	}
