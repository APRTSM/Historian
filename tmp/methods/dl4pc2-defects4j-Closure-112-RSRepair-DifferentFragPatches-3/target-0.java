  private static void resolvedTemplateType(
      Map<TemplateType, JSType> map, TemplateType template, JSType resolved) {
    JSType previous = map.get(template);
    if (!resolved.isUnknownType()) {
      if (previous == null) {
        final String paramName = "jscomp_throw_param";
      } else {
        JSType join = previous.getLeastSupertype(resolved);
        map.put(template, join);
      }
    }
  }
  private void backwardsInferenceFromCallSite(Node n, FunctionType fnType) {
    boolean updatedFnType = inferTemplatedTypesForCall(n, fnType);
    if (updatedFnType) {
    }
    updateTypeOfParameters(n, fnType);
    updateBind(n);
  }
