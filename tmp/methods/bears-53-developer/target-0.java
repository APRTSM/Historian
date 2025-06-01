	void setPackageOrDeclaringType(CtTypeReference<?> ref, CtReference declaring) {
		if (declaring instanceof CtPackageReference) {
			ref.setPackage((CtPackageReference) declaring);
		} else if (declaring instanceof CtTypeReference) {
			ref.setDeclaringType((CtTypeReference) declaring);
		} else if (declaring == null) {
			// in that case we consider the package should be the same as the current one. Fix #1293
			ref.setPackage(jdtTreeBuilder.getContextBuilder().compilationUnitSpoon.getDeclaredPackage().getReference());
		} else {
			throw new AssertionError("unexpected declaring type: " + declaring.getClass() + " of " + declaring);
		}
	}
