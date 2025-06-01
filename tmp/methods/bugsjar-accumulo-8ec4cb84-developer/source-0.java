  private void addUpdatesToMutation(HashMap<Text,ColumnVisibility> vizMap, Mutation m, List<ColumnUpdate> cu) {
    for (ColumnUpdate update : cu) {
      ColumnVisibility viz = EMPTY_VIS;
      if (update.isSetColVisibility()) {
        viz = getCahcedCV(vizMap, update.getColVisibility());
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
  }
