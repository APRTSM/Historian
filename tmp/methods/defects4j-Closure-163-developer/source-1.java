    private boolean isGlobalFunctionDeclaration(NodeTraversal t, Node n) {
      // Make sure we're either in the global scope, or the function
      // we're looking at is the root of the current local scope.

      return t.inGlobalScope() &&
          (NodeUtil.isFunctionDeclaration(n) ||
           n.isFunction() &&
           n.getParent().isName());
    }
    private boolean isPrototypePropertyAssign(Node assign) {
      Node n = assign.getFirstChild();
      if (n != null && NodeUtil.isVarOrSimpleAssignLhs(n, assign)
          && n.isGetProp()
          && assign.getParent().isExprResult()) {
        boolean isChainedProperty =
            n.getFirstChild().isGetProp();

    /**
     * Returns the name of a prototype property being assigned to this r-value.
     *
     * Returns null if this is not the R-value of a prototype property, or if
     * the R-value is used in multiple expressions (i.e., if there's
     * a prototype property assignment in a more complex expression).
     */
        if (isChainedProperty) {
          Node child = n.getFirstChild().getFirstChild().getNext();

          if (child.isString() &&
              child.getString().equals("prototype")) {
            return true;
          }
        }
      }


      return false;
    }
    public void visit(NodeTraversal t, Node n, Node parent) {
      if (n.isGetProp()) {
        String propName = n.getFirstChild().getNext().getString();

          if (propName.equals("prototype")) {
          processPrototypeParent(t, parent);
          } else if (compiler.getCodingConvention().isExported(propName)) {
            addGlobalUseOfSymbol(propName, t.getModule(), PROPERTY);
          } else {
            // Do not mark prototype prop assigns as a 'use' in the global scope.
          addSymbolUse(propName, t.getModule(), PROPERTY);
        }

      } else if (n.isObjectLit() &&
        // Make sure that we're not handling object literals being
        // assigned to a prototype, as in:
        // Foo.prototype = {bar: 3, baz: 5};
          !(parent.isAssign() &&
            parent.getFirstChild().isGetProp() &&
            parent.getFirstChild().getLastChild().getString().equals(
                "prototype"))) {

        // var x = {a: 1, b: 2}
        // should count as a use of property a and b.
        for (Node propNameNode = n.getFirstChild(); propNameNode != null;
             propNameNode = propNameNode.getNext()) {
          // May be STRING, GET, or SET, but NUMBER isn't interesting.
          if (!propNameNode.isQuotedString()) {
            addSymbolUse(propNameNode.getString(), t.getModule(), PROPERTY);
          }
        }
      } else if (n.isName()) {
        String name = n.getString();

        Var var = t.getScope().getVar(name);
        if (var != null) {
          // Only process global functions.
          if (var.isGlobal()) {
            if (var.getInitialValue() != null &&
                var.getInitialValue().isFunction()) {
              if (t.inGlobalScope()) {
                if (!processGlobalFunctionDeclaration(t, n, parent,
                        parent.getParent())) {
                  addGlobalUseOfSymbol(name, t.getModule(), VAR);
                }
              } else {
                addSymbolUse(name, t.getModule(), VAR);
              }
            }

          // If it is not a global, it might be accessing a local of the outer
          // scope. If that's the case the functions between the variable's
          // declaring scope and the variable reference scope cannot be moved.
          } else if (var.getScope() != t.getScope()){
            for (int i = symbolStack.size() - 1; i >= 0; i--) {
              NameContext context = symbolStack.get(i);
              if (context.scope == var.getScope()) {
                break;
              }

              context.name.readClosureVariables = true;
            }
          }
        }
      }

      // Process prototype assignments to non-functions.
      if (isPrototypePropertyAssign(n) ||
          isGlobalFunctionDeclaration(t, n) ||
          n.isFunction()) {
        symbolStack.pop();
      }
    }
    private boolean processGlobalFunctionDeclaration(NodeTraversal t,
        Node nameNode, Node parent, Node gramps) {
      Node firstChild = nameNode.getFirstChild();

      if (// Check for a named FUNCTION.
          isGlobalFunctionDeclaration(t, parent) ||
          // Check for a VAR declaration.
          firstChild != null &&
          isGlobalFunctionDeclaration(t, firstChild)) {
        String name = nameNode.getString();
        getNameInfoForName(name, VAR).getDeclarations().add(
            new GlobalFunction(nameNode, parent, gramps, t.getModule()));

        // If the function name is exported, we should create an edge here
        // so that it's never removed.
        if (compiler.getCodingConvention().isExported(name) ||
            anchorUnusedVars) {
          addGlobalUseOfSymbol(name, t.getModule(), VAR);
        }

        return true;
      }
      return false;
    }
    public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
      // Process prototype assignments to non-functions.
      if (isPrototypePropertyAssign(n)) {
        symbolStack.push(new NameContext(getNameInfoForName(
                n.getFirstChild().getLastChild().getString(), PROPERTY)));
      } else if (isGlobalFunctionDeclaration(t, n)) {
        String name = parent.isName() ?
            parent.getString() /* VAR */ :
            n.getFirstChild().getString() /* named function */;
        symbolStack.push(new NameContext(getNameInfoForName(name, VAR)));
      } else if (n.isFunction()) {
        symbolStack.push(new NameContext(anonymousNode));
      }
      return true;
    }
    AssignmentProperty(Node node, JSModule module) {
      this.exprNode = node;
      this.module = module;
    }
    LiteralProperty(Node key, Node value, Node map, Node assign,
        JSModule module) {
      this.key = key;
      this.value = value;
      this.map = map;
      this.assign = assign;
      this.module = module;
    }
    private ProcessProperties() {
      symbolStack.push(new NameContext(globalNode));
    }
    public void enterScope(NodeTraversal t) {
      symbolStack.peek().scope = t.getScope();
          // NOTE(nicksantos): We use the same anonymous node for all
          // functions that do not have reasonable names. I can't remember
          // at the moment why we do this. I think it's because anonymous
          // nodes can never have in-edges. They're just there as a placeholder
          // for scope information, and do not matter in the edge propagation.
    }
    private void processPrototypeParent(NodeTraversal t, Node n) {

      switch (n.getType()) {
        // Foo.prototype.getBar = function() { ... }
        case Token.GETPROP:
          Node dest = n.getFirstChild().getNext();
          Node parent = n.getParent();
          Node grandParent = parent.getParent();

          if (dest.isString() &&
              NodeUtil.isExprAssign(grandParent) &&
              NodeUtil.isVarOrSimpleAssignLhs(n, parent)) {
            String name = dest.getString();
            Property prop = new AssignmentProperty(
                grandParent,
                t.getModule());
            getNameInfoForName(name, PROPERTY).getDeclarations().add(prop);
          }
          break;

        // Foo.prototype = { "getBar" : function() { ... } }
        case Token.ASSIGN:
          Node map = n.getFirstChild().getNext();
          if (map.isObjectLit()) {
            for (Node key = map.getFirstChild();
                 key != null; key = key.getNext()) {
              // May be STRING, GET, or SET,
              String name = key.getString();
              Property prop = new LiteralProperty(
                  key, key.getFirstChild(), map, n,
                  t.getModule());
              getNameInfoForName(name, PROPERTY).getDeclarations().add(prop);
            }
          }
          break;
      }
    }
    NameContext(NameInfo name) {
      this.name = name;
    }
    GlobalFunction(Node nameNode, Node parent, Node gramps, JSModule module) {
      Preconditions.checkState(
          parent.isVar() ||
          NodeUtil.isFunctionDeclaration(parent));
      this.nameNode = nameNode;
      this.module = module;
    }
  private void moveMethods(Collection<NameInfo> allNameInfo) {
    boolean hasStubDeclaration = idGenerator.hasGeneratedAnyIds();
    for (NameInfo nameInfo : allNameInfo) {
      if (!nameInfo.isReferenced()) {
        // The code below can't do anything with unreferenced name
        // infos.  They should be skipped to avoid NPE since their
        // deepestCommonModuleRef is null.
        continue;
      }

      if (nameInfo.readsClosureVariables()) {
        continue;
      }

      JSModule deepestCommonModuleRef = nameInfo.getDeepestCommonModuleRef();
      if (deepestCommonModuleRef == null) {
        compiler.report(JSError.make(NULL_COMMON_MODULE_ERROR));
        continue;
      }

      Iterator<Symbol> declarations =
          nameInfo.getDeclarations().descendingIterator();
      while (declarations.hasNext()) {
        Symbol symbol = declarations.next();
        if (!(symbol instanceof Property)) {
          continue;
        }
        Property prop = (Property) symbol;

        // We should only move a property across modules if:
        // 1) We can move it deeper in the module graph, and
        // 2) it's a function, and
        // 3) it is not a get or a set, and
        // 4) the class is available in the global scope.
        //
        // #1 should be obvious. #2 is more subtle. It's possible
        // to copy off of a prototype, as in the code:
        // for (var k in Foo.prototype) {
        //   doSomethingWith(Foo.prototype[k]);
        // }
        // This is a common way to implement pseudo-multiple inheritance in JS.
        //
        // So if we move a prototype method into a deeper module, we must
        // replace it with a stub function so that it preserves its original
        // behavior.

        Node value = prop.getValue();
        if (moduleGraph.dependsOn(deepestCommonModuleRef, prop.getModule()) &&
            value.isFunction()) {
          Node valueParent = value.getParent();
          if (valueParent.isGetterDef()
              || valueParent.isSetterDef()) {
            // TODO(johnlenz): a GET or SET can't be deferred like a normal
            // FUNCTION property definition as a mix-in would get the result
            // of a GET instead of the function itself.
            continue;
          }
          Node proto = prop.getPrototype();
          int stubId = idGenerator.newId();

          // example: JSCompiler_stubMethod(id);
          Node stubCall = IR.call(
              IR.name(STUB_METHOD_NAME),
              IR.number(stubId))
              .copyInformationFromForTree(value);
          stubCall.putBooleanProp(Node.FREE_CALL, true);

          // stub out the method in the original module
          // A.prototype.b = JSCompiler_stubMethod(id);
          valueParent.replaceChild(value, stubCall);

          // unstub the function body in the deeper module
          Node unstubParent = compiler.getNodeForCodeInsertion(
              deepestCommonModuleRef);
          Node unstubCall = IR.call(
              IR.name(UNSTUB_METHOD_NAME),
              IR.number(stubId),
              value);
          unstubCall.putBooleanProp(Node.FREE_CALL, true);
          unstubParent.addChildToFront(
              // A.prototype.b = JSCompiler_unstubMethod(id, body);
              IR.exprResult(
                  IR.assign(
                      IR.getprop(
                          proto.cloneTree(),
                          IR.string(nameInfo.name)),
                      unstubCall))
                  .copyInformationFromForTree(value));

          compiler.reportCodeChange();
        }
      }
    }

    if (!hasStubDeclaration && idGenerator.hasGeneratedAnyIds()) {
      // Declare stub functions in the top-most module.
      Node declarations = compiler.parseSyntheticCode(STUB_DECLARATIONS);
      compiler.getNodeForCodeInsertion(null).addChildrenToFront(
          declarations.removeChildren());
    }
  }
