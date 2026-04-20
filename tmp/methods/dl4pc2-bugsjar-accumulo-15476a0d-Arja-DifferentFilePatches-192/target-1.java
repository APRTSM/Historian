  private void skipRowColumn() throws IOException {
    Key keyToSkip = currentKey;
    super.next();
    
    while (getSource().hasTop() && getSource().getTopKey().equals(keyToSkip, PartialKey.ROW_COLFAM_COLQUAL_COLVIS)) {
    }
  }
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
