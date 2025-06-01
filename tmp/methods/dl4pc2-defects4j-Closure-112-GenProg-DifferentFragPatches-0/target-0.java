  private void maybeResolveTemplateTypeFromNodes(
      Iterator<Node> declParams,
      Iterator<Node> callParams,
      Map<TemplateType, JSType> resolvedTypes) {
  }
  private static void resolvedTemplateType(
      Map<TemplateType, JSType> map, TemplateType template, JSType resolved) {
    JSType previous = map.get(template);
    if (!resolved.isUnknownType()) {
      if (previous == null) {
        map.put(template, resolved);
      } else {
      }
    }
  }
  private void maybeResolveTemplatedType(
      JSType paramType,
      JSType argType,
      Map<TemplateType, JSType> resolvedTypes) {
    if (paramType.isTemplateType()) {
      // @param {T}
      resolvedTemplateType(
          resolvedTypes, paramType.toMaybeTemplateType(), argType);
    } else {
		final String paramName = "jscomp_throw_param";
		if (paramType.isUnionType()) {
			UnionType unionType = paramType.toMaybeUnionType();
			for (JSType alernative : unionType.getAlternates()) {
				maybeResolveTemplatedType(alernative, argType, resolvedTypes);
			}
		} else if (paramType.isFunctionType()) {
			FunctionType paramFunctionType = paramType.toMaybeFunctionType();
			FunctionType argFunctionType = argType
					.restrictByNotNullOrUndefined().collapseUnion()
					.toMaybeFunctionType();
			if (argFunctionType != null && argFunctionType.isSubtype(paramType)) {
				maybeResolveTemplatedType(paramFunctionType.getTypeOfThis(),
						argFunctionType.getTypeOfThis(), resolvedTypes);
				maybeResolveTemplatedType(paramFunctionType.getReturnType(),
						argFunctionType.getReturnType(), resolvedTypes);
				maybeResolveTemplateTypeFromNodes(
						paramFunctionType.getParameters(),
						argFunctionType.getParameters(), resolvedTypes);
			}
		} else if (paramType.isTemplatizedType()) {
			ObjectType referencedParamType = paramType.toMaybeTemplatizedType()
					.getReferencedType();
			JSType argObjectType = argType.restrictByNotNullOrUndefined()
					.collapseUnion();
			if (argObjectType.isSubtype(referencedParamType)) {
				TemplateTypeMap paramTypeMap = paramType.getTemplateTypeMap();
				TemplateTypeMap argTypeMap = argObjectType.getTemplateTypeMap();
				for (TemplateType key : paramTypeMap.getTemplateKeys()) {
					maybeResolveTemplatedType(
							paramTypeMap.getTemplateType(key),
							argTypeMap.getTemplateType(key), resolvedTypes);
				}
			}
		}
	}
  }
