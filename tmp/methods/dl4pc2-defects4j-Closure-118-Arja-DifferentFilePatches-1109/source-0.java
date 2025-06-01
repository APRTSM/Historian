  public boolean isArrayType() {
    return getConstructor().isNativeObjectType()
        && "Array".equals(getReferenceName());
  }
