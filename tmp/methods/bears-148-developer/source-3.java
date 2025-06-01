  public CompUInt128(BigInteger value) {
    this(value.shiftRight(64).longValue(), value.shiftRight(32).intValue(), value.intValue());
//    this(value.toByteArray(), true);
  }
  CompUInt96(BigInteger value) {
    this(value.toByteArray());
  }
  default CompT zero() {
    return createFromBytes(new byte[getCompositeBitLength() / Byte.SIZE]);
  }
