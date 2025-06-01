  FunctionTypeBuilder inferReturnType(@Nullable JSDocInfo info) {
    returnType = info != null && info.hasReturnType() ?
        info.getReturnType().evaluate(scope, typeRegistry) : null;
    if (templateTypeName != null &&
        returnType != null &&
        returnType.restrictByNotNullOrUndefined().isTemplateType()) {
      reportError(TEMPLATE_TYPE_EXPECTED, fnName);
    }
    return this;
  }
  FunctionType buildAndRegister() {
    if (returnType == null) {
      returnType = typeRegistry.getNativeType(UNKNOWN_TYPE);
    }

    if (parametersNode == null) {
      throw new IllegalStateException(
          "All Function types must have params and a return type");
    }

    FunctionType fnType;
    if (isConstructor) {
      fnType = getOrCreateConstructor();
    } else if (isInterface) {
      fnType = typeRegistry.createInterfaceType(fnName, sourceNode);
      if (scope.isGlobal() && !fnName.isEmpty()) {
        typeRegistry.declareType(fnName, fnType.getInstanceType());
      }
      maybeSetBaseType(fnType);
    } else {
      fnType = new FunctionBuilder(typeRegistry)
          .withName(fnName)
          .withSourceNode(sourceNode)
          .withParamsNode(parametersNode)
          .withReturnType(returnType, returnTypeInferred)
          .withTypeOfThis(thisType)
          .withTemplateName(templateTypeName)
          .build();
      maybeSetBaseType(fnType);
    }

    if (implementedInterfaces != null) {
      fnType.setImplementedInterfaces(implementedInterfaces);
    }

    typeRegistry.clearTemplateTypeName();

    return fnType;
  }
  FunctionTypeBuilder inferReturnStatements(@Nullable Node functionBlock) {
    if (functionBlock == null || compiler.getInput(sourceName).isExtern()) {
      return this;
    }
    Preconditions.checkArgument(functionBlock.getType() == Token.BLOCK);
    if (returnType == null) {
      boolean hasNonEmptyReturns = false;
      List<Node> worklist = Lists.newArrayList(functionBlock);
      while (!worklist.isEmpty()) {
        Node current = worklist.remove(worklist.size() - 1);
        int cType = current.getType();
        if (cType == Token.RETURN && current.getFirstChild() != null ||
            cType == Token.THROW) {
          hasNonEmptyReturns = true;
          break;
        } else if (NodeUtil.isStatementBlock(current) ||
            NodeUtil.isControlStructure(current)) {
          for (Node child = current.getFirstChild();
               child != null; child = child.getNext()) {
            worklist.add(child);
          }
        }
      }

      if (!hasNonEmptyReturns) {
        returnType = typeRegistry.getNativeType(VOID_TYPE);
        returnTypeInferred = true;
      }
    }
    return this;
  }
