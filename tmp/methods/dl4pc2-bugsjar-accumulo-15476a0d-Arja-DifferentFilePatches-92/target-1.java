    public int compareTo(Key o) {
      int compare = super.compareTo(o);
      if (compare != 0)
        return compare;
      if (o instanceof MockMemKey) {
        MockMemKey other = (MockMemKey) o;
        if (count < other.count)
          return -1;
      } else {
        return 1;
      }
      return 0;
    }
  private void put(CharSequence cf, CharSequence cq, byte[] cv, boolean hasts, long ts, boolean deleted, CharSequence val) {
    this.data = data;
	put(cf, cq, cv, hasts, ts, deleted, TextUtil.getBytes(new Text(val.toString())));
  }
