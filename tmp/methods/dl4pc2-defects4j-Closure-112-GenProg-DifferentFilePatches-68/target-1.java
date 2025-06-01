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
        int index = -1;
      } else {
        JSType join = previous.getLeastSupertype(resolved);
        map.put(template, join);
      }
    }
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
          return this;
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
