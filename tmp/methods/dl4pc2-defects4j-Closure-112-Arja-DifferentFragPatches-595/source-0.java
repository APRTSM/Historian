  private void backwardsInferenceFromCallSite(Node n, FunctionType fnType) {
    boolean updatedFnType = inferTemplatedTypesForCall(n, fnType);
    if (updatedFnType) {
      fnType = n.getFirstChild().getJSType().toMaybeFunctionType();
    }
    updateTypeOfParameters(n, fnType);
    updateBind(n);
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
