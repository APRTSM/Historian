	public boolean supportsParameter(MethodParameter parameter) {

		Class<?> type = parameter.getParameterType();

		if (!type.isInterface()) {
			return false;
		}

		// Annotated parameter
		if (parameter.getParameterAnnotation(ProjectedPayload.class) != null) {
			return true;
		}

		// Annotated type
		if (AnnotatedElementUtils.findMergedAnnotation(type, ProjectedPayload.class) != null) {
			return true;
		}

		// Fallback for only user defined interfaces
		for (String prefix : IGNORED_PACKAGES) {
			if (ClassUtils.getPackageName(type).startsWith(prefix)) {
				return false;
			}
		}

		return true;
	}
