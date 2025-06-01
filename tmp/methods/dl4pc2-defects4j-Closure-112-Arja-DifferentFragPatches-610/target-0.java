  private void maybeResolveTemplateTypeFromNodes(
      Iterator<Node> declParams,
      Iterator<Node> callParams,
      Map<TemplateType, JSType> resolvedTypes) {
  }
    public JSType caseTemplateType(TemplateType type) {
      JSType replacement = replacements.get(type);
      return replacement != null ?
          replacement : registry.getNativeType(UNKNOWN_TYPE);
    }
