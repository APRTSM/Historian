	private <T> CtExecutableReference<T> createReferenceInternal(CtExecutable<T> e) {
		CtTypeReference<?> refs[] = new CtTypeReference[e.getParameters().size()];
		int i = 0;
		for (CtParameter<?> param : e.getParameters()) {
			refs[i++] = param.getType() != null
					? param.getType().clone()
					// With a lambda and in noclasspath (when the type of
					// parameters isn't specified), we assume Object.
					: factory.Type().OBJECT.clone();
		}
		String executableName = e.getSimpleName();
		if (e instanceof CtMethod) {
			boolean isStatic = ((CtMethod) e).hasModifier(ModifierKind.STATIC);
			return createReference(((CtMethod<T>) e).getDeclaringType().getReference(), isStatic, ((CtMethod<T>) e).getType().clone(), executableName, refs);
		} else if (e instanceof CtLambda) {
			CtMethod<T> lambdaMethod = ((CtLambda) e).getOverriddenMethod();
			return createReference(e.getParent(CtType.class).getReference(), lambdaMethod == null ? null : lambdaMethod.getType().clone(), executableName, refs);
		} else if (e instanceof CtAnonymousExecutable) {
			return createReference(((CtAnonymousExecutable) e).getDeclaringType().getReference(), e.getType().clone(), executableName);
		}
		// constructor
		return createReference(((CtConstructor<T>) e).getDeclaringType().getReference(), ((CtConstructor<T>) e).getType().clone(), CtExecutableReference.CONSTRUCTOR_NAME, refs);
	}
	public Set<CtMethod<?>> getAllMethods() {
		final Set<CtMethod<?>> l = new HashSet<>();
		final ClassTypingContext ctc = new ClassTypingContext(this);
		map(new AllTypeMembersFunction(CtMethod.class)).forEach(new CtConsumer<CtMethod<?>>() {
			@Override
			public void accept(CtMethod<?> currentMethod) {
				for (CtMethod<?> alreadyVisitedMethod : l) {
					if (ctc.isSameSignature(currentMethod, alreadyVisitedMethod)) {
						return;
					}
				}

				l.add(currentMethod);
			}
		});
		return Collections.unmodifiableSet(l);
	}
	protected CtTypeReference<?> adaptTypeParameter(CtTypeParameter typeParam) {
		if (typeParam == null) {
			throw new SpoonException("You cannot adapt a null type parameter.");
		}
		CtFormalTypeDeclarer declarer = typeParam.getTypeParameterDeclarer();
		if ((declarer instanceof CtType<?>) == false) {
			return null;
		}
		//get the actual type argument values for the declarer of `typeParam`
		List<CtTypeReference<?>> actualTypeArguments = resolveActualTypeArgumentsOf(((CtType<?>) declarer).getReference());
		if (actualTypeArguments == null) {
			if (enclosingClassTypingContext != null) {
				//try to adapt parameter using enclosing class typing context
				return enclosingClassTypingContext.adaptType(typeParam);
			}
			return null;
		}
		return getValue(actualTypeArguments, typeParam, declarer);
	}
	private CtTypeReference<?> adaptTypeForNewMethod(CtTypeReference<?> typeRef) {
		if (typeRef == null) {
			return null;
		}
		if (typeRef instanceof CtTypeParameterReference) {
			CtTypeParameterReference typeParamRef = (CtTypeParameterReference) typeRef;
			CtTypeParameter typeParam = typeParamRef.getDeclaration();
			if (typeParam == null) {
				throw new SpoonException("Declaration of the CtTypeParameter should not be null.");
			}

			if (typeParam.getTypeParameterDeclarer() instanceof CtExecutable) {
				//the parameter is declared in scope of Method or Constructor
				return typeRef.clone();
			}
		}
		return adaptType(typeRef);
	}
