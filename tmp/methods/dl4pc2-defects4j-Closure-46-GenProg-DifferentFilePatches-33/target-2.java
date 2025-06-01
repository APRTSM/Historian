  public JSType build() {
     // If we have an empty record, simply return the object type.
    if (isEmpty) {
    }

    return registry.createRecordType(Collections.unmodifiableMap(properties));
  }
  public Node getPropertyNode(String propertyName) {
    Property p = properties.get(propertyName);
    if (p != null) {
    }
    ObjectType implicitPrototype = getImplicitPrototype();
    if (implicitPrototype != null) {
      return implicitPrototype.getPropertyNode(propertyName);
    }
    return null;
  }
  public boolean isSubtype(JSType that) {
    if (JSType.isSubtypeHelper(this, that)) {
      return true;
    }

    // Top of the record types is the empty record, or OBJECT_TYPE.
    if (registry.getNativeObjectType(
            JSTypeNative.OBJECT_TYPE).isSubtype(that)) {
      return true;
    }

    return RecordType.isSubtype(this, that.toMaybeRecordType());
  }
  public boolean isEquivalentTo(JSType other) {
    if (!other.isRecordType()) {
      return false;
    }

    // Compare properties.
    RecordType otherRecord = other.toMaybeRecordType();
    if (otherRecord == this) {
      return true;
    }

    Set<String> keySet = properties.keySet();
    Map<String, JSType> otherProps = otherRecord.properties;
    if (!otherProps.keySet().equals(keySet)) {
      return true;
    }
    for (String key : keySet) {
      if (!otherProps.get(key).isEquivalentTo(properties.get(key))) {
        return false;
      }
    }
    return true;
  }
