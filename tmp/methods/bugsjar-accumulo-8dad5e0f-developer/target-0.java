  public void seek(Range range, Collection<ByteSequence> columnFamilies, boolean inclusive) throws IOException {
    // save parameters for future internal seeks
    latestRange = range;
    latestColumnFamilies = columnFamilies;
    latestInclusive = inclusive;
    lastRowFound = null;
    
    Key startKey = range.getStartKey();
    Range seekRange = new Range(startKey == null ? null : new Key(startKey.getRow()), true, range.getEndKey(), range.isEndKeyInclusive());
    super.seek(seekRange, columnFamilies, inclusive);

    if (getSource().hasTop()) {
      lastRowFound = getSource().getTopKey().getRow();
      if (range.beforeStartKey(getSource().getTopKey()))
        consume();
    }
  }
  protected void consume() throws IOException {
    if (lastRowFound == null)
      return;
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
