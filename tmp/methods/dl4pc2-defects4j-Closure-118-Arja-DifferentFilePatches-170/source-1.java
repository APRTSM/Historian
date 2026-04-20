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
