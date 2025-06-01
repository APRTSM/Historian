  private JSType explicitReturnExpected(Node scope) {
    JSType scopeType = scope.getJSType();

    if (!(scopeType instanceof FunctionType)) {
      return null;
    }

    if (isEmptyFunction(scope)) {
      return null;
    }

    JSType returnType = ((FunctionType) scopeType).getReturnType();

    if (returnType == null) {
      return null;
    }

    if (!isVoidOrUnknown(returnType)) {
      return returnType;
    }

    return null;
  }
