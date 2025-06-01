	public <T> CtExecutableReference<T> createReference(CtExecutable<T> e) {
		CtTypeReference<?> refs[] = new CtTypeReference[e.getParameters().size()];
		int i = 0;
		for (CtParameter<?> param : e.getParameters()) {
			if (param.getType() != null) {
				// With a lambda and in noclasspath (when the type of parameters isn't specified), we don't have a type.
				refs[i++] = param.getType().clone();
			}
		}
		String executableName = e.getSimpleName();
		if (e instanceof CtMethod) {
			boolean isStatic = ((CtMethod) e).hasModifier(ModifierKind.STATIC);
			return createReference(((CtMethod<T>) e).getDeclaringType().getReference(), isStatic, ((CtMethod<T>) e).getType().clone(), executableName, refs);
		} else if (e instanceof CtLambda) {
			return createReference(e.getParent(CtType.class).getReference(), e.getType(), executableName, refs);
		} else if (e instanceof CtAnonymousExecutable) {
			return createReference(((CtAnonymousExecutable) e).getDeclaringType().getReference(), e.getType().clone(), executableName);
		}
		// constructor
		return createReference(((CtConstructor<T>) e).getDeclaringType().getReference(), ((CtConstructor<T>) e).getType().clone(), CtExecutableReference.CONSTRUCTOR_NAME, refs);
	}
	private <T> boolean hasChildEqualsToType(CtConstructorCall<T> ctConstructorCall) {
		final AllocationExpression parent = (AllocationExpression) jdtTreeBuilder.getContextBuilder().stack.peek().node;
		// Type is equals to the jdt child.
		return parent.type != null && parent.type.equals(childJDT);
	}
