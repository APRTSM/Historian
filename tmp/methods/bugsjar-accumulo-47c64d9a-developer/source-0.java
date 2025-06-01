  public Map<String,String> getAllPropertiesWithPrefix(ClientProperty property) {
    checkType(property, PropertyType.PREFIX);

    Map<String,String> propMap = new HashMap<String,String>();
    Iterator<?> iter = this.getKeys(property.getKey());
    while (iter.hasNext()) {
      String p = (String) iter.next();
      propMap.put(p, getString(p));
    }
    return propMap;
  }
