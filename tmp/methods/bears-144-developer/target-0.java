  public byte[] toByteArray() {
    // TODO when we return bits directly we run into correlation errors in TestParallelMascots.
    return bits.clone();
  }
