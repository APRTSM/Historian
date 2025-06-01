		private boolean isSameSignatureLikeScopeMethod(CtExecutable<?> thatExecutable, boolean canTypeErasure) {
			//https://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.4.2
			CtFormalTypeDeclarer thatDeclarer = (CtFormalTypeDeclarer) thatExecutable;
			CtFormalTypeDeclarer thisDeclarer = getAdaptationScope();
			CtExecutable<?> thisExecutable = (CtExecutable<?>) thisDeclarer;
			if (thatExecutable.getSimpleName().equals(thisExecutable.getSimpleName()) == false) {
				return false;
			}
			if (thisExecutable.getParameters().size() != thatExecutable.getParameters().size()) {
				//the executables has different count of parameters they cannot have same signature
				return false;
			}
			List<CtTypeParameter> thisTypeParameters = thisDeclarer.getFormalCtTypeParameters();
			List<CtTypeParameter> thatTypeParameters = thatDeclarer.getFormalCtTypeParameters();
			boolean useTypeErasure = false;
			if (thisTypeParameters.size() == thatTypeParameters.size()) {
				//the methods has same count of formal parameters
				//check that formal type parameters are same
				if (hasSameMethodFormalTypeParameters((CtFormalTypeDeclarer) thatExecutable) == false) {
					return false;
				}
			} else {
				//the methods has different count of formal type parameters.
				if (canTypeErasure == false) {
					//type erasure is not allowed. So non-generic methods cannot match with generic methods
					return false;
				}
				//non-generic method can override a generic one if type erasure is allowed
				if (thisTypeParameters.isEmpty() == false) {
					//scope methods has some parameters. It is generic too, it is not a subsignature of that method
					return false;
				}
				//scope method has zero formal type parameters. It is not generic.
				useTypeErasure = true;
			}
			List<CtTypeReference<?>> thisParameterTypes = getParameterTypes(thisExecutable.getParameters());
			List<CtTypeReference<?>> thatParameterTypes = getParameterTypes(thatExecutable.getParameters());
			//check that parameters are same after adapting to the same scope
			for (int i = 0; i < thisParameterTypes.size(); i++) {
				CtTypeReference<?> thisType = thisParameterTypes.get(i);
				CtTypeReference<?> thatType = thatParameterTypes.get(i);
				if (useTypeErasure) {
					if (thatType instanceof CtTypeParameterReference) {
						thatType = ((CtTypeParameterReference) thatType).getTypeErasure();
					}
				} else {
					thatType = adaptType(thatType);
				}
				if (thatType == null) {
					//the type cannot be adapted.
					return false;
				}

				// we can be in a case where thisType is CtType and thatType is CtType<?>
				// the types are not equals but it's overridden
				// in that specific case we simply remove the list of actualTypeArguments from thatType
				if (thisType.getActualTypeArguments().isEmpty() && thatType.getActualTypeArguments().size() == 1) {
					CtTypeReference actualTA = thatType.getActualTypeArguments().get(0);
					if (actualTA instanceof CtWildcardReference) {
						if (((CtWildcardReference) actualTA).getBoundingType() == null) {
							thatType.setActualTypeArguments(Collections.EMPTY_LIST);
						}
					}
				}

				if (thisType.equals(thatType) == false) {
					return false;
				}
			}
			return true;
		}
	static void insertAllMethods(CtType<?> targetType, Template<?> template, CtClass<?> sourceClass) {

		Set<CtMethod<?>> methodsOfTemplate = sourceClass.getFactory().Type().get(Template.class).getMethods();
		// insert all the methods
		for (CtMethod<?> m : sourceClass.getMethods()) {
			if (m.getAnnotation(Local.class) != null) {
				continue;
			}
			if (m.getAnnotation(Parameter.class) != null) {
				continue;
			}

			boolean isOverridingTemplateItf = false;
			for (CtMethod m2 : methodsOfTemplate) {
				if (m.isOverriding(m2)) {
					isOverridingTemplateItf = true;
				}
			}

			if (isOverridingTemplateItf) {
				continue;
			}

			insertMethod(targetType, template, m);
		}
	}
