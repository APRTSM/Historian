  private void addCellsToWriter(Map<ByteBuffer,List<ColumnUpdate>> cells, BatchWriterPlusException bwpe) {
    if (bwpe.exception != null)
      return;
    
    HashMap<Text,ColumnVisibility> vizMap = new HashMap<Text,ColumnVisibility>();
    
    for (Map.Entry<ByteBuffer,List<ColumnUpdate>> entry : cells.entrySet()) {
      Mutation m = new Mutation(ByteBufferUtil.toBytes(entry.getKey()));
      
      for (ColumnUpdate update : entry.getValue()) {
        ColumnVisibility viz = EMPTY_VIS;
        if (update.isSetColVisibility()) {
          Text vizText = new Text(update.getColVisibility());
          viz = vizMap.get(vizText);
          if (viz == null) {
            vizMap.put(vizText, viz = new ColumnVisibility(vizText));
          }
        }
        byte[] value = new byte[0];
        if (update.isSetValue())
          value = update.getValue();
        if (update.isSetTimestamp()) {
          if (update.isSetDeleteCell()) {
            m.putDelete(update.getColFamily(), update.getColQualifier(), viz, update.getTimestamp());
          } else {
            if (update.isSetDeleteCell()) {
              m.putDelete(update.getColFamily(), update.getColQualifier(), viz, update.getTimestamp());
            } else {
              m.put(update.getColFamily(), update.getColQualifier(), viz, update.getTimestamp(), value);
            }
          }
        } else {
          m.put(update.getColFamily(), update.getColQualifier(), viz, value);
        }
      }
      try {
        bwpe.writer.addMutation(m);
      } catch (MutationsRejectedException mre) {
        bwpe.exception = mre;
      }
    }
  }
