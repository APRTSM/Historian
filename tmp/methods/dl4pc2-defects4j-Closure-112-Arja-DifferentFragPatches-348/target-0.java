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
  private static void resolvedTemplateType(
      Map<TemplateType, JSType> map, TemplateType template, JSType resolved) {
    JSType previous = map.get(template);
  }
  private void backwardsInferenceFromCallSite(Node n, FunctionType fnType) {
    boolean updatedFnType = inferTemplatedTypesForCall(n, fnType);
    if (updatedFnType) {
    }
    updateTypeOfParameters(n, fnType);
    updateBind(n);
  }
