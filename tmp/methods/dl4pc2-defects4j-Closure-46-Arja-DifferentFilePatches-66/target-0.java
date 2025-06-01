  public Node getPropertyNode(String propertyName) {
    Property p = properties.get(propertyName);
    ObjectType implicitPrototype = getImplicitPrototype();
    if (implicitPrototype != null) {
      return implicitPrototype.getPropertyNode(propertyName);
    }
    return null;
  }
