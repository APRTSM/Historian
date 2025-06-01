    public boolean accept(Key k, Value v) {
      for (Range r : ranges) {
        if (r.contains(k))
          return true;
      }
      return false;
    }
