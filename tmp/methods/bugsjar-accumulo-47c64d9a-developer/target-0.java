  public Map<String,String> getAllPropertiesWithPrefix(ClientProperty property) {
    checkType(property, PropertyType.PREFIX);

    Map<String,String> propMap = new HashMap<>();
    String prefix = property.getKey();
    if (prefix.endsWith(".")) {
      prefix = prefix.substring(0, prefix.length() - 1);
    }
    Iterator<?> iter = this.getKeys(prefix);
    while (iter.hasNext()) {
      String p = (String) iter.next();
      propMap.put(p, getString(p));
    }
    return propMap;
  }
