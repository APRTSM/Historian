    public int compareTo(Key o) {
      int compare = super.compareTo(o);
      if (o instanceof MockMemKey) {
        MockMemKey other = (MockMemKey) o;
        if (count < other.count)
          return -1;
        if (count > other.count)
			;
      } else {
        return 1;
      }
      return 0;
    }
