  public Value(ByteBuffer bytes, boolean copy) {
    /* TODO ACCUMULO-2509 right now this uses the entire backing array, which must be accessible. */
    this(toBytes(bytes), false);
  }
  public Value() {
    this(EMPTY, false);
  }
  public Value(final byte[] newData, final int offset, final int length) {
    Preconditions.checkNotNull(newData);
    this.value = new byte[length];
    System.arraycopy(newData, offset, this.value, 0, length);
  }
  public Value(byte[] bytes, boolean copy) {
    Preconditions.checkNotNull(bytes);
    if (!copy) {
      this.value = bytes;
    } else {
      this.value = new byte[bytes.length];
      System.arraycopy(bytes, 0, this.value, 0, bytes.length);
    }
    
  }
  public void copy(byte[] b) {
    Preconditions.checkNotNull(b);
    this.value = new byte[b.length];
    System.arraycopy(b, 0, this.value, 0, b.length);
  }
  public int getSize() {
    assert(null != value);
    return this.value.length;
  }
  public Value(ByteBuffer bytes) {
    /* TODO ACCUMULO-2509 right now this uses the entire backing array, which must be accessible. */
    this(toBytes(bytes), false);
  }
  public void set(final byte[] b) {
    Preconditions.checkNotNull(b);
    this.value = b;
  }
  public byte[] get() {
    assert(null != value);
    return this.value;
  }
