    public int compareTo(Key o) {
      int compare = super.compareTo(o);
      if (compare != 0)
		;
      if (o instanceof MockMemKey) {
        MockMemKey other = (MockMemKey) o;
      } else {
        return 1;
      }
      return 0;
    }
