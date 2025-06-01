	public <T> CtTypeReference<T> createCtTypeReference(Class<?> originalClass) {
		if (originalClass == null) {
			return null;
		}
		CtTypeReference<T> typeReference = factory.Core().<T>createTypeReference();
		typeReference.setSimpleName(originalClass.getSimpleName());
		if (originalClass.isPrimitive()) {
			return typeReference;
		}
		return typeReference.setPackage(createCtPackageReference(originalClass.getPackage()));
	}
