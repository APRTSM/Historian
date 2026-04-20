    public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
      return true;
    }
    public void addDeclaredName(String name) {
      if (!name.equals(ARGUMENTS)) {
        if (global) {
          reserveName(name);
        } else {
          // It hasn't been declared locally yet, so increment the count.
          if (!declarations.containsKey(name)) {
            int id = incrementNameCount(name);
            String newName = null;
            if (id != 0) {
              newName = getUniqueName(name, id);
            }
            declarations.put(name, newName);
          }
        }
      }
    }
    public void addDeclaredName(String name) {
      Preconditions.checkState(!name.equals(ARGUMENTS));
      if (!declarations.containsKey(name)) {
        declarations.put(name, getUniqueName(name));
      }
    }
    void handleScopeVar(Var v) {
      String name  = v.getName();
      if (containsSeparator(name)) {
        String newName = getOrginalName(name);
        // Check if the new name is valid and if it would cause conflicts.
        if (TokenStream.isJSIdentifier(newName) &&
            !referencedNames.contains(newName) &&
            !newName.equals(ARGUMENTS)) {
          referencedNames.remove(name);
          // Adding a reference to the new name to prevent either the parent
          // scopes or the current scope renaming another var to this new name.
          referencedNames.add(newName);
          List<Node> references = nameMap.get(name);
          Preconditions.checkState(references != null);
          for (Node n : references) {
            Preconditions.checkState(n.getType() == Token.NAME);
            n.setString(newName);
          }
          compiler.reportCodeChange();
        }
        nameMap.remove(name);
      }
    }
    public void visit(NodeTraversal t, Node node, Node parent) {
      if (t.inGlobalScope()) {
        return;
      }

      if (NodeUtil.isReferenceName(node)) {
        String name = node.getString();
        // Add all referenced names to the set so it is possible to check for
        // conflicts.
        referencedNames.add(name);
        // Store only references to candidate names in the node map.
        if (containsSeparator(name)) {
          addCandidateNameReference(name, node);
        }
      }
    }
    private void addCandidateNameReference(String name, Node n) {
      List<Node> nodes = nameMap.get(name);
      if (null == nodes) {
        nodes = Lists.newLinkedList();
        nameMap.put(name, nodes);
      }
      nodes.add(n);
    }
    public void enterScope(NodeTraversal t) {
      if (t.inGlobalScope()) {
        return;
      }

      referenceStack.push(referencedNames);
      referencedNames = Sets.newHashSet();
    }
    public void exitScope(NodeTraversal t) {
      if (t.inGlobalScope()) {
        return;
      }

      for (Iterator<Var> it = t.getScope().getVars(); it.hasNext();) {
        Var v = it.next();
        handleScopeVar(v);
      }

      // Merge any names that were referenced but not declared in the current
      // scope.
      Set<String> current = referencedNames;
      referencedNames = referenceStack.pop();
      // If there isn't anything left in the stack we will be going into the
      // global scope: don't try to build a set of referenced names for the
      // global scope.
      if (!referenceStack.isEmpty()) {
        referencedNames.addAll(current);
      }
    }
  static boolean isReferenceName(Node n) {
    return isName(n) && !n.getString().isEmpty() && !isLabelName(n);
  }
  public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root,
        new NormalizeStatements(compiler, assertOnChange));
    if (MAKE_LOCAL_NAMES_UNIQUE) {
      MakeDeclaredNamesUnique renamer = new MakeDeclaredNamesUnique();
      NodeTraversal t = new NodeTraversal(compiler, renamer);
      t.traverseRoots(externs, root);
    }
    // It is important that removeDuplicateDeclarations runs after
    // MakeDeclaredNamesUnique in order for catch block exception names to be
    // handled properly. Specifically, catch block exception names are
    // only valid within the catch block, but our currect Scope logic
    // has no concept of this and includes it in the containing function 
    // (or global scope). MakeDeclaredNamesUnique makes the catch exception
    // names unique so that removeDuplicateDeclarations() will properly handle
    // cases where a function scope variable conflict with a exception name:
    //   function f() {
    //      try {throw 0;} catch(e) {e; /* catch scope 'e'*/}
    //      var e = 1; // f scope 'e'
    //   }
    // otherwise 'var e = 1' would be rewritten as 'e = 1'.
    // TODO(johnlenz): Introduce a seperate scope for catch nodes. 
    removeDuplicateDeclarations(root);
    new PropogateConstantAnnotations(compiler, assertOnChange)
        .process(externs, root);
  }
