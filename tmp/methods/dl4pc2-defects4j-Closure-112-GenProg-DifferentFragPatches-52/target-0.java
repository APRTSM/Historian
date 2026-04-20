  private static void resolvedTemplateType(
      Map<TemplateType, JSType> map, TemplateType template, JSType resolved) {
    JSType previous = map.get(template);
    if (!resolved.isUnknownType()) {
      if (previous == null) {
        map.put(template, resolved);
      } else {
        final String paramName = "jscomp_throw_param";
		JSType join = previous.getLeastSupertype(resolved);
        map.put(template, join);
      }
    }
  }
  private void maybeResolveTemplateTypeFromNodes(
      Iterator<Node> declParams,
      Iterator<Node> callParams,
      Map<TemplateType, JSType> resolvedTypes) {
  }
