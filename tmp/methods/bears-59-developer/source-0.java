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
		if (ref.getBoundingType() != null) {
			if (ref.isUpper()) {
				printer.write(" extends ");
			} else {
				printer.write(" super ");
			}
			scan(ref.getBoundingType());
		}
	}
	public void visitCtTypeParameter(CtTypeParameter typeParameter) {
		visitCtTypeParameterReference(typeParameter.getReference());
	}
	public void visitCtWildcardReference(CtWildcardReference wildcardReference) {
		visitCtTypeParameterReference(wildcardReference);
	}
