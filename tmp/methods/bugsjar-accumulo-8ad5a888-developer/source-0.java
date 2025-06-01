  public static String toPrintableString(byte ba[], int offset, int len, int maxLen) {
    StringBuilder sb = new StringBuilder();
    
    int plen = Math.min(len, maxLen);
    
    for (int i = 0; i < plen; i++) {
      int c = 0xff & ba[offset + i];
      if (c >= 32 && c <= 126)
        sb.append((char) c);
      else
        sb.append("%" + String.format("%02x;", c));
    }
    
    if (len > maxLen) {
      sb.append("... TRUNCATED");
    }
    
    return sb.toString();
  }
  public String toStringNoTime() {
    
    String labelString = new ColumnVisibility(colVisibility).toString();
    
    String s = new String(row, 0, row.length) + " " + new String(colFamily, 0, colFamily.length) + ":" + new String(colQualifier, 0, colQualifier.length) + " "
        + labelString;
    return s;
  }
  public String toString() {
    String labelString = new ColumnVisibility(colVisibility).toString();
    
    String s = toPrintableString(row, 0, row.length, Constants.MAX_DATA_TO_PRINT) + " "
        + toPrintableString(colFamily, 0, colFamily.length, Constants.MAX_DATA_TO_PRINT) + ":"
        + toPrintableString(colQualifier, 0, colQualifier.length, Constants.MAX_DATA_TO_PRINT) + " " + labelString + " " + Long.toString(timestamp) + " "
        + deleted;
    return s;
  }
