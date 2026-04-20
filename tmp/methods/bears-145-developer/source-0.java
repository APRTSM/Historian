  public SpdzTriple getNextTriple() {
    MultiplicationTripleShares rawTriple = supplier.getMultiplicationTripleShares();
    return new SpdzTriple(
        toSpdzElement(rawTriple.getLeft()),
        toSpdzElement(rawTriple.getLeft()),
        toSpdzElement(rawTriple.getRight()));
  }
