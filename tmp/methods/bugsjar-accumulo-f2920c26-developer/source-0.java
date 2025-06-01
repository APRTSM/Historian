  public int getSize() {
    if (this.value == null) {
      throw new IllegalStateException("Uninitialized. Null constructor " + "called w/o accompanying readFields invocation");
    }
    return this.value.length;
  }
  public Value() {
    super();
  }
  public byte[] get() {
    if (this.value == null) {
      throw new IllegalStateException("Uninitialized. Null constructor " + "called w/o accompanying readFields invocation");
    }
    return this.value;
  }
