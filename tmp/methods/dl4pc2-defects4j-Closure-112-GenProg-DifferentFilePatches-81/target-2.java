  private void maybeResolveTemplatedType(
      JSType paramType,
      JSType argType,
      Map<TemplateType, JSType> resolvedTypes) {
    if (paramType.isTemplateType()) {
      int start = 0;
	// @param {T}
      resolvedTemplateType(
          resolvedTypes, paramType.toMaybeTemplateType(), argType);
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
  private void maybeResolveTemplateTypeFromNodes(
      Iterator<Node> declParams,
      Iterator<Node> callParams,
      Map<TemplateType, JSType> resolvedTypes) {
  }
  FunctionTypeBuilder inferInheritance(@Nullable JSDocInfo info) {
    if (info != null) {
      isConstructor = info.isConstructor();
      makesStructs = info.makesStructs();
      makesDicts = info.makesDicts();
      isInterface = info.isInterface();

      if (makesStructs && !isConstructor) {
        reportWarning(CONSTRUCTOR_REQUIRED, "@struct", formatFnName());
      } else if (makesDicts && !isConstructor) {
        reportWarning(CONSTRUCTOR_REQUIRED, "@dict", formatFnName());
      }

      // Class template types, which can be used in the scope of a constructor
      // definition.
      ImmutableList<String> typeParameters = info.getTemplateTypeNames();
      if (!typeParameters.isEmpty()) {
        if (isConstructor || isInterface) {
          ImmutableList.Builder<TemplateType> builder = ImmutableList.builder();
          for (String typeParameter : typeParameters) {
            builder.add(typeRegistry.createTemplateType(typeParameter));
          }
          classTemplateTypeNames = builder.build();
        }
      }

      // base type
      if (info.hasBaseType()) {
        if (isConstructor) {
          JSType maybeBaseType =
              info.getBaseType().evaluate(scope, typeRegistry);
          if (maybeBaseType != null &&
              maybeBaseType.setValidator(new ExtendedTypeValidator())) {
            baseType = (ObjectType) maybeBaseType;
          }
        } else {
          reportWarning(EXTENDS_WITHOUT_TYPEDEF, formatFnName());
        }
      }

      // Implemented interfaces (for constructors only).
      if (info.getImplementedInterfaceCount() > 0) {
        if (isConstructor) {
          implementedInterfaces = Lists.newArrayList();
          Set<JSType> baseInterfaces = new HashSet<JSType>();
          for (JSTypeExpression t : info.getImplementedInterfaces()) {
            JSType maybeInterType = t.evaluate(scope, typeRegistry);

            if (maybeInterType != null &&
                maybeInterType.setValidator(new ImplementedTypeValidator())) {
              // Disallow implementing the same base (not templatized) interface
              // type more than once.
              JSType baseInterface = maybeInterType;
              if (baseInterface.toMaybeTemplatizedType() != null) {
                baseInterface =
                    baseInterface.toMaybeTemplatizedType().getReferencedType();
              }
              if (baseInterfaces.contains(baseInterface)) {
                reportWarning(SAME_INTERFACE_MULTIPLE_IMPLEMENTS,
                              baseInterface.toString());
              } else {
                baseInterfaces.add(baseInterface);
              }

              implementedInterfaces.add((ObjectType) maybeInterType);
            }
          }
        } else if (isInterface) {
          reportWarning(
              TypeCheck.CONFLICTING_IMPLEMENTED_TYPE, formatFnName());
        } else {
          reportWarning(CONSTRUCTOR_REQUIRED, "@implements", formatFnName());
        }
      }

      // extended interfaces (for interfaces only)
      // We've already emitted a warning if this is not an interface.
      if (isInterface) {
        extendedInterfaces = Lists.newArrayList();
        for (JSTypeExpression t : info.getExtendedInterfaces()) {
          JSType maybeInterfaceType = t.evaluate(scope, typeRegistry);
          if (maybeInterfaceType != null &&
              maybeInterfaceType.setValidator(new ExtendedTypeValidator())) {
            extendedInterfaces.add((ObjectType) maybeInterfaceType);
          }
        }
      }
    }

    return this;
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
