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
			return createReference(e.getParent(CtType.class).getReference(), lambdaMethod == null ? null : lambdaMethod.getType(), executableName, refs);
		} else if (e instanceof CtAnonymousExecutable) {
			return createReference(((CtAnonymousExecutable) e).getDeclaringType().getReference(), e.getType().clone(), executableName);
		}
		// constructor
		return createReference(((CtConstructor<T>) e).getDeclaringType().getReference(), ((CtConstructor<T>) e).getType().clone(), CtExecutableReference.CONSTRUCTOR_NAME, refs);
	}
	public Set<CtMethod<?>> getAllMethods() {
		final Set<String> distinctSignatures = new HashSet<>();
		final Set<CtMethod<?>> l = new SignatureBasedSortedSet<>();
		map(new AllTypeMembersFunction(CtMethod.class)).forEach(new CtConsumer<CtMethod<?>>() {
			@Override
			public void accept(CtMethod<?> method) {
				if (distinctSignatures.add(method.getSignature())) {
					l.add(method);
				}
			}
		});
		return Collections.unmodifiableSet(l);
	}
