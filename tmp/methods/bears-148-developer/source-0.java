  public CompUInt128(BigInteger value) {
    this(value.shiftRight(64).longValue(), value.shiftRight(32).intValue(), value.intValue());
//    this(value.toByteArray(), true);
  }
