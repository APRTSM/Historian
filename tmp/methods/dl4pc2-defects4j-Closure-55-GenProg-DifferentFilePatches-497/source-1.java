  private void validateNodeType(int type, Node n) {
    if (n.getType() != type) {
      violation(
          "Expected " + Node.tokenToName(type) + " but was "
              + Node.tokenToName(n.getType()), n);
    }
  }
    private Node getGetPropertyName(Node functionNode) {
      Node value = maybeGetSingleReturnRValue(functionNode);
      if (value != null &&
          NodeUtil.isGetProp(value) &&
          NodeUtil.isThis(value.getFirstChild())) {
        return value.getLastChild();
      }
      return null;
    }
    void apply() {
      parent.replaceChild(oldChild, newChild);
      compiler.reportCodeChange();
    }
  public void process(Node externs, Node root) {
    List<Reducer> reducers = ImmutableList.of(new ReturnConstantReducer(),
                                              new GetterReducer(),
                                              new SetterReducer(),
                                              new EmptyFunctionReducer(),
                                              new IdentityReducer());

    Multimap<Reducer, Reduction> reductionMap = HashMultimap.create();

    // Accumulate possible reductions in the reduction multi map.  They
    // will be applied in the loop below.
    NodeTraversal.traverse(compiler, root,
                           new ReductionGatherer(reducers, reductionMap));

    // Apply reductions iff they will provide some savings.
    for (Reducer reducer : reducers) {
      Collection<Reduction> reductions = reductionMap.get(reducer);
      if (reductions.isEmpty()) {
        continue;
      }

      Node helperCode = parseHelperCode(reducer);
      if (helperCode == null) {
        continue;
      }

      int helperCodeCost = InlineCostEstimator.getCost(helperCode);

      // Estimate savings
      int savings = 0;
      for (Reduction reduction : reductions) {
        savings += reduction.estimateSavings();
      }

      // Compare estimated savings against the helper cost.  Apply
      // reductions if doing so will result in some savings.
      if (savings > (helperCodeCost + SAVINGS_THRESHOLD)) {
        for (Reduction reduction : reductions) {
          reduction.apply();
        }

        Node addingRoot = compiler.getNodeForCodeInsertion(null);
        addingRoot.addChildrenToFront(helperCode);
        compiler.reportCodeChange();
      }
    }
  }
    public boolean shouldTraverse(NodeTraversal raversal,
                                  Node node,
                                  Node parent) {
      for (Reducer reducer : reducers) {
        Node replacement = reducer.reduce(node);
        if (replacement != node) {
          reductions.put(reducer, new Reduction(parent, node, replacement));
          return false;
        }
      }
      return true;
    }
    public Node reduce(Node node) {
      if (!isReduceableFunctionExpression(node)) {
        return node;
      }

      Node propName = getGetPropertyName(node);
      if (propName != null) {
        if (propName.getType() != Token.STRING) {
          throw new IllegalStateException(
              "Expected STRING, got " + Token.name(propName.getType()));
        }

        return buildCallNode(FACTORY_METHOD_NAME, propName,
                             node.getLineno(), node.getCharno());
      } else {
        return node;
      }
    }
