  public SpdzInputMask getNextInputMask(int towardPlayerID) {
    ensureInitialized();
    if (masks.isEmpty()) {
      logger.trace("Getting another mask batch");
      masks.addAll(mascot.getInputMasks(towardPlayerID, batchSize));
      logger.trace("Got another mask batch");
    }
    return MascotFormatConverter.toSpdzInputMask(masks.pop());
  }
  public SpdzMascotDataSupplier(int myId, int numberOfPlayers, int instanceId,
      Supplier<Network> tripleNetwork, BigInteger modulus, int modBitLength,
      Function<Integer, SpdzSInt[]> preprocessedValues, int prgSeedLength, int batchSize,
      FieldElement ssk, Map<Integer, RotList> seedOts, Drbg drbg) {
    this.myId = myId;
    this.numberOfPlayers = numberOfPlayers;
    this.instanceId = instanceId;
    this.tripleNetwork = tripleNetwork;
    this.modulus = modulus;
    this.preprocessedValues = preprocessedValues;
    this.triples = new ArrayDeque<>();
    this.masks = new ArrayDeque<>();
    this.randomElements = new ArrayDeque<>();
    this.randomBits = new ArrayDeque<>();
    this.prgSeedLength = prgSeedLength;
    this.modBitLength = modBitLength;
    this.batchSize = batchSize;
    this.ssk = ssk;
    this.seedOts = seedOts;
    this.drbg = drbg;
  }
