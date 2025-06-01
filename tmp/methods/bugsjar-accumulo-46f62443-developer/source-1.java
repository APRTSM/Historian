  public Set<String> listIterators(String tableName) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
    Set<String> result = new HashSet<String>();
    Set<String> lifecycles = new HashSet<String>();
    for (IteratorScope scope : IteratorScope.values())
      lifecycles.add(scope.name().toLowerCase());
    for (Entry<String,String> property : this.getProperties(tableName)) {
      String name = property.getKey();
      String[] parts = name.split("\\.");
      if (parts.length == 4) {
        if (parts[0].equals("table") && parts[1].equals("iterator") && lifecycles.contains(parts[2]))
          result.add(parts[3]);
      }
    }
    return result;
  }
  public void checkIteratorConflicts(String tableName, IteratorSetting setting) throws AccumuloException, TableNotFoundException {
    for (IteratorScope scope : setting.getScopes()) {
      String scopeStr = String.format("%s%s", Property.TABLE_ITERATOR_PREFIX, scope.name().toLowerCase());
      String nameStr = String.format("%s.%s", scopeStr, setting.getName());
      String optStr = String.format("%s.opt.", nameStr);
      Map<String,String> optionConflicts = new TreeMap<String,String>();
      for (Entry<String,String> property : this.getProperties(tableName)) {
        if (property.getKey().startsWith(scopeStr)) {
          if (property.getKey().equals(nameStr))
            throw new IllegalArgumentException("iterator name conflict for " + setting.getName() + ": " + property.getKey() + "=" + property.getValue());
          if (property.getKey().startsWith(optStr))
            optionConflicts.put(property.getKey(), property.getValue());
          if (property.getKey().contains(".opt."))
            continue;
          String parts[] = property.getValue().split(",");
          if (parts.length != 2)
            throw new AccumuloException("Bad value for existing iterator setting: " + property.getKey() + "=" + property.getValue());
          try {
            if (Integer.parseInt(parts[0]) == setting.getPriority())
              throw new IllegalArgumentException("iterator priority conflict: " + property.getKey() + "=" + property.getValue());
          } catch (NumberFormatException e) {
            throw new AccumuloException("Bad value for existing iterator setting: " + property.getKey() + "=" + property.getValue());
          }
        }
      }
      if (optionConflicts.size() > 0)
        throw new IllegalArgumentException("iterator options conflict for " + setting.getName() + ": " + optionConflicts);
    }
  }
  public void removeIterator(String tableName, String name, EnumSet<IteratorScope> scopes) throws AccumuloSecurityException, AccumuloException,
      TableNotFoundException {
    Map<String,String> copy = new TreeMap<String,String>();
    for (Entry<String,String> property : this.getProperties(tableName)) {
      copy.put(property.getKey(), property.getValue());
    }
    for (IteratorScope scope : scopes) {
      String root = String.format("%s%s.%s", Property.TABLE_ITERATOR_PREFIX, scope.name().toLowerCase(), name);
      for (Entry<String,String> property : copy.entrySet()) {
        if (property.getKey().equals(root) || property.getKey().startsWith(root + ".opt."))
          this.removeProperty(tableName, property.getKey());
      }
    }
  }
  public IteratorSetting getIteratorSetting(String tableName, String name, IteratorScope scope) throws AccumuloSecurityException, AccumuloException,
      TableNotFoundException {
    int priority = -1;
    String classname = null;
    Map<String,String> settings = new HashMap<String,String>();
    
    String root = String.format("%s%s.%s", Property.TABLE_ITERATOR_PREFIX, scope.name().toLowerCase(), name);
    String opt = root + ".opt.";
    for (Entry<String,String> property : this.getProperties(tableName)) {
      if (property.getKey().equals(root)) {
        String parts[] = property.getValue().split(",");
        if (parts.length != 2) {
          throw new AccumuloException("Bad value for iterator setting: " + property.getValue());
        }
        priority = Integer.parseInt(parts[0]);
        classname = parts[1];
      } else if (property.getKey().startsWith(opt)) {
        settings.put(property.getKey().substring(opt.length()), property.getValue());
      }
    }
    if (priority <= 0 || classname == null) {
      return null;
    }
    return new IteratorSetting(priority, name, classname, EnumSet.of(scope), settings);
  }
  public void clearLocatorCache(String tableName) throws TableNotFoundException {}
  public void setLocalityGroups(String tableName, Map<String,Set<Text>> groups) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {}
  public void create(String tableName, boolean versioningIter, TimeType timeType) throws AccumuloException, AccumuloSecurityException, TableExistsException {
    if (!tableName.matches(Constants.VALID_TABLE_NAME_REGEX)) {
      throw new IllegalArgumentException();
    }
    acu.createTable(username, tableName, versioningIter, timeType);
  }
  public Collection<Text> getSplits(String tableName, int maxSplits) {
    return Collections.emptyList();
  }
  public Map<String,Set<Text>> getLocalityGroups(String tableName) throws AccumuloException, TableNotFoundException {
    return null;
  }
  public void addSplits(String tableName, SortedSet<Text> partitionKeys) throws TableNotFoundException, AccumuloException, AccumuloSecurityException {}
  public Collection<Text> getSplits(String tableName) {
    return Collections.emptyList();
  }
  public void offline(String tableName) throws AccumuloSecurityException, AccumuloException {}
