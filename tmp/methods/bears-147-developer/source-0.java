  public BigIntegerOIntArithmetic(OIntFactory factory) {
    this.factory = factory;
    this.twoPowersList = new ArrayList<>();
  }
  public List<DRes<OInt>> getPowersOfTwo(int maxPower) {
    // TODO taken from MiscBigIntegerGenerators, clean up
    int currentLength = twoPowersList.size();
    if (maxPower > currentLength) {
      ArrayList<DRes<OInt>> newTwoPowersList = new ArrayList<>(maxPower);
      newTwoPowersList.addAll(twoPowersList);
      BigInteger currentValue = ((BigIntegerOInt) newTwoPowersList.get(currentLength - 1))
          .getValue();
      while (maxPower > newTwoPowersList.size()) {
        currentValue = currentValue.shiftLeft(1);
        newTwoPowersList.add(new BigIntegerOInt(currentValue));
      }
      twoPowersList = Collections.unmodifiableList(newTwoPowersList);
    }
    return twoPowersList.subList(0, maxPower);
  }
