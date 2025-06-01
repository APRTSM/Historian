	public CtTypeParameter getDeclaration() {
		if (!isParentInitialized()) {
			return null;
		}

		// case #1: we're a type of a method parameter, a local variable, ...
		// the strategy is to look in the parents
		// collecting all formal type declarers of the hierarchy
		CtElement e = this;
		while ((e = e.getParent(CtFormalTypeDeclarer.class)) != null) {
			CtTypeParameter result = findTypeParamDeclaration((CtFormalTypeDeclarer) e, this.getSimpleName());
			if (result != null) {
				return result;
			}
		}
		return null;
	}
