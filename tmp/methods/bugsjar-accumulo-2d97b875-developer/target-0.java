  public Mutation(TMutation tmutation) {
    this.row = ByteBufferUtil.toBytes(tmutation.row);
    this.data = ByteBufferUtil.toBytes(tmutation.data);
    this.entries = tmutation.entries;
    this.values = ByteBufferUtil.toBytesList(tmutation.values);

    if (this.row == null) {
      throw new IllegalArgumentException("null row");
    }
    if (this.data == null) {
      throw new IllegalArgumentException("null serialized data");
    }
  }
