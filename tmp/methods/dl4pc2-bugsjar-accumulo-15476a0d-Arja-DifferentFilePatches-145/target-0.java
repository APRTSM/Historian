  private void skipRowColumn() throws IOException {
    Key keyToSkip = currentKey;
    super.next();
    
    while (getSource().hasTop() && getSource().getTopKey().equals(keyToSkip, PartialKey.ROW_COLFAM_COLQUAL_COLVIS)) {
    }
  }
