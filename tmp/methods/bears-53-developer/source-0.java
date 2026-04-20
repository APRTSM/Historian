	void setPackageOrDeclaringType(CtTypeReference<?> ref, CtReference declaring) {
		if (declaring instanceof CtPackageReference) {
			ref.setPackage((CtPackageReference) declaring);
		} else if (declaring instanceof CtTypeReference) {
			ref.setDeclaringType((CtTypeReference) declaring);
		} else if (declaring == null) {
			ref.setPackage(jdtTreeBuilder.getFactory().Package().topLevel());
		} else {
			throw new AssertionError("unexpected declaring type: " + declaring.getClass() + " of " + declaring);
		}
	}
