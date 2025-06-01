	public Collection<CtExecutableReference<?>> getDeclaredExecutables() {
		CtType<T> t = getDeclaration();
		if (t == null) {
			return RtHelper.getAllExecutables(getActualClass(), getFactory());
		} else {
			return t.getDeclaredExecutables();
		}
	}
	public CtTypeReference<?> getSuperclass() {
		CtType<T> t = getDeclaration();
		if (t != null) {
			return t.getSuperclass();
		} else {
			Class<T> c = getActualClass();
			Class<?> sc = c.getSuperclass();
			if (sc == null) {
				return null;
			}
			return getFactory().Type().createReference(sc);
		}
	}
