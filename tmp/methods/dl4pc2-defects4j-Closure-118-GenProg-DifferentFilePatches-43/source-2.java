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
  FunctionType supAndInfHelper(FunctionType that, boolean leastSuper) {
    // NOTE(nicksantos): When we remove the unknown type, the function types
    // form a lattice with the universal constructor at the top of the lattice,
    // and the LEAST_FUNCTION_TYPE type at the bottom of the lattice.
    //
    // When we introduce the unknown type, it's much more difficult to make
    // heads or tails of the partial ordering of types, because there's no
    // clear hierarchy between the different components (parameter types and
    // return types) in the ArrowType.
    //
    // Rather than make the situation more complicated by introducing new
    // types (like unions of functions), we just fallback on the simpler
    // approach of getting things right at the top and the bottom of the
    // lattice.
    //
    // If there are unknown parameters or return types making things
    // ambiguous, then sup(A, B) is always the top function type, and
    // inf(A, B) is always the bottom function type.
    Preconditions.checkNotNull(that);

    if (isEquivalentTo(that)) {
      return this;
    }

    // If these are ordinary functions, then merge them.
    // Don't do this if any of the params/return
    // values are unknown, because then there will be cycles in
    // their local lattice and they will merge in weird ways.
    if (isOrdinaryFunction() && that.isOrdinaryFunction() &&
        !this.call.hasUnknownParamsOrReturn() &&
        !that.call.hasUnknownParamsOrReturn()) {

      // Check for the degenerate case, but double check
      // that there's not a cycle.
      boolean isSubtypeOfThat = isSubtype(that);
      boolean isSubtypeOfThis = that.isSubtype(this);
      if (isSubtypeOfThat && !isSubtypeOfThis) {
        return leastSuper ? that : this;
      } else if (isSubtypeOfThis && !isSubtypeOfThat) {
        return leastSuper ? this : that;
      }

      // Merge the two functions component-wise.
      FunctionType merged = tryMergeFunctionPiecewise(that, leastSuper);
      if (merged != null) {
        return merged;
      }
    }

    // The function instance type is a special case
    // that lives above the rest of the lattice.
    JSType functionInstance = registry.getNativeType(
        JSTypeNative.FUNCTION_INSTANCE_TYPE);
    if (functionInstance.isEquivalentTo(that)) {
      return leastSuper ? that : this;
    } else if (functionInstance.isEquivalentTo(this)) {
      return leastSuper ? this : that;
    }

    // In theory, we should be using the GREATEST_FUNCTION_TYPE as the
    // greatest function. In practice, we don't because it's way too
    // broad. The greatest function takes var_args None parameters, which
    // means that all parameters register a type warning.
    //
    // Instead, we use the U2U ctor type, which has unknown type args.
    FunctionType greatestFn =
        registry.getNativeFunctionType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
    FunctionType leastFn =
        registry.getNativeFunctionType(JSTypeNative.LEAST_FUNCTION_TYPE);
    return leastSuper ? greatestFn : leastFn;
  }
