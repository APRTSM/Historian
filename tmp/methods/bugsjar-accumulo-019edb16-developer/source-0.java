    protected void setupIterators(JobConf job, Scanner scanner, String tableName, org.apache.accumulo.core.client.mapred.RangeInputSplit split) {
      List<IteratorSetting> iterators = null;

      if (null == split) {
        iterators = getIterators(job);
      } else {
        iterators = split.getIterators();
      }

      setupIterators(iterators, scanner);
    }
