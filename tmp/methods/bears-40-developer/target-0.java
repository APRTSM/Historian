	public boolean canAccess(CtTypeReference<?> type) {
		try {
			Set<ModifierKind> modifiers = type.getModifiers();

			if (modifiers.contains(ModifierKind.PUBLIC)) {
				return true;
			}
			if (modifiers.contains(ModifierKind.PROTECTED)) {
				//the accessed type is protected in scope of declaring type.
				CtTypeReference<?> declaringType = type.getDeclaringType();
				if (declaringType == null) {
					//top level type cannot be protected. So this is a model inconsistency.
					throw new SpoonException("The protected class " + type.getQualifiedName() + " has no declaring class.");
				}
				if (isImplementationOf(declaringType)) {
					//type is visible in code which implements declaringType
					return true;
				} //else it is visible in same package, like package protected
				return isInSamePackage(type);
			}
			if (modifiers.contains(ModifierKind.PRIVATE)) {
				//it is visible in scope of the same class only
				return type.getTopLevelType().getQualifiedName().equals(this.getTopLevelType().getQualifiedName());
			}
			/*
			 * no modifier, we have to check if it is nested type and if yes, if parent is interface or class.
			 * In case of no parent then implicit access is package protected
			 * In case of parent is interface, then implicit access is PUBLIC
			 * In case of parent is class, then implicit access is package protected
			 */
			CtTypeReference<?> declaringTypeRef = type.getDeclaringType();
			if (declaringTypeRef != null && declaringTypeRef.isInterface()) {
				//the declaring type is interface, then implicit access is PUBLIC
				return true;
			}
			//package protected
			//visible only in scope of the same package
			return isInSamePackage(type);
		} catch (SpoonClassNotFoundException e) {
			handleParentNotFound(e);
			//if the modifiers cannot be resolved then we expect that it is visible
			return true;
		}
	}
	private boolean isImplementationOf(CtTypeReference<?> type) {
		CtTypeReference<?> impl = this;
		while (impl != null) {
			if (impl.isSubtypeOf(type)) {
				return true;
			}
			impl = impl.getDeclaringType();
		}
		return false;
	}
	private boolean isInSamePackage(CtTypeReference<?> type) {
		return type.getTopLevelType().getPackage().getSimpleName().equals(this.getTopLevelType().getPackage().getSimpleName());
	}
	public CtTypeReference<?> getAccessType() {
		CtTypeReference<?> declType = this.getDeclaringType();
		if (declType == null) {
			throw new SpoonException("The declaring type is expected, but " + getQualifiedName() + " is top level type");
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
