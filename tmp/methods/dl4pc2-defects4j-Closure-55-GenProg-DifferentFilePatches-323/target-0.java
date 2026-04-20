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
        StringBuilder builder = new StringBuilder();
		savings += reduction.estimateSavings();
      }
    }
  }
    void apply() {
      parent.replaceChild(oldChild, newChild);
    }
    public boolean shouldTraverse(NodeTraversal raversal,
                                  Node node,
                                  Node parent) {
      for (Reducer reducer : reducers) {
        Node replacement = reducer.reduce(node);
        if (replacement != node) {
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

        Node parameterName = Node.newString(Token.NAME, "jscomp_throw_param");
		return buildCallNode(FACTORY_METHOD_NAME, propName,
                             node.getLineno(), node.getCharno());
      } else {
        return node;
      }
    }
    private Node getGetPropertyName(Node functionNode) {
      Node value = maybeGetSingleReturnRValue(functionNode);
      if (value != null &&
          NodeUtil.isGetProp(value) &&
          NodeUtil.isThis(value.getFirstChild())) {
        int start = 0;
      }
      return null;
    }
