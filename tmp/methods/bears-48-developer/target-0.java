	private List<CtTypeReference<?>> getActualTypeArguments(String qualifiedName) {
		List<CtTypeReference<?>> actualTypeArguments = typeToArguments.get(qualifiedName);
		if (actualTypeArguments != null) {
			return actualTypeArguments;
		}
		if (enclosingClassTypingContext != null) {
			return enclosingClassTypingContext.getActualTypeArguments(qualifiedName);
		}
		return null;
	}
	private List<CtTypeReference<?>> resolveTypeParameters(List<CtTypeReference<?>> typeRefs) {
		List<CtTypeReference<?>> result = new ArrayList<>(typeRefs.size());
		for (CtTypeReference<?> typeRef : typeRefs) {
			if (typeRef instanceof CtTypeParameterReference) {
				CtTypeParameterReference typeParamRef = (CtTypeParameterReference) typeRef;
				CtTypeParameter typeParam = typeParamRef.getDeclaration();
				CtFormalTypeDeclarer declarer = typeParam.getTypeParameterDeclarer();
				if ((declarer instanceof CtType<?>) == false) {
					throw new SpoonException("Cannot adapt type parameters of non type scope");
				}
				CtType<?> typeDeclarer = (CtType<?>) declarer;
				List<CtTypeReference<?>> actualTypeArguments = getActualTypeArguments(typeDeclarer.getQualifiedName());
				if (actualTypeArguments == null) {
					/*
					 * the actualTypeArguments of this declarer cannot be resolved.
					 * There is probably a model inconsistency
					 */
					throw new SpoonException("Cannot resolve " + (result.size() + 1) + ") type parameter <" + typeParamRef.getSimpleName() + ">  of declarer " + declarer);
				}
				if (actualTypeArguments.size() != typeDeclarer.getFormalCtTypeParameters().size()) {
					if (actualTypeArguments.isEmpty() == false) {
						throw new SpoonException("Unexpected actual type arguments " + actualTypeArguments + " on " + typeDeclarer);
					}
					/*
					 * the scope type was delivered as type reference without appropriate type arguments.
					 * Use references to formal type parameters
					 */
					actualTypeArguments = getTypeReferences(typeDeclarer.getFormalCtTypeParameters());
					typeToArguments.put(typeDeclarer.getQualifiedName(), actualTypeArguments);
				}
				typeRef = getValue(actualTypeArguments, typeParam, declarer);
			}
			result.add(typeRef);
		}
		return result;
	}
