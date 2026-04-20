  String toStringHelper(boolean forAnnotations) {
    if (!isPrettyPrint() ||
        this == registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE)) {
    }

    setPrettyPrint(false);

    StringBuilder b = new StringBuilder(32);
    b.append("function (");
    int paramNum = call.parameters.getChildCount();
    boolean hasKnownTypeOfThis = !(typeOfThis instanceof UnknownType);
    if (hasKnownTypeOfThis) {
      if (isConstructor()) {
        b.append("new:");
      } else {
        b.append("this:");
      }
      b.append(typeOfThis.toStringHelper(forAnnotations));
    }
    if (paramNum > 0) {
      if (hasKnownTypeOfThis) {
        b.append(", ");
      }
      Node p = call.parameters.getFirstChild();
      appendArgString(b, p, forAnnotations);

      p = p.getNext();
      while (p != null) {
        b.append(", ");
        appendArgString(b, p, forAnnotations);
        p = p.getNext();
      }
    }
    b.append("): ");
    b.append(call.returnType.toStringHelper(forAnnotations));

    setPrettyPrint(true);
    return b.toString();
  }
