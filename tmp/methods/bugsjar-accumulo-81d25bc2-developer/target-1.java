  public PreciseNumberType() {}
  public String format(Object obj) {
    int i = (Integer)obj;
    String display = String.format("%,d", obj);
    if (i < errMin || i > errMax)
      return String.format("<span class='error'>%s</span>", display);
    if (i < warnMin || i > warnMax)
      return String.format("<span class='warning'>%s</span>", display);
    return display;
  }
