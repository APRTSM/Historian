	public void visitAnnotation(Annotation annotation) {
		final CtAnnotation<Annotation> ctAnnotation = factory.Core().createAnnotation();

		enter(new AnnotationRuntimeBuilderContext(ctAnnotation));
		super.visitAnnotation(annotation);
		exit();

		contexts.peek().addAnnotation(ctAnnotation);
	}
	public void visitAnnotation(Annotation annotation) {
		if (annotation.annotationType() != null) {
			visitClassReference(annotation.annotationType());
		}
	}
