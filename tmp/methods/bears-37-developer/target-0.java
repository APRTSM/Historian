	public void visitMethod(RtMethod method) {
		final CtMethod<Object> ctMethod = factory.Core().createMethod();
		ctMethod.setSimpleName(method.getName());
		ctMethod.setBody(factory.Core().createBlock());
		setModifier(ctMethod, method.getModifiers());
		ctMethod.setDefaultMethod(method.isDefault());

		enter(new ExecutableRuntimeBuilderContext(ctMethod));
		super.visitMethod(method);
		exit();

		contexts.peek().addMethod(ctMethod);
	}
