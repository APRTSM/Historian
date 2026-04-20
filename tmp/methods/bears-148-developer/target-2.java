  public CompUInt128(BigInteger value) {
    this(value.shiftRight(64).longValue(), value.shiftRight(32).intValue(), value.intValue());
  }
  CompUInt96(BigInteger value) {
    this(value.shiftRight(64).intValue(), value.shiftRight(32).intValue(), value.intValue());
  }
  public CompUInt96 createFromBigInteger(BigInteger value) {
    return value == null ? null : new CompUInt96(value);
  }
