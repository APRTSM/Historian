  boolean defineProperty(String name, JSType type,
      boolean inferred, Node propertyNode) {
    if ("prototype".equals(name)) {
      ObjectType objType = type.toObjectType();
      if (objType != null) {
        if (prototypeSlot != null &&
            objType.isEquivalentTo(prototypeSlot.getType())) {
          return true;
        }
        setPrototypeBasedOn(objType, propertyNode);
        return true;
      } else {
        return false;
      }
    }
    return super.defineProperty(name, type, inferred, propertyNode);
  }
