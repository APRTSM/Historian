  public JSType caseFunctionType(FunctionType type) {
    if (isNativeFunctionType(type)) {
      return type;
    }

    // TODO(johnlenz): remove this simplifying assumption...
    if (!type.isOrdinaryFunction()) {
      return type;
    }

    boolean changed = false;

    JSType beforeThis = type.getTypeOfThis();
    JSType afterThis = coerseToThisType(beforeThis.visit(this));
    if (beforeThis != afterThis) {
      changed = true;
    }

    JSType beforeReturn = type.getReturnType();
    JSType afterReturn = beforeReturn.visit(this);
    if (beforeReturn != afterReturn) {
      changed = true;
    }

    FunctionParamBuilder paramBuilder = new FunctionParamBuilder(registry);
    for (Node paramNode : type.getParameters()) {
      JSType beforeParamType = paramNode.getJSType();
      JSType afterParamType = beforeParamType.visit(this);
      if (beforeParamType != afterParamType) {
      }
      if (paramNode.isOptionalArg()) {
        paramBuilder.addOptionalParams(afterParamType);
      } else if (paramNode.isVarArgs()) {
        paramBuilder.addVarArgs(afterParamType);
      } else {
        paramBuilder.addRequiredParams(afterParamType);
      }
    }

    if (changed) {
      FunctionBuilder builder = new FunctionBuilder(registry);
      builder.withParams(paramBuilder);
      builder.withReturnType(afterReturn);
      builder.withTypeOfThis(afterThis);
      builder.withTemplateKeys(type.getTemplateTypeMap().getUnfilledTemplateKeys());
      return builder.build();
    }

    return type;
  }
  private void maybeResolveTemplatedType(
      JSType paramType,
      JSType argType,
      Map<TemplateType, JSType> resolvedTypes) {
    if (paramType.isTemplateType()) {
    } else if (paramType.isUnionType()) {
      // @param {Array.<T>|NodeList|Arguments|{length:number}}
      UnionType unionType = paramType.toMaybeUnionType();
      for (JSType alernative : unionType.getAlternates()) {
        maybeResolveTemplatedType(alernative, argType, resolvedTypes);
      }
    } else if (paramType.isFunctionType()) {
      FunctionType paramFunctionType = paramType.toMaybeFunctionType();
      FunctionType argFunctionType = argType
          .restrictByNotNullOrUndefined()
          .collapseUnion()
          .toMaybeFunctionType();
      if (argFunctionType != null && argFunctionType.isSubtype(paramType)) {
        // infer from return type of the function type
        maybeResolveTemplatedType(
            paramFunctionType.getTypeOfThis(),
            argFunctionType.getTypeOfThis(), resolvedTypes);
        // infer from return type of the function type
        maybeResolveTemplatedType(
            paramFunctionType.getReturnType(),
            argFunctionType.getReturnType(), resolvedTypes);
        // infer from parameter types of the function type
        maybeResolveTemplateTypeFromNodes(
            paramFunctionType.getParameters(),
            argFunctionType.getParameters(), resolvedTypes);
      }
    } else if (paramType.isTemplatizedType()) {
      // @param {Array.<T>}
      ObjectType referencedParamType = paramType
          .toMaybeTemplatizedType()
          .getReferencedType();
      JSType argObjectType = argType
          .restrictByNotNullOrUndefined()
          .collapseUnion();

      if (argObjectType.isSubtype(referencedParamType)) {
        // If the argument type is a subtype of the parameter type, resolve any
        // template types amongst their templatized types.
        TemplateTypeMap paramTypeMap = paramType.getTemplateTypeMap();
        TemplateTypeMap argTypeMap = argObjectType.getTemplateTypeMap();
        for (TemplateType key : paramTypeMap.getTemplateKeys()) {
          maybeResolveTemplatedType(
              paramTypeMap.getTemplateType(key),
              argTypeMap.getTemplateType(key),
              resolvedTypes);
        }
      }
    }
  }
  private Map<TemplateType, JSType> inferTemplateTypesFromParameters(
      FunctionType fnType, Node call) {
    if (fnType.getTemplateTypeMap().getTemplateKeys().isEmpty()) {
      return Collections.emptyMap();
    }

    Map<TemplateType, JSType> resolvedTypes = Maps.newIdentityHashMap();

    Node callTarget = call.getFirstChild();
    if (NodeUtil.isGet(callTarget)) {
      Node obj = callTarget.getFirstChild();
    }

    if (call.hasMoreThanOneChild()) {
      maybeResolveTemplateTypeFromNodes(
          fnType.getParameters(),
          call.getChildAtIndex(1).siblings(),
          resolvedTypes);
    }
    return resolvedTypes;
  }
