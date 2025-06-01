	public <R> CtMethod<R> getOverriddenMethod() {
		//The type of this lambda expression. For example: `Consumer<Integer>`
		CtTypeReference<T> lambdaTypeRef = getType();
		if (lambdaTypeRef == null) {
			//it can be null in noclasspath mode, so we do not know which method is called, by lambda
			return null;
		}
		CtType<T> lambdaType = lambdaTypeRef.getTypeDeclaration();
		if (lambdaType.isInterface() == false) {
			throw new SpoonException("The lambda can be based on interface only. But type " + lambdaTypeRef.getQualifiedName() + " is not an interface");
		}
		Set<CtMethod<?>> lambdaTypeMethods = lambdaType.getAllMethods();
		CtMethod<?> lambdaExecutableMethod = null;
		if (lambdaTypeMethods.size() == 1) {
			//even the default method can be used, if it is the only one
			lambdaExecutableMethod = lambdaTypeMethods.iterator().next();
		} else {
			for (CtMethod<?> method : lambdaTypeMethods) {
				if (method.isDefaultMethod() || method.hasModifier(ModifierKind.PRIVATE) || method.hasModifier(ModifierKind.STATIC)) {
					continue;
				}
				if (lambdaExecutableMethod != null) {
					throw new SpoonException("The lambda can be based on interface, which has only one method. But " + lambdaTypeRef.getQualifiedName() + " has at least two: " + lambdaExecutableMethod.getSignature() + " and " + method.getSignature());
				}
				lambdaExecutableMethod = method;
			}
		}
		if (lambdaExecutableMethod == null) {
			throw new SpoonException("The lambda can be based on interface, which has one method. But " + lambdaTypeRef.getQualifiedName() + " has no one");
		}
		return (CtMethod<R>) lambdaExecutableMethod;
	}
