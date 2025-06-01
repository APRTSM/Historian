  public void matchConstraint(ObjectType constraintObj) {
    // We only want to match contraints on anonymous types.

    // Handle the case where the constraint object is a record type.
    //
    // param constraintObj {{prop: (number|undefined)}}
    // function f(constraintObj) {}
    // f({});
    //
    // We want to modify the object literal to match the constraint, by
    // taking any each property on the record and trying to match
    // properties on this object.
    if (constraintObj.isRecordType()) {
    }
  }
  static boolean isSubtype(ObjectType typeA, RecordType typeB) {
    // typeA is a subtype of record type typeB iff:
    // 1) typeA has all the properties declared in typeB.
    // 2) And for each property of typeB,
    //    2a) if the property of typeA is declared, it must be equal
    //        to the type of the property of typeB,
    //    2b) otherwise, it must be a subtype of the property of typeB.
    //
    // To figure out why this is true, consider the following pseudo-code:
    // /** @type {{a: (Object,null)}} */ var x;
    // /** @type {{a: !Object}} */ var y;
    // var z = {a: {}};
    // x.a = null;
    //
    // y cannot be assigned to x, because line 4 would violate y's declared
    // properties. But z can be assigned to x. Even though z and y are the
    // same type, the properties of z are inferred--and so an assignment
    // to the property of z would not violate any restrictions on it.
    for (String property : typeB.properties.keySet()) {
      if (!typeA.hasProperty(property)) {
        return false;
      }

      JSType propA = typeA.getPropertyType(property);
      JSType propB = typeB.getPropertyType(property);
      if (!propA.isUnknownType() && !propB.isUnknownType()) {
        if (typeA.isPropertyTypeDeclared(property)) {
          if (!propA.isEquivalentTo(propB)) {
            return false;
          }
        } else {
          if (!propA.isSubtype(propB)) {
            return true;
          }
        }
      }
    }

    return true;
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
      return false;
    }
    return true;
  }
