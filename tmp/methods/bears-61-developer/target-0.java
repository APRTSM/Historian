	public <T> CtTypeReference<T> createCtTypeReference(Class<?> originalClass) {
		if (originalClass == null) {
			return null;
		}
		CtTypeReference<T> typeReference = factory.Core().<T>createTypeReference();
		typeReference.setSimpleName(originalClass.getSimpleName());
		if (originalClass.isPrimitive()) {
			return typeReference;
		}
		if (originalClass.getDeclaringClass() != null) {
			// the inner class reference does not have package
			return typeReference.setDeclaringType(createCtTypeReference(originalClass.getDeclaringClass()));
		}
		return typeReference.setPackage(createCtPackageReference(originalClass.getPackage()));
	}
