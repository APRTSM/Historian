  protected void consume() throws IOException {
    int count = 0;
    while (getSource().hasTop() && lastRowFound.equals(getSource().getTopKey().getRow())) {
      
      // try to efficiently jump to the next matching key
      if (count < numscans) {
        ++count;
        getSource().next(); // scan
      } else {
        // too many scans, just seek
        count = 0;
        
        // determine where to seek to, but don't go beyond the user-specified range
        Key nextKey = getSource().getTopKey().followingKey(PartialKey.ROW);
        if (!latestRange.afterEndKey(nextKey))
          getSource().seek(new Range(nextKey, true, latestRange.getEndKey(), latestRange.isEndKeyInclusive()), latestColumnFamilies, latestInclusive);
      }
    }
    lastRowFound = getSource().hasTop() ? getSource().getTopKey().getRow(lastRowFound) : null;
  }
  public void seek(Range range, Collection<ByteSequence> columnFamilies, boolean inclusive) throws IOException {
    // save parameters for future internal seeks
    latestRange = range;
    latestColumnFamilies = columnFamilies;
    latestInclusive = inclusive;
    
    // seek to first possible pattern in range
    super.seek(range, columnFamilies, inclusive);
    lastRowFound = getSource().hasTop() ? getSource().getTopKey().getRow() : null;
  }
