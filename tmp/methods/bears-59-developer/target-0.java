	public void visitCtTypeParameterReference(CtTypeParameterReference ref) {
		if (ref.isImplicit()) {
			return;
		}
		elementPrinterHelper.writeAnnotations(ref);
		if (printQualified(ref)) {
			printer.write(ref.getQualifiedName());
		} else {
			printer.write(ref.getSimpleName());
		}
	}
	public void visitCtWildcardReference(CtWildcardReference wildcardReference) {
		if (wildcardReference.isImplicit()) {
			return;
		}
		elementPrinterHelper.writeAnnotations(wildcardReference);
		if (printQualified(wildcardReference)) {
			printer.write(wildcardReference.getQualifiedName());
		} else {
			printer.write(wildcardReference.getSimpleName());
		}
		if (wildcardReference.getBoundingType() != null) {
			if (wildcardReference.isUpper()) {
				printer.write(" extends ");
			} else {
				printer.write(" super ");
			}
			scan(wildcardReference.getBoundingType());
		}
	}
	public void visitCtTypeParameter(CtTypeParameter typeParameter) {
		CtTypeParameterReference ref = typeParameter.getReference();
		if (ref.isImplicit()) {
			return;
		}
		elementPrinterHelper.writeAnnotations(ref);
		if (printQualified(ref)) {
			printer.write(ref.getQualifiedName());
		} else {
			printer.write(ref.getSimpleName());
		}
		if (ref.getBoundingType() != null) {
			if (ref.isUpper()) {
				printer.write(" extends ");
			} else {
				printer.write(" super ");
			}
			scan(ref.getBoundingType());
		}
	}
