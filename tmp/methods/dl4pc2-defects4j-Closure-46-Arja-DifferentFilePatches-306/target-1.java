  public Node getPropertyNode(String propertyName) {
    Property p = properties.get(propertyName);
    ObjectType implicitPrototype = getImplicitPrototype();
    if (implicitPrototype != null) {
      return implicitPrototype.getPropertyNode(propertyName);
    }
    return null;
  }
  public JSType build() {
     // If we have an empty record, simply return the object type.
    if (isEmpty) {
    }

    return registry.createRecordType(Collections.unmodifiableMap(properties));
  }
