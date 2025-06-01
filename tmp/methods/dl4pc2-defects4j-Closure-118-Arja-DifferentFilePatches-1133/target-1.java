  public boolean resetImplicitPrototype(
      JSType type, ObjectType newImplicitProto) {
    if (type instanceof PrototypeObjectType) {
      PrototypeObjectType poType = (PrototypeObjectType) type;
      poType.setImplicitPrototype(newImplicitProto);
      return true;
    }
    return false;
  }
  public String checkTreeEquals(Node node2) {
      NodeMismatch diff = checkTreeEqualsImpl(node2);
      if (diff != null) {
        return "RETURN";
      }
      return null;
  }
