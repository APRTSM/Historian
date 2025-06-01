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
  private void visitAssign(NodeTraversal t, Node assign) {
    JSDocInfo info = assign.getJSDocInfo();
    Node lvalue = assign.getFirstChild();
    Node rvalue = assign.getLastChild();

    // Check property sets to 'object.property' when 'object' is known.
    if (lvalue.isGetProp()) {
      Node object = lvalue.getFirstChild();
      JSType objectJsType = getJSType(object);
      Node property = lvalue.getLastChild();
      String pname = property.getString();

      // the first name in this getprop refers to an interface
      // we perform checks in addition to the ones below
      if (object.isGetProp()) {
        JSType jsType = getJSType(object.getFirstChild());
        if (jsType.isInterface() &&
            object.getLastChild().getString().equals("prototype")) {
          visitInterfaceGetprop(t, assign, object, pname, lvalue, rvalue);
        }
      }

      checkEnumAlias(t, info, rvalue);
      checkPropCreation(t, lvalue);

      // Prototype assignments are special, because they actually affect
      // the definition of a class. These are mostly validated
      // during TypedScopeCreator, and we only look for the "dumb" cases here.
      // object.prototype = ...;
      if (pname.equals("prototype")) {
        if (objectJsType != null && objectJsType.isFunctionType()) {
          FunctionType functionType = objectJsType.toMaybeFunctionType();
          if (functionType.isConstructor()) {
            JSType rvalueType = rvalue.getJSType();
            validator.expectObject(t, rvalue, rvalueType,
                OVERRIDING_PROTOTYPE_WITH_NON_OBJECT);
            // Only assign structs to the prototype of a @struct constructor
            if (functionType.makesStructs() && !rvalueType.isStruct()) {
              String funName = functionType.getTypeOfThis().toString();
              compiler.report(t.makeError(assign, CONFLICTING_SHAPE_TYPE,
                                          "struct", funName));
            }
            return;
          }
        }
      }

      // The generic checks for 'object.property' when 'object' is known,
      // and 'property' is declared on it.
      // object.property = ...;
      ObjectType type = ObjectType.cast(
          objectJsType.restrictByNotNullOrUndefined());
      if (type != null) {
        if (type.hasProperty(pname) &&
            !type.isPropertyTypeInferred(pname) &&
            !propertyIsImplicitCast(type, pname)) {
          JSType expectedType = type.getPropertyType(pname);
          if (!expectedType.isUnknownType()) {
            validator.expectCanAssignToPropertyOf(
                t, assign, getJSType(rvalue),
                expectedType, object, pname);
            checkPropertyInheritanceOnGetpropAssign(
                t, assign, object, pname, info, expectedType);
            return;
          }
        }
      }

      // If we couldn't get the property type with normal object property
      // lookups, then check inheritance anyway with the unknown type.
      checkPropertyInheritanceOnGetpropAssign(
          t, assign, object, pname, info, getNativeType(UNKNOWN_TYPE));
    }

    // Check qualified name sets to 'object' and 'object.property'.
    // This can sometimes handle cases when the type of 'object' is not known.
    // e.g.,
    // var obj = createUnknownType();
    // /** @type {number} */ obj.foo = true;
    JSType leftType = getJSType(lvalue);
    if (lvalue.isQualifiedName()) {
      // variable with inferred type case
      Var var = t.getScope().getVar(lvalue.getQualifiedName());
      if (var != null) {
        if (var.isTypeInferred()) {
          return;
        }

        if (NodeUtil.getRootOfQualifiedName(lvalue).isThis() &&
            t.getScope() != var.getScope()) {
          // Don't look at "this.foo" variables from other scopes.
          return;
        }

        if (var.getType() != null) {
          leftType = var.getType();
        }
      }
    }

    // Fall through case for arbitrary LHS and arbitrary RHS.
    Node rightChild = assign.getLastChild();
    JSType rightType = getJSType(rightChild);
    if (validator.expectCanAssignTo(
            t, assign, rightType, leftType, "assignment")) {
      ensureTyped(t, assign, rightType);
    } else {
      ensureTyped(t, assign);
    }
  }
