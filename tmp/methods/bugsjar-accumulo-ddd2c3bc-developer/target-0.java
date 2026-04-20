  public void readFields(DataInput dataInput) throws IOException {
    // load iterators
    long iterSize = dataInput.readInt();
    if (iterSize > 0)
      iterators = new ArrayList<IteratorSetting>();
    for (int i = 0; i < iterSize; i++)
      iterators.add(new IteratorSetting(dataInput));
    // load ranges
    long rangeSize = dataInput.readInt();
    if (rangeSize > 0)
      ranges = new ArrayList<Range>();
    for (int i = 0; i < rangeSize; i++) {
      Range range = new Range();
      range.readFields(dataInput);
      ranges.add(range);
    }
    // load columns
    long columnSize = dataInput.readInt();
    if (columnSize > 0)
      columns = new HashSet<Pair<Text,Text>>();
    for (int i = 0; i < columnSize; i++) {
      long numPairs = dataInput.readInt();
      Text colFam = new Text();
      colFam.readFields(dataInput);
      if (numPairs == 1) {
        columns.add(new Pair<Text,Text>(colFam, null));
      } else if (numPairs == 2) {
        Text colQual = new Text();
        colQual.readFields(dataInput);
        columns.add(new Pair<Text,Text>(colFam, colQual));
      }
    }
    autoAdjustRanges = dataInput.readBoolean();
    useLocalIterators = dataInput.readBoolean();
    useIsolatedScanners = dataInput.readBoolean();
    offlineScan = dataInput.readBoolean();
  }
  public void write(DataOutput dataOutput) throws IOException {
    if (iterators != null) {
      dataOutput.writeInt(iterators.size());
      for (IteratorSetting setting : iterators)
        setting.write(dataOutput);
    } else {
      dataOutput.writeInt(0);
    }
    if (ranges != null) {
      dataOutput.writeInt(ranges.size());
      for (Range range : ranges)
        range.write(dataOutput);
    } else {
      dataOutput.writeInt(0);
    }
    if (columns != null) {
      dataOutput.writeInt(columns.size());
      for (Pair<Text,Text> column : columns) {
        if (column.getSecond() == null) {
          dataOutput.writeInt(1);
          column.getFirst().write(dataOutput);
        } else {
          dataOutput.writeInt(2);
          column.getFirst().write(dataOutput);
          column.getSecond().write(dataOutput);
        }
      }
    } else {
      dataOutput.writeInt(0);
    }
    dataOutput.writeBoolean(autoAdjustRanges);
    dataOutput.writeBoolean(useLocalIterators);
    dataOutput.writeBoolean(useIsolatedScanners);
    dataOutput.writeBoolean(offlineScan);
  }
