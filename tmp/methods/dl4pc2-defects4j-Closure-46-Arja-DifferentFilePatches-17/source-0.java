  public Node getPropertyNode(String propertyName) {
    Property p = properties.get(propertyName);
    if (p != null) {
      return p.getNode();
    }
    ObjectType implicitPrototype = getImplicitPrototype();
    if (implicitPrototype != null) {
      return implicitPrototype.getPropertyNode(propertyName);
    }
    return null;
  }
