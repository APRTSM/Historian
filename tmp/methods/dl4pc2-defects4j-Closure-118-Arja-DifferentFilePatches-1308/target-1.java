  public boolean resetImplicitPrototype(
      JSType type, ObjectType newImplicitProto) {
    if (type instanceof PrototypeObjectType) {
      PrototypeObjectType poType = (PrototypeObjectType) type;
      poType.setImplicitPrototype(newImplicitProto);
      return true;
    }
    return false;
  }
    private void handleObjectLit(NodeTraversal t, Node n) {
    }
