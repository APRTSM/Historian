	public static void changeTypeName(final CtType<?> type, String name) {
		final List<CtTypeReference<?>> references = Query.getElements(type.getFactory(), new TypeFilter<CtTypeReference<?>>(CtTypeReference.class) {
			@Override
			public boolean matches(CtTypeReference<?> reference) {
				return type.getQualifiedName().equals(reference.getQualifiedName());
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
			klass.setSimpleName(klass.getSimpleName().replaceAll("^[0-9]*", ""));
			klass.setParent(ret.getFactory().Package().getRootPackage());
			ret.getFactory().Package().getRootPackage().addType(klass);
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
		final CtField<T> ctField = lookupDynamically();
		if (ctField != null) {
			return ctField;
		}
		return fromDeclaringType();
	}
	private CtField<T> lookupDynamically() {
		CtElement element = this;
		CtField optional = null;
		String name = getSimpleName();
		try {
			do {
				CtType type = element.getParent(CtType.class);
				if (type == null) {
					return null;
				}
				final CtField potential = type.getField(name);
				if (potential != null) {
					optional = potential;
				}
				element = type;
			} while (optional == null);
		} catch (ParentNotInitializedException e) {
			return null;
		}
		return optional;
	}
