  public boolean accept(Key k, Value v) {
    Text testVis = k.getColumnVisibility(tmpVis);
    
    if (testVis.getLength() == 0 && defaultVisibility.getLength() == 0)
      return true;
    else if (testVis.getLength() == 0)
      testVis = defaultVisibility;
    
    Boolean b = (Boolean) cache.get(testVis);
    if (b != null)
      return b;
    
    try {
      Boolean bb = ve.evaluate(new ColumnVisibility(testVis));
      cache.put(new Text(testVis), bb);
      return bb;
    } catch (VisibilityParseException e) {
      log.error("Parse Error", e);
      return false;
    } catch (BadArgumentException e) {
      log.error("Parse Error", e);
      return false;
    }
  }
