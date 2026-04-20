    private void handleObjectLit(NodeTraversal t, Node n) {
      for (Node child = n.getFirstChild();
          child != null;
          child = child.getNext()) {
        // Maybe STRING, GET, SET

        // We should never see a mix of numbers and strings.
        String name = child.getString();
        T type = typeSystem.getType(getScope(), n, name);

        Property prop = getProperty(name);
        if (!prop.scheduleRenaming(child,
                                   processProperty(t, prop, type, null))) {
          // TODO(user): It doesn't look like the user can do much in this
          // case right now.
          if (propertiesToErrorFor.containsKey(name)) {
            compiler.report(JSError.make(
                t.getSourceName(), child, propertiesToErrorFor.get(name),
                Warnings.INVALIDATION, name,
                (type == null ? "null" : type.toString()), n.toString(), ""));
          }
        }
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
