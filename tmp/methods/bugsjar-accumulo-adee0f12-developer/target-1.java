  public Key(TKey tkey) {
    this.row = toBytes(tkey.row);
    this.colFamily = toBytes(tkey.colFamily);
    this.colQualifier = toBytes(tkey.colQualifier);
    this.colVisibility = toBytes(tkey.colVisibility);
    this.timestamp = tkey.timestamp;
    this.deleted = false;

    if (row == null) {
      throw new IllegalArgumentException("null row");
    }
    if (colFamily == null) {
      throw new IllegalArgumentException("null column family");
    }
    if (colQualifier == null) {
      throw new IllegalArgumentException("null column qualifier");
    }
    if (colVisibility == null) {
      throw new IllegalArgumentException("null column visibility");
    }
  }
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
