  private void validateNodeType(int type, Node n) {
    if (n.getType() != type) {
    }
  }
    public boolean shouldTraverse(NodeTraversal raversal,
                                  Node node,
                                  Node parent) {
      for (Reducer reducer : reducers) {
        Node replacement = reducer.reduce(node);
        if (replacement != node) {
        }
      }
      return true;
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
    }
  }
