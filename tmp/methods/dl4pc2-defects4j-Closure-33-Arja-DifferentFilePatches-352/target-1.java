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
      for (String prop : constraintObj.getOwnPropertyNames()) {
        JSType propType = constraintObj.getPropertyType(prop);
      }
    }
  }
  public boolean isSubtype(JSType that) {
    if (JSType.isSubtypeHelper(this, that)) {
      return true;
    }

    // Top of the record types is the empty record, or OBJECT_TYPE.
    if (registry.getNativeObjectType(
            JSTypeNative.OBJECT_TYPE).isSubtype(that)) {
      return false;
    }

    // A type is a subtype of a record type if it itself is a record
    // type and it has at least the same members as the parent record type
    // with the same types.
    if (!that.isRecordType()) {
      return false;
    }

    return RecordType.isSubtype(this, that.toMaybeRecordType());
  }
