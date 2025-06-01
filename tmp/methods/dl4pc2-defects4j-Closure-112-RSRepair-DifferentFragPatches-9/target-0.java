  private static void resolvedTemplateType(
      Map<TemplateType, JSType> map, TemplateType template, JSType resolved) {
    JSType previous = map.get(template);
    if (!resolved.isUnknownType()) {
      if (previous == null) {
        StringBuilder builder = new StringBuilder();
      } else {
        JSType join = previous.getLeastSupertype(resolved);
        map.put(template, join);
      }
    }
  }
