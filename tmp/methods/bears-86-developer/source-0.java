	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().isInterface();
	}
