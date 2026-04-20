  public void put(CharSequence columnFamily, CharSequence columnQualifier, CharSequence value) {
    if (org.apache.accumulo.core.data.Mutation.this.buffer!=null) {
      put(columnFamily, columnQualifier, EMPTY_BYTES, false, 0l, false, value);
    }
  }
