	public CtTypeReference<?> getAccessType() {
		CtTypeReference<?> declType = this.getDeclaringType();
		if (declType == null) {
			throw new SpoonException("The nestedType is expected, but it is: " + getQualifiedName());
		}
		CtType<?> contextType = getParent(CtType.class);
		if (contextType == null) {
			return declType;
		}
		CtTypeReference<?> contextTypeRef = contextType.getReference();
		if (contextType != null && contextTypeRef.canAccess(declType) == false) {
			//search for visible declaring type
			CtTypeReference<?> visibleDeclType = null;
			CtTypeReference<?> type = contextTypeRef;
			//search which type or declaring type of startType extends from nestedType
			while (visibleDeclType == null && type != null) {
				visibleDeclType = getLastVisibleSuperClassExtendingFrom(type, declType);
				if (visibleDeclType != null) {
					//found one!
					applyActualTypeArguments(visibleDeclType, declType);
					break;
				}
				//try class hierarchy of declaring type
				type = type.getDeclaringType();
			}
			declType = visibleDeclType;
		}
		if (declType == null) {
			throw new SpoonException("Cannot compute access path to type: " + this.getQualifiedName() + " in context of type: " + contextType.getQualifiedName());
		}
		return declType;
	}
	public boolean canAccess(CtTypeReference<?> type) {
		try {
			Set<ModifierKind> modifiers = type.getModifiers();

			if (modifiers.contains(ModifierKind.PUBLIC)) {
				return true;
			}
			if (modifiers.contains(ModifierKind.PROTECTED)) {
				if (isSubtypeOf(type)) {
					//is visible in subtypes
					return true;
				} //else it is visible in same package, like package protected
			}
			if (modifiers.contains(ModifierKind.PRIVATE)) {
				//it is visible in scope of the same class only
				return type.getTopLevelType().getQualifiedName().equals(this.getTopLevelType().getQualifiedName());
			}
			//package protected
			if (type.getTopLevelType().getPackage().getSimpleName().equals(this.getTopLevelType().getPackage().getSimpleName())) {
				//visible only in scope of the same package
				return true;
			}
			return false;
		} catch (SpoonClassNotFoundException e) {
			handleParentNotFound(e);
			//if the modifiers cannot be resolved then we expect that it is visible
			return true;
		}
	}
