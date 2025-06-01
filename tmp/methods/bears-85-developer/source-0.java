	public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {

		if (!canRead(mediaType)) {
			return false;
		}

		Class<?> rawType = ResolvableType.forType(type).getRawClass();
		Boolean result = supportedTypesCache.get(rawType);

		if (result != null) {
			return result;
		}

		result = rawType.isInterface() && AnnotationUtils.findAnnotation(rawType, ProjectedPayload.class) != null;
		supportedTypesCache.put(rawType, result);

		return result;
	}
