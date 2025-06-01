  public static Set<Pair<Text,Text>> getFetchedColumns(Class<?> implementingClass, Configuration conf) {
    ArgumentChecker.notNull(conf);
    String confValue = conf.get(enumToConfKey(implementingClass, ScanOpts.COLUMNS));
    List<String> serialized = new ArrayList<String>();
    if (confValue != null) {
      // Split and include any trailing empty strings to allow empty column families
      for (String val : confValue.split(",", -1)) {
        serialized.add(val);
      }
    }
    return deserializeFetchedColumns(serialized);
  }
