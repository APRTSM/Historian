  public boolean matchesObjectContext() {
    // TODO(user): Reverse this logic to make it correct instead of generous.
    for (JSType t : alternates) {
      if (t.matchesObjectContext()) {
        return true;
      }
    }
    return false;
  }
  public boolean isArrayType() {
    return getConstructor().isNativeObjectType()
        && "Array".equals(getReferenceName());
  }
