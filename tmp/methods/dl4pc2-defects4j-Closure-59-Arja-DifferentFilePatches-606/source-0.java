  private boolean isVoidOrUnknown(JSType returnType) {
    final JSType voidType =
        compiler.getTypeRegistry().getNativeType(JSTypeNative.VOID_TYPE);
    return voidType.isSubtype(returnType);
  }
