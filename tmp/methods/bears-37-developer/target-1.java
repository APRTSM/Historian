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
	private static boolean _java8_isDefault(Method method) {
		if (_method_isDefault == null) {
			//spoon is running with java 7 JDK, all methods are not default, because java 7 does not have default methods
			return false;
		}
		try {
			return (Boolean) _method_isDefault.invoke(method);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new SpoonException("Calling of Java8 Method#isDefault() failed", e);
		} catch (InvocationTargetException e) {
			throw new SpoonException("Calling of Java8 Method#isDefault() failed", e.getTargetException());
		}
	}
	public RtMethod(Class<?> clazz, String name, Class<?> returnType, TypeVariable<Method>[] typeParameters, Class<?>[] parameterTypes, Class<?>[] exceptionTypes, int modifiers, Annotation[] annotations,
			Annotation[][] parameterAnnotations, boolean isVarArgs, boolean isDefault) {
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
		this.isDefault = isDefault;
	}
	public boolean isDefault() {
		return isDefault;
	}
	public static RtMethod create(Method method) {
		return new RtMethod(method.getDeclaringClass(), method.getName(), method.getReturnType(),
				method.getTypeParameters(), method.getParameterTypes(), method.getExceptionTypes(), method.getModifiers(),
				method.getDeclaredAnnotations(), method.getParameterAnnotations(), method.isVarArgs(),
				//spoon is compatible with Java 7, so compilation fails here
				//method.isDefault());
				_java8_isDefault(method));
	}
