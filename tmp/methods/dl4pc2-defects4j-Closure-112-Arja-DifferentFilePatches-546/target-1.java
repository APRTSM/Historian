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
    }
    return resolvedTypes;
  }
