  public static String toPrintableString(byte ba[], int offset, int len, int maxLen) {
    return appendPrintableString(ba, offset, len, maxLen, new StringBuilder()).toString();
  }
  public static StringBuilder appendPrintableString(byte ba[], int offset, int len, int maxLen, StringBuilder sb) {
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
    
    return sb;
  }
  public String toString() {
    StringBuilder sb = rowColumnStringBuilder();
    sb.append(" ");
    sb.append(Long.toString(timestamp));
    sb.append(" ");
    sb.append(deleted);
    return sb.toString();
  }
  public String toStringNoTime() {
    return rowColumnStringBuilder().toString();
  }
  private StringBuilder rowColumnStringBuilder() {
    StringBuilder sb = new StringBuilder();
    appendPrintableString(row, 0, row.length, Constants.MAX_DATA_TO_PRINT, sb);
    sb.append(" ");
    appendPrintableString(colFamily, 0, colFamily.length, Constants.MAX_DATA_TO_PRINT, sb);
    sb.append(":");
    appendPrintableString(colQualifier, 0, colQualifier.length, Constants.MAX_DATA_TO_PRINT, sb);
    sb.append(" [");
    appendPrintableString(colVisibility, 0, colVisibility.length, Constants.MAX_DATA_TO_PRINT, sb);
    sb.append("]");
    return sb;
  }
