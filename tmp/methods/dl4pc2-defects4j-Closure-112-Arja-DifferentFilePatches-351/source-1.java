  public boolean hasTemplateKey(TemplateType templateKey) {
    // Note: match by identity, not equality
    for (TemplateType entry : templateKeys) {
      if (entry == templateKey) {
        return true;
      }
    }
    return false;
  }
  private void maybeResolveTemplateTypeFromNodes(
      Iterable<Node> declParams,
      Iterable<Node> callParams,
      Map<TemplateType, JSType> resolvedTypes) {
    maybeResolveTemplateTypeFromNodes(
        declParams.iterator(), callParams.iterator(), resolvedTypes);
  }
