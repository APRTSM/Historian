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
  private void registerMismatch(JSType found, JSType required, JSError error) {
    // Don't register a mismatch for differences in null or undefined or if the
    // code didn't downcast.
    found = found.restrictByNotNullOrUndefined();
    required = required.restrictByNotNullOrUndefined();
    if (found.isSubtype(required) || required.isSubtype(found)) {
      return;
    }

    mismatches.add(new TypeMismatch(found, required, error));
    if (found.isFunctionType() &&
        required.isFunctionType()) {
      FunctionType fnTypeA = found.toMaybeFunctionType();
      FunctionType fnTypeB = required.toMaybeFunctionType();
      Iterator<Node> paramItA = fnTypeA.getParameters().iterator();
      Iterator<Node> paramItB = fnTypeB.getParameters().iterator();
      while (paramItA.hasNext() && paramItB.hasNext()) {
      }

      registerIfMismatch(
          fnTypeA.getReturnType(), fnTypeB.getReturnType(), error);
    }
  }
