	public void visitAnnotation(final Annotation annotation) {
		final CtAnnotation<Annotation> ctAnnotation = factory.Core().createAnnotation();

		enter(new AnnotationRuntimeBuilderContext(ctAnnotation) {
			@Override
			public void addMethod(CtMethod ctMethod) {
				try {
					Object value = annotation.getClass().getMethod(ctMethod.getSimpleName()).invoke(annotation);
					ctAnnotation.addValue(ctMethod.getSimpleName(), value);
				} catch (Exception ignore) {
					ctAnnotation.addValue(ctMethod.getSimpleName(), "");
				}
			}
		});
		super.visitAnnotation(annotation);
		exit();

		contexts.peek().addAnnotation(ctAnnotation);
	}
