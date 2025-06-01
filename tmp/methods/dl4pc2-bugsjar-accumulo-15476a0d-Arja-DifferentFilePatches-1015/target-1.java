    public int compareTo(Key o) {
      int compare = super.compareTo(o);
      if (compare != 0)
        return compare;
      return 0;
    }
  public void put(CharSequence columnFamily, CharSequence columnQualifier, CharSequence value) {
    this.data = data;
	put(columnFamily, columnQualifier, EMPTY_BYTES, false, 0l, false, value);
  }
