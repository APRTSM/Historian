	public boolean isOverriding(CtExecutableReference<?> executable) {
		final boolean isSame = getSimpleName().equals(executable.getSimpleName()) && getParameters().equals(executable.getParameters()) && getActualTypeArguments().equals(executable.getActualTypeArguments());
		if (!isSame) {
			return false;
		}
		if (getDeclaringType().isAnonymous()) {
			if (!getDeclaringType().getDeclaringType().isSubtypeOf(executable.getDeclaringType())) {
				return false;
			}
		} else if (!getDeclaringType().isSubtypeOf(executable.getDeclaringType())) {
			return false;
		}
		return true;
	}
