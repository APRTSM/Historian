	public CtTypeParameter getDeclaration() {
		if (!isParentInitialized()) {
			return null;
		}

		CtElement e = this;
		CtElement parent = getParent();
		if (parent instanceof CtTypeReference) {
			if (parent.isParentInitialized() == false) {
				return null;
			}
			parent = parent.getParent();
		}
		if (parent instanceof CtExecutableReference) {
			CtExecutableReference parentExec = (CtExecutableReference) parent;
			if (!parentExec.getDeclaringType().equals(e)) {
				CtElement parent2 = parentExec.getExecutableDeclaration();
				if (parent2 instanceof CtMethod) {
					e = parent2;
				} else {
					e = e.getParent(CtFormalTypeDeclarer.class);
				}
			} else {
				e = e.getParent(CtFormalTypeDeclarer.class);
			}

		} else {
			e = e.getParent(CtFormalTypeDeclarer.class);
		}

		// case #1: we're a type of a method parameter, a local variable, ...
		// the strategy is to look in the parents
		// collecting all formal type declarers of the hierarchy
		while (e != null) {
			CtTypeParameter result = findTypeParamDeclaration((CtFormalTypeDeclarer) e, this.getSimpleName());
			if (result != null) {
				return result;
			}
			e = e.getParent(CtFormalTypeDeclarer.class);
		}
		return null;
	}
