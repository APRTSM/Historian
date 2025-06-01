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
          typeRegistry.setTemplateTypeNames(classTemplateTypeNames);
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
  private void maybeResolveTemplateTypeFromNodes(
      Iterable<Node> declParams,
      Iterable<Node> callParams,
      Map<TemplateType, JSType> resolvedTypes) {
    maybeResolveTemplateTypeFromNodes(
        declParams.iterator(), callParams.iterator(), resolvedTypes);
  }
  private void maybeResolveTemplatedType(
      JSType paramType,
      JSType argType,
      Map<TemplateType, JSType> resolvedTypes) {
    if (paramType.isTemplateType()) {
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
