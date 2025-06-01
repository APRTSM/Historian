	private static <T> Lazy<Optional<T>> detectAnnotation(Object entity, Class<? extends Annotation> annotationType) {

		return Lazy.of(() -> {

			AnnotationDetectionFieldCallback numberCallback = new AnnotationDetectionFieldCallback(annotationType);
			ReflectionUtils.doWithFields(entity.getClass(), numberCallback);
			return numberCallback.getValue(entity);
		});
	}
