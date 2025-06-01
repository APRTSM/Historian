    AssignmentProperty(Node node, Var rootVar, JSModule module) {
      this.exprNode = node;
      this.rootVar = rootVar;
      this.module = module;
    }
    private boolean isGlobalFunctionDeclaration(NodeTraversal t, Node n) {
      // Make sure we're either in the global scope, or the function
      // we're looking at is the root of the current local scope.
      Scope s = t.getScope();
      if (!(s.isGlobal() ||
            s.getDepth() == 1 && s.getRootNode() == n)) {
        return false;
      }

      return NodeUtil.isFunctionDeclaration(n) ||
          n.isFunction() && n.getParent().isName();
    }
    private boolean processPrototypeRef(NodeTraversal t, Node ref) {
      Node root = NodeUtil.getRootOfQualifiedName(ref);

      Node n = ref.getParent();
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
                t.getScope().getVar(root.getString()),
                t.getModule());
            getNameInfoForName(name, PROPERTY).getDeclarations().add(prop);
            return true;
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
                  t.getScope().getVar(root.getString()),
                  t.getModule());
              getNameInfoForName(name, PROPERTY).getDeclarations().add(prop);
            }
            return true;
          }
          break;
      }
      return false;
    }
    private boolean isAssignRValue(Node n, Node parent) {
      return parent != null && parent.isAssign() && parent.getFirstChild() != n;
    }
    private String getPrototypePropertyNameFromRValue(Node rValue) {
      Node lValue = NodeUtil.getBestLValue(rValue);
      if (lValue == null ||
          lValue.getParent() == null ||
          lValue.getParent().getParent() == null ||
          !(NodeUtil.isObjectLitKey(lValue, lValue.getParent()) ||
            NodeUtil.isExprAssign(lValue.getParent().getParent()))) {
        return null;
      }

      String lValueName =
          NodeUtil.getBestLValueName(NodeUtil.getBestLValue(rValue));
      if (lValueName == null) {
        return null;
      }
      int lastDot = lValueName.lastIndexOf('.');
      if (lastDot == -1) {
        return null;
      }

      String firstPart = lValueName.substring(0, lastDot);
      if (!firstPart.endsWith(".prototype")) {
        return null;
      }

      return lValueName.substring(lastDot + 1);
    }
    public Var getRootVar() {
      return rootVar;
    }
    public Var getRootVar() {
      return rootVar;
    }
    public void exitScope(NodeTraversal t) {
      symbolStack.pop();
    }
    private String processNonFunctionPrototypeAssign(Node n, Node parent) {
      if (isAssignRValue(n, parent) && !n.isFunction()) {
        return getPrototypePropertyNameFromRValue(n);
      }
      return null;
    }
    NameContext(NameInfo name, Scope scope) {
      this.name = name;
      this.scope = scope;
    }
    public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
      // Process prototype assignments to non-functions.
      String propName = processNonFunctionPrototypeAssign(n, parent);
      if (propName != null) {
        symbolStack.push(
            new NameContext(
                getNameInfoForName(propName, PROPERTY), null));
      }
      return true;
    }
    private boolean processGlobalFunctionDeclaration(NodeTraversal t,
        Node nameNode, Var v) {
      Node firstChild = nameNode.getFirstChild();
      Node parent = nameNode.getParent();

      if (// Check for a named FUNCTION.
          isGlobalFunctionDeclaration(t, parent) ||
          // Check for a VAR declaration.
          firstChild != null &&
          isGlobalFunctionDeclaration(t, firstChild)) {
        String name = nameNode.getString();
        getNameInfoForName(name, VAR).getDeclarations().add(
            new GlobalFunction(nameNode, v, t.getModule()));

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
    public void visit(NodeTraversal t, Node n, Node parent) {
      if (n.isGetProp()) {
        String propName = n.getFirstChild().getNext().getString();

        if (n.isQualifiedName()) {
          if (propName.equals("prototype")) {
            if (processPrototypeRef(t, n)) {
              return;
            }
          } else if (compiler.getCodingConvention().isExported(propName)) {
            addGlobalUseOfSymbol(propName, t.getModule(), PROPERTY);
            return;
          } else {
            // Do not mark prototype prop assigns as a 'use' in the global scope.
            if (n.getParent().isAssign() && n.getNext() != null) {
              String rValueName = getPrototypePropertyNameFromRValue(n);
              if (rValueName != null) {
                return;
              }
            }
          }
        }

        addSymbolUse(propName, t.getModule(), PROPERTY);
      } else if (n.isObjectLit()) {
        // Make sure that we're not handling object literals being
        // assigned to a prototype, as in:
        // Foo.prototype = {bar: 3, baz: 5};
        String lValueName = NodeUtil.getBestLValueName(
            NodeUtil.getBestLValue(n));
        if (lValueName != null && lValueName.endsWith(".prototype")) {
          return;
        }

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
                if (!processGlobalFunctionDeclaration(t, n, var)) {
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
      if (processNonFunctionPrototypeAssign(n, parent) != null) {
        symbolStack.pop();
      }
    }
    public Var getRootVar() {
      return var;
    }
    GlobalFunction(Node nameNode, Var var, JSModule module) {
      Node parent = nameNode.getParent();
      Preconditions.checkState(
          parent.isVar() ||
          NodeUtil.isFunctionDeclaration(parent));
      this.nameNode = nameNode;
      this.var = var;
      this.module = module;
    }
    public void enterScope(NodeTraversal t) {
      Node n = t.getCurrentNode();
      if (n.isFunction()) {
        String propName = getPrototypePropertyNameFromRValue(n);
        if (propName != null) {
          symbolStack.push(
              new NameContext(
                  getNameInfoForName(propName, PROPERTY),
                  t.getScope()));
        } else if (isGlobalFunctionDeclaration(t, n)) {
          Node parent = n.getParent();
          String name = parent.isName() ?
              parent.getString() /* VAR */ :
              n.getFirstChild().getString() /* named function */;
          symbolStack.push(
              new NameContext(getNameInfoForName(name, VAR), t.getScope()));
        } else {
          // NOTE(nicksantos): We use the same anonymous node for all
          // functions that do not have reasonable names. I can't remember
          // at the moment why we do this. I think it's because anonymous
          // nodes can never have in-edges. They're just there as a placeholder
          // for scope information, and do not matter in the edge propagation.
          symbolStack.push(new NameContext(anonymousNode, t.getScope()));
        }
      } else {
        Preconditions.checkState(t.inGlobalScope());
        symbolStack.push(new NameContext(globalNode, t.getScope()));
      }
    }
    LiteralProperty(Node key, Node value, Node map, Node assign,
        Var rootVar, JSModule module) {
      this.key = key;
      this.value = value;
      this.map = map;
      this.assign = assign;
      this.rootVar = rootVar;
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
        if (!(prop.getRootVar() != null && prop.getRootVar().isGlobal())) {
          continue;
        }

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
