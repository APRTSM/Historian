	public Collection<CtExecutableReference<?>> getDeclaredExecutables() {
		CtType<T> t = getDeclaration();
		if (t == null) {
			try {
				return RtHelper.getAllExecutables(getActualClass(), getFactory());
			} catch (final SpoonClassNotFoundException e) {
				if (getFactory().getEnvironment().getNoClasspath()) {
					return Collections.emptyList();
				}
				throw e;
			}
		} else {
			return t.getDeclaredExecutables();
		}
	}
	public CtTypeReference<?> getSuperclass() {
		CtType<T> t = getDeclaration();
		if (t != null) {
			return t.getSuperclass();
		} else {
			try {
				Class<T> c = getActualClass();
				Class<?> sc = c.getSuperclass();
				if (sc == null) {
					return null;
				}
				return getFactory().Type().createReference(sc);
			} catch (final SpoonClassNotFoundException e) {
				if (getFactory().getEnvironment().getNoClasspath()) {
					return null;
				}
				throw e;
			}
		}
	}
