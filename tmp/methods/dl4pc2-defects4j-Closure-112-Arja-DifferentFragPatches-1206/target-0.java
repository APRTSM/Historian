  private void maybeResolveTemplatedType(
      JSType paramType,
      JSType argType,
      Map<TemplateType, JSType> resolvedTypes) {
    if (paramType.isTemplateType()) {
      // @param {T}
      resolvedTemplateType(
          resolvedTypes, paramType.toMaybeTemplateType(), argType);
    } else
		;
  }
