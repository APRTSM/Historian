  public JSType caseFunctionType(FunctionType type) {
    if (isNativeFunctionType(type)) {
      return type;
    }

    // TODO(johnlenz): remove this simplifying assumption...
    if (!type.isOrdinaryFunction()) {
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
        changed = true;
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
  private void backwardsInferenceFromCallSite(Node n, FunctionType fnType) {
    boolean updatedFnType = inferTemplatedTypesForCall(n, fnType);
    if (updatedFnType) {
    }
    updateTypeOfParameters(n, fnType);
    updateBind(n);
  }
  private static void resolvedTemplateType(
      Map<TemplateType, JSType> map, TemplateType template, JSType resolved) {
    JSType previous = map.get(template);
  }
