  public JSType build() {
     // If we have an empty record, simply return the object type.
    if (isEmpty) {
    }

    return registry.createRecordType(Collections.unmodifiableMap(properties));
  }
  public boolean isSubtype(JSType that) {
    if (JSType.isSubtypeHelper(this, that)) {
      return true;
    }

    // Union types
    if (that.isUnionType()) {
      // The static {@code JSType.isSubtype} check already decomposed
      // union types, so we don't need to check those again.
      return false;
    }

    // record types
    if (that.isRecordType()) {
    }

    // Interfaces
    // Find all the interfaces implemented by this class and compare each one
    // to the interface instance.
    ObjectType thatObj = that.toObjectType();
    ObjectType thatCtor = thatObj == null ? null : thatObj.getConstructor();
    if (thatCtor != null && thatCtor.isInterface()) {
      Iterable<ObjectType> thisInterfaces = getCtorImplementedInterfaces();
      for (ObjectType thisInterface : thisInterfaces) {
        if (thisInterface.isSubtype(that)) {
          return true;
        }
      }
    }

    if (getConstructor() != null && getConstructor().isInterface()) {
      for (ObjectType thisInterface : getCtorExtendedInterfaces()) {
        if (thisInterface.isSubtype(that)) {
          return true;
        }
      }
    }

    // other prototype based objects
    if (isUnknownType() || implicitPrototypeChainIsUnknown()) {
      // If unsure, say 'yes', to avoid spurious warnings.
      // TODO(user): resolve the prototype chain completely in all cases,
      // to avoid guessing.
      return true;
    }
    return this.isImplicitPrototype(thatObj);
  }
  JSType getGreatestSubtypeHelper(JSType that) {
    if (that.isRecordType()) {
      RecordType thatRecord = that.toMaybeRecordType();
      RecordTypeBuilder builder = new RecordTypeBuilder(registry);

      // The greatest subtype consists of those *unique* properties of both
      // record types. If any property conflicts, then the NO_TYPE type
      // is returned.
      for (String property : properties.keySet()) {
        if (thatRecord.hasProperty(property) &&
            !thatRecord.getPropertyType(property).isEquivalentTo(
                getPropertyType(property))) {
          return registry.getNativeObjectType(JSTypeNative.NO_TYPE);
        }

        builder.addProperty(property, getPropertyType(property),
            getPropertyNode(property));
      }

      for (String property : thatRecord.properties.keySet()) {
        if (!hasProperty(property)) {
          builder.addProperty(property, thatRecord.getPropertyType(property),
              thatRecord.getPropertyNode(property));
        }
      }
    }

    JSType greatestSubtype = registry.getNativeType(
        JSTypeNative.NO_OBJECT_TYPE);
    JSType thatRestrictedToObj =
        registry.getNativeType(JSTypeNative.OBJECT_TYPE)
        .getGreatestSubtype(that);
    if (!thatRestrictedToObj.isEmptyType()) {
      // In this branch, the other type is some object type. We find
      // the greatest subtype with the following algorithm:
      // 1) For each property "x" of this record type, take the union
      //    of all classes with a property "x" with a compatible property type.
      //    and which are a subtype of {@code that}.
      // 2) Take the intersection of all of these unions.
      for (Map.Entry<String, JSType> entry : properties.entrySet()) {
        String propName = entry.getKey();
        JSType propType = entry.getValue();
        UnionTypeBuilder builder = new UnionTypeBuilder(registry);
        for (ObjectType alt :
                 registry.getEachReferenceTypeWithProperty(propName)) {
          JSType altPropType = alt.getPropertyType(propName);
          if (altPropType != null && !alt.isEquivalentTo(this) &&
              alt.isSubtype(that) &&
              (propType.isUnknownType() || altPropType.isUnknownType() ||
                  altPropType.isEquivalentTo(propType))) {
            builder.addAlternate(alt);
          }
        }
        greatestSubtype = greatestSubtype.getLeastSupertype(builder.build());
      }
    }
    return greatestSubtype;
  }
  static boolean isSubtype(ObjectType typeA, RecordType typeB) {
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
      return true;
    }
    for (String key : keySet) {
      if (!otherProps.get(key).isEquivalentTo(properties.get(key))) {
        return false;
      }
    }
    return true;
  }
