	public void visitMethod(RtMethod method) {
		final CtMethod<Object> ctMethod = factory.Core().createMethod();
		ctMethod.setSimpleName(method.getName());
		ctMethod.setBody(factory.Core().createBlock());
		setModifier(ctMethod, method.getModifiers());

		enter(new ExecutableRuntimeBuilderContext(ctMethod));
		super.visitMethod(method);
		exit();

		contexts.peek().addMethod(ctMethod);
	}
	public RtMethod(Class<?> clazz, String name, Class<?> returnType, TypeVariable<Method>[] typeParameters, Class<?>[] parameterTypes, Class<?>[] exceptionTypes, int modifiers, Annotation[] annotations,
			Annotation[][] parameterAnnotations, boolean isVarArgs) {
		this.clazz = clazz;
		this.name = name;
		this.returnType = returnType;
		this.typeParameters = typeParameters;
		this.parameterTypes = parameterTypes;
		this.exceptionTypes = exceptionTypes;
		this.modifiers = modifiers;
		this.annotations = annotations;
		this.parameterAnnotations = parameterAnnotations;
		this.isVarArgs = isVarArgs;
	}
	public static RtMethod create(Method method) {
		return new RtMethod(method.getDeclaringClass(), method.getName(), method.getReturnType(),
				method.getTypeParameters(), method.getParameterTypes(), method.getExceptionTypes(), method.getModifiers(),
				method.getDeclaredAnnotations(), method.getParameterAnnotations(), method.isVarArgs());
	}
