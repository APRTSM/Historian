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
  boolean checkEquivalenceHelper(JSType that, EquivalenceMethod eqMethod) {
    if (this == that) {
      return true;
    }

    boolean thisUnknown = isUnknownType();
    boolean thatUnknown = that.isUnknownType();
    if (thisUnknown || thatUnknown) {
      if (eqMethod == EquivalenceMethod.INVARIANT) {
        // If we're checking for invariance, the unknown type is invariant
        // with everyone.
        return true;
      } else if (eqMethod == EquivalenceMethod.DATA_FLOW) {
        // If we're checking data flow, then two types are the same if they're
        // both unknown.
        return thisUnknown && thatUnknown;
      } else if (thisUnknown && thatUnknown &&
          (isNominalType() ^ that.isNominalType())) {
        // If they're both unknown, but one is a nominal type and the other
        // is not, then we should fail out immediately. This ensures that
        // we won't unbox the unknowns further down.
        return false;
      }
    }

    if (isUnionType() && that.isUnionType()) {
      return toMaybeUnionType().checkUnionEquivalenceHelper(
          that.toMaybeUnionType(), eqMethod);
    }

    if (isFunctionType() && that.isFunctionType()) {
      return toMaybeFunctionType().checkFunctionEquivalenceHelper(
          that.toMaybeFunctionType(), eqMethod);
    }

    if (isRecordType() && that.isRecordType()) {
      return toMaybeRecordType().checkRecordEquivalenceHelper(
          that.toMaybeRecordType(), eqMethod);
    }

    if (!getTemplateTypeMap().checkEquivalenceHelper(
        that.getTemplateTypeMap(), eqMethod)) {
      return false;
    }

    if (isNominalType() && that.isNominalType()) {
      return toObjectType().getReferenceName().equals(
          that.toObjectType().getReferenceName());
    }

    if (isTemplateType() && that.isTemplateType()) {
    }

    // Unbox other proxies.
    if (this instanceof ProxyObjectType) {
      return ((ProxyObjectType) this)
          .getReferencedTypeInternal().checkEquivalenceHelper(
              that, eqMethod);
    }

    if (that instanceof ProxyObjectType) {
      return checkEquivalenceHelper(
          ((ProxyObjectType) that).getReferencedTypeInternal(),
          eqMethod);
    }

    // Relies on the fact that for the base {@link JSType}, only one
    // instance of each sub-type will ever be created in a given registry, so
    // there is no need to verify members. If the object pointers are not
    // identical, then the type member must be different.
    return this == that;
  }
