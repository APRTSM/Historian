  public Key(TKey tkey) {
    this.row = toBytes(tkey.row);
    this.colFamily = toBytes(tkey.colFamily);
    this.colQualifier = toBytes(tkey.colQualifier);
    this.colVisibility = toBytes(tkey.colVisibility);
    this.timestamp = tkey.timestamp;
    this.deleted = false;
  }
