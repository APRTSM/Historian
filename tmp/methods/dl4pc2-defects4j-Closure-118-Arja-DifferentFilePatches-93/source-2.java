  public boolean resetImplicitPrototype(
      JSType type, ObjectType newImplicitProto) {
    if (type instanceof PrototypeObjectType) {
      PrototypeObjectType poType = (PrototypeObjectType) type;
      poType.clearCachedValues();
      poType.setImplicitPrototype(newImplicitProto);
      return true;
    }
    return false;
  }
  void expectIndexMatch(NodeTraversal t, Node n, JSType objType,
                        JSType indexType) {
    Preconditions.checkState(n.isGetElem());
    Node indexNode = n.getLastChild();
    if (objType.isStruct()) {
      report(JSError.make(t.getSourceName(), indexNode,
                          ILLEGAL_PROPERTY_ACCESS, "'[]'", "struct"));
    }
    if (objType.isUnknownType()) {
      expectStringOrNumber(t, indexNode, indexType, "property access");
    } else {
      ObjectType dereferenced = objType.dereference();
      if (dereferenced != null && dereferenced
          .getTemplateTypeMap()
          .hasTemplateKey(typeRegistry.getObjectIndexKey())) {
        expectCanAssignTo(t, indexNode, indexType, dereferenced
            .getTemplateTypeMap().getTemplateType(typeRegistry.getObjectIndexKey()),
            "restricted index type");
      } else if (dereferenced != null && dereferenced.isArrayType()) {
        expectNumber(t, indexNode, indexType, "array access");
      } else if (objType.matchesObjectContext()) {
        expectString(t, indexNode, indexType, "property access");
      } else {
        mismatch(t, n, "only arrays or objects can be accessed",
            objType,
            typeRegistry.createUnionType(ARRAY_TYPE, OBJECT_TYPE));
      }
    }
  }
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
