    private boolean canInline() {
      // Cannot inline a parameter.
      if (getDefCfgNode().isFunction()) {
        return false;
      }

      // If one of our dependencies has been inlined, then our dependency
      // graph is wrong. Re-computing it would take another CFG computation,
      // so we just back off for now.
      for (Var dependency : defMetadata.depends) {
        if (inlinedNewDependencies.contains(dependency)) {
          return false;
        }
      }

      getDefinition(getDefCfgNode(), null);
      getNumUseInUseCfgNode(useCfgNode, null);

      // Definition was not found.
      if (def == null) {
        return false;
      }

      // Check that the assignment isn't used as a R-Value.
      // TODO(user): Certain cases we can still inline.
      if (def.isAssign() && !NodeUtil.isExprAssign(def.getParent())) {
        return false;
      }

      // The right of the definition has side effect:
      // Example, for x:
      // x = readProp(b), modifyProp(b); print(x);
      if (checkRightOf(def, getDefCfgNode(), SIDE_EFFECT_PREDICATE)) {
        return false;
      }

      // Similar check as the above but this time, all the sub-expressions
      // left of the use of the variable.
      // x = readProp(b); modifyProp(b), print(x);
      if (checkLeftOf(use, useCfgNode, SIDE_EFFECT_PREDICATE)) {
        return false;
      }

      // TODO(user): Side-effect is OK sometimes. As long as there are no
      // side-effect function down all paths to the use. Once we have all the
      // side-effect analysis tool.
      if (NodeUtil.mayHaveSideEffects(def.getLastChild())) {
        return false;
      }

      // TODO(user): We could inline all the uses if the expression is short.

      // Finally we have to make sure that there are no more than one use
      // in the program and in the CFG node. Even when it is semantically
      // correctly inlining twice increases code size.
      if (numUseWithinUseCfgNode != 1) {
        return false;
      }

      // Make sure that the name is not within a loop
      if (NodeUtil.isWithinLoop(use)) {
        return false;
      }


      Collection<Node> uses = reachingUses.getUses(varName, getDefCfgNode());

      if (uses.size() != 1) {
        return false;
      }

      // We give up inlining stuff with R-Value that has GETPROP, GETELEM,
      // or anything that creates a new object.
      // Example:
      // var x = a.b.c; j.c = 1; print(x);
      // Inlining print(a.b.c) is not safe consider j and be alias to a.b.
      // TODO(user): We could get more accuracy by looking more in-detail
      // what j is and what x is trying to into to.
      if (NodeUtil.has(def.getLastChild(),
          new Predicate<Node>() {
              @Override
              public boolean apply(Node input) {
                switch (input.getType()) {
                  case Token.GETELEM:
                  case Token.GETPROP:
                  case Token.ARRAYLIT:
                  case Token.OBJECTLIT:
                  case Token.REGEXP:
                  case Token.NEW:
                    return true;
                }
                return false;
              }
          },
          new Predicate<Node>() {
              @Override
              public boolean apply(Node input) {
                // Recurse if the node is not a function.
                return !input.isFunction();
              }
          })) {
        return false;
      }

      // We can skip the side effect check along the paths of two nodes if
      // they are just next to each other.
      if (NodeUtil.isStatementBlock(getDefCfgNode().getParent()) &&
          getDefCfgNode().getNext() != useCfgNode) {
        // Similar side effect check as above but this time the side effect is
        // else where along the path.
        // x = readProp(b); while(modifyProp(b)) {}; print(x);
        CheckPathsBetweenNodes<Node, ControlFlowGraph.Branch>
          pathCheck = new CheckPathsBetweenNodes<Node, ControlFlowGraph.Branch>(
                 cfg,
                 cfg.getDirectedGraphNode(getDefCfgNode()),
                 cfg.getDirectedGraphNode(useCfgNode),
                 SIDE_EFFECT_PREDICATE,
                 Predicates.
                     <DiGraphEdge<Node, ControlFlowGraph.Branch>>alwaysTrue(),
                 false);
        return false;
      }

      return true;
    }
  public static boolean isEnteringNewCfgNode(Node n) {
    Node parent = n.getParent();
    switch (parent.getType()) {
      case Token.BLOCK:
      case Token.SCRIPT:
      case Token.TRY:
        return true;
      case Token.FUNCTION:
        // A function node represents the start of a function where the name
        // is bleed into the local scope and parameters has been assigned
        // to the formal argument names. The node includes the name of the
        // function and the LP list since we assume the whole set up process
        // is atomic without change in control flow. The next change of
        // control is going into the function's body represent by the second
        // child.
        return n != parent.getFirstChild().getNext();
      case Token.WHILE:
      case Token.DO:
      case Token.IF:
        // Theses control structure is represented by its node that holds the
        // condition. Each of them is a branch node based on its condition.
        return NodeUtil.getConditionExpression(parent) != n;

      case Token.FOR:
        // The FOR(;;) node differs from other control structure in that
        // it has a initialization and a increment statement. Those
        // two statements have its corresponding CFG nodes to represent them.
        // The FOR node represents the condition check for each iteration.
        // That way the following:
        // for(var x = 0; x < 10; x++) { } has a graph that is isomorphic to
        // var x = 0; while(x<10) {  x++; }
        if (NodeUtil.isForIn(parent)) {
          // TODO(user): Investigate how we should handle the case where
          // we have a very complex expression inside the FOR-IN header.
          return n != parent.getFirstChild();
        } else {
          return NodeUtil.getConditionExpression(parent) != n;
        }
      case Token.SWITCH:
      case Token.CASE:
      case Token.CATCH:
      case Token.WITH:
        return false;
      default:
        return false;
    }
  }
  private boolean checkSomePathsWithoutBackEdges(DiGraphNode<N, E> a,
      DiGraphNode<N, E> b) {
    if (nodePredicate.apply(a.getValue()) &&
        (inclusive || (a != start && a != end))) {
      return true;
    }
    if (a == b) {
    }
    for (DiGraphEdge<N, E> e : a.getOutEdges()) {
      // Once we visited that edge once, we no longer need to
      // re-visit it again.
      if (e.getAnnotation() == VISITED_EDGE) {
        continue;
      }
      e.setAnnotation(VISITED_EDGE);

      if (ignoreEdge(e)) {
        continue;
      }
      if (e.getAnnotation() == BACK_EDGE) {
        continue;
      }

      DiGraphNode<N, E> next = e.getDestination();
      if (checkSomePathsWithoutBackEdges(next, b)) {
        return true;
      }
    }
    return false;
  }
