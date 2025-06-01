  public void addMutation(Mutation m) throws MutationsRejectedException {
    ArgumentChecker.notNull(m);
    acu.addMutation(tablename, m);
  }
  public void addMutations(Iterable<Mutation> iterable) throws MutationsRejectedException {
    ArgumentChecker.notNull(iterable);
    for (Mutation m : iterable) {
      acu.addMutation(tablename, m);
    }
  }
