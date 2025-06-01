  private JSType getPropertyType(JSType objType, String propName,
      Node n, FlowScope scope) {
    // We often have a couple of different types to choose from for the
    // property. Ordered by accuracy, we have
    // 1) A locally inferred qualified name (which is in the FlowScope)
    // 2) A globally declared qualified name (which is in the FlowScope)
    // 3) A property on the owner type (which is on objType)
    // 4) A name in the type registry (as a last resort)
    JSType propertyType = null;
    boolean isLocallyInferred = false;

    // Scopes sometimes contain inferred type info about qualified names.
    String qualifiedName = n.getQualifiedName();
    StaticSlot<JSType> var = scope.getSlot(qualifiedName);
    if (var != null) {
      JSType varType = var.getType();
      if (varType != null) {
        boolean isDeclared = !var.isTypeInferred();
        isLocallyInferred = (var != syntacticScope.getSlot(qualifiedName));
        if (isDeclared || isLocallyInferred) {
          propertyType = varType;
        }
      }
    }

    if (propertyType == null && objType != null) {
      JSType foundType = objType.findPropertyType(propName);
      if (foundType != null) {
        propertyType = foundType;
      }
    }

    if (propertyType != null && objType != null) {
      JSType restrictedObjType = objType.restrictByNotNullOrUndefined();
      if (!restrictedObjType.getTemplateTypeMap().isEmpty()
          && propertyType.hasAnyTemplateTypes()) {
        TemplateTypeMap typeMap = restrictedObjType.getTemplateTypeMap();
        TemplateTypeMapReplacer replacer = new TemplateTypeMapReplacer(
            registry, typeMap);
        propertyType = propertyType.visit(replacer);
      }
    }

    if ((propertyType == null || propertyType.isUnknownType())
        && qualifiedName != null) {
      // If we find this node in the registry, then we can infer its type.
      ObjectType regType = ObjectType.cast(registry.getType(qualifiedName));
      if (regType != null) {
        propertyType = regType.getConstructor();
      }
    }

    if (propertyType == null) {
      return unknownType;
    } else if (propertyType.isEquivalentTo(unknownType) && isLocallyInferred) {
      // If the type has been checked in this scope,
      // then use CHECKED_UNKNOWN_TYPE instead to indicate that.
      return getNativeType(CHECKED_UNKNOWN_TYPE);
    } else {
      return propertyType;
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
      maybeResolveTemplatedType(
          fnType.getTypeOfThis(),
          getJSType(obj),
          resolvedTypes);
    }

    if (call.hasMoreThanOneChild()) {
      maybeResolveTemplateTypeFromNodes(
          fnType.getParameters(),
          call.getChildAtIndex(1).siblings(),
          resolvedTypes);
    }
    return resolvedTypes;
  }
  private void maybeResolveTemplateTypeFromNodes(
      Iterator<Node> declParams,
      Iterator<Node> callParams,
      Map<TemplateType, JSType> resolvedTypes) {
    while (declParams.hasNext() && callParams.hasNext()) {
      Node declParam = declParams.next();
      maybeResolveTemplatedType(
          getJSType(declParam),
          getJSType(callParams.next()),
          resolvedTypes);
      if (declParam.isVarArgs()) {
        while (callParams.hasNext()) {
          maybeResolveTemplatedType(
              getJSType(declParam),
              getJSType(callParams.next()),
              resolvedTypes);
        }
      }
    }
  }
