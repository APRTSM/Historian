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
		return createReference(((CtConstructor<T>) e).getDeclaringType().getReference(), ((CtConstructor<T>) e).getType().clone(), CtExecutableReference.CONSTRUCTOR_NAME, refs);
	}
	private <T> boolean hasChildEqualsToType(CtConstructorCall<T> ctConstructorCall) {
		final AllocationExpression parent = (AllocationExpression) jdtTreeBuilder.getContextBuilder().stack.peek().node;
		// Type is equals to the jdt child.
		return parent.type != null && parent.type.equals(childJDT)
				// Type not yet initialized.
				&& !((CtTypeAccess) child).getAccessedType().equals(ctConstructorCall.getExecutable().getType());
	}
	<T> CtExecutableReference<T> getExecutableReference(MethodBinding exec) {
		if (exec == null) {
			return null;
		}

		final CtExecutableReference ref = this.jdtTreeBuilder.getFactory().Core().createExecutableReference();
		ref.setSimpleName(new String(exec.selector));
		ref.setType(getTypeReference(exec.returnType));

		if (exec instanceof ProblemMethodBinding) {
			if (exec.declaringClass != null && Arrays.asList(exec.declaringClass.methods()).contains(exec)) {
				ref.setDeclaringType(getTypeReference(exec.declaringClass));
			} else {
				final CtReference declaringType = getDeclaringReferenceFromImports(exec.constantPoolName());
				if (declaringType instanceof CtTypeReference) {
					ref.setDeclaringType((CtTypeReference<?>) declaringType);
				}
			}
			if (exec.isConstructor()) {
				// super() invocation have a good declaring class.
				ref.setDeclaringType(getTypeReference(exec.declaringClass));
			}
			ref.setStatic(true);
		} else {
			ref.setDeclaringType(getTypeReference(exec.declaringClass));
			ref.setStatic(exec.isStatic());
		}

		if (exec.declaringClass instanceof ParameterizedTypeBinding) {
			ref.setDeclaringType(getTypeReference(exec.declaringClass.actualType()));
		}

		// original() method returns a result not null when the current method is generic.
		if (exec.original() != null) {
			final List<CtTypeReference<?>> parameters = new ArrayList<>(exec.original().parameters.length);
			for (TypeBinding b : exec.original().parameters) {
				parameters.add(getTypeReference(b));
			}
			ref.setParameters(parameters);
		} else if (exec.parameters != null) {
			// This is a method without a generic argument.
			final List<CtTypeReference<?>> parameters = new ArrayList<>();
			for (TypeBinding b : exec.parameters) {
				parameters.add(getTypeReference(b));
			}
			ref.setParameters(parameters);
		}

		return ref;
	}
