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
    private boolean isQualifiedNameInferred(
        String qName, Node n, JSDocInfo info,
        Node rhsValue, JSType valueType) {
      if (valueType == null) {
        return true;
      }

      // Prototypes of constructors and interfaces are always declared.
      if (qName != null && qName.endsWith(".prototype")) {
        String className = qName.substring(0, qName.lastIndexOf(".prototype"));
        Var slot = scope.getSlot(className);
        JSType classType = slot == null ? null : slot.getType();
        if (classType != null
            && (classType.isConstructor() || classType.isInterface())) {
          return false;
        }
      }

      boolean inferred = true;
      if (info != null) {
        inferred = !(info.hasType()
            || info.hasEnumParameterType()
            || (isConstantSymbol(info, n) && valueType != null
                && !valueType.isUnknownType())
            || FunctionTypeBuilder.isFunctionTypeDeclaration(info));
      }

      if (inferred && rhsValue != null && rhsValue.isFunction()) {
        if (info != null) {
          return false;
        } else if (!scope.isDeclared(qName, false) &&
            n.isUnscopedQualifiedName()) {

          // Check if this is in a conditional block.
          // Functions assigned in conditional blocks are inferred.
          for (Node current = n.getParent();
               !(current.isScript() || current.isFunction());
               current = current.getParent()) {
            if (NodeUtil.isControlStructure(current)) {
              return true;
            }
          }

          // Check if this is assigned in an inner scope.
          // Functions assigned in inner scopes are inferred.
          AstFunctionContents contents =
              getFunctionAnalysisResults(scope.getRootNode());
          if (contents == null ||
              !contents.getEscapedQualifiedNames().contains(qName)) {
            return false;
          }
        }
      }
      return inferred;
    }
    public void visit(NodeTraversal t, Node n, Node parent) {
      if (n.isGetProp()) {
        handleGetProp(t, n);
      } else if (n.isObjectLit()) {
        handleObjectLit(t, n);
      }
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
