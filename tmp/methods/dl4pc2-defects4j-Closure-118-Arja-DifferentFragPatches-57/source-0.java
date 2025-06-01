    public void visit(NodeTraversal t, Node n, Node parent) {
      if (n.isGetProp()) {
        handleGetProp(t, n);
      } else if (n.isObjectLit()) {
        handleObjectLit(t, n);
      }
    }
    @Override public ObjectType getTypeWithProperty(String field, JSType type) {
      if (type == null) {
        return null;
      }

      if (type.isEnumElementType()) {
        return getTypeWithProperty(
            field, type.toMaybeEnumElementType().getPrimitiveType());
      }

      if (!(type instanceof ObjectType)) {
        if (type.autoboxesTo() != null) {
          type = type.autoboxesTo();
        } else {
          return null;
        }
      }

      // Ignore the prototype itself at all times.
      if ("prototype".equals(field)) {
        return null;
      }

      // We look up the prototype chain to find the highest place (if any) that
      // this appears.  This will make references to overridden properties look
      // like references to the initial property, so they are renamed alike.
      ObjectType foundType = null;
      ObjectType objType = ObjectType.cast(type);
      if (objType != null && objType.getConstructor() != null
          && objType.getConstructor().isInterface()) {
        ObjectType topInterface = FunctionType.getTopDefiningInterface(
            objType, field);
        if (topInterface != null && topInterface.getConstructor() != null) {
          foundType = topInterface.getConstructor().getPrototype();
        }
      } else {
        while (objType != null && objType.getImplicitPrototype() != objType) {
          if (objType.hasOwnProperty(field)) {
            foundType = objType;
          }
          objType = objType.getImplicitPrototype();
        }
      }

      // If the property does not exist on the referenced type but the original
      // type is an object type, see if any subtype has the property.
      if (foundType == null) {
        ObjectType maybeType = ObjectType.cast(
            registry.getGreatestSubtypeWithProperty(type, field));
        // getGreatestSubtypeWithProperty does not guarantee that the property
        // is defined on the returned type, it just indicates that it might be,
        // so we have to double check.
        if (maybeType != null && maybeType.hasOwnProperty(field)) {
          foundType = maybeType;
        }
      }
      return foundType;
    }
