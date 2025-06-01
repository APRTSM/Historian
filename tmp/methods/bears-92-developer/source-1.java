	private static <T> Lazy<Optional<T>> detectAnnotation(Object entity, Class<? extends Annotation> annotationType) {

		return Lazy.of(() -> {

			AnnotationDetectionFieldCallback numberCallback = new AnnotationDetectionFieldCallback(annotationType);
			ReflectionUtils.doWithFields(entity.getClass(), numberCallback);
			return numberCallback.getValue(entity);
		});
	}
	default LocalDateTime getRequiredRevisionDate() {
		return getRevisionDate()
				.orElseThrow(() -> new IllegalStateException(String.format("No revision date found on %s!", getDelegate())));
	}
	default N getRequiredRevisionNumber() {
		return getRevisionNumber()
				.orElseThrow(() -> new IllegalStateException(String.format("No revision number found on %s!", getDelegate())));
	}
