	public static void changeTypeName(final CtType<?> type, String name) {

		final String typeQFN = type.getQualifiedName();

		final List<CtTypeReference<?>> references = Query.getElements(type.getFactory(), new TypeFilter<CtTypeReference<?>>(CtTypeReference.class) {
			@Override
			public boolean matches(CtTypeReference<?> reference) {
				String refFQN = reference.getQualifiedName();
				return typeQFN.equals(refFQN);
			}
		});

		type.setSimpleName(name);
		for (CtTypeReference<?> reference : references) {
			reference.setSimpleName(name);
		}
	}
	private static CtStatement internalCompileStatement(CtElement st, CtTypeReference returnType) {
		Factory f = st.getFactory();

		String contents = createWrapperContent(st, f, returnType);

		build(f, contents);

		CtType<?> c = f.Type().get(WRAPPER_CLASS_NAME);

		// Get the part we want

		CtMethod<?> wrapper = c.getMethod(WRAPPER_METHOD_NAME);

		List<CtStatement> statements = wrapper.getBody().getStatements();
		CtStatement ret = statements.get(statements.size() - 1);

		// Clean up
		c.getPackage().getTypes().remove(c);

		if (ret instanceof CtClass) {
			CtClass klass = (CtClass) ret;
			ret.getFactory().Package().getRootPackage().addType(klass);
			klass.setSimpleName(klass.getSimpleName().replaceAll("^[0-9]*", ""));
		}
		return ret;
	}
	public <T extends CtNamedElement> T setSimpleName(String simpleName) {
		Factory factory = getFactory();
		if (factory instanceof FactoryImpl) {
			simpleName = ((FactoryImpl) factory).dedup(simpleName);
		}

		this.simpleName = simpleName;
		return (T) this;
	}
	public CtField<T> getDeclaration() {
		return fromDeclaringType();
	}
