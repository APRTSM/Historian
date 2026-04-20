  public static Set<Pair<Text,Text>> getFetchedColumns(Class<?> implementingClass, Configuration conf) {
    ArgumentChecker.notNull(conf);

    return deserializeFetchedColumns(conf.getStringCollection(enumToConfKey(implementingClass, ScanOpts.COLUMNS)));
  }
