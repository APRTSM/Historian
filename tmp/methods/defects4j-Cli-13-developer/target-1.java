    List getUndefaultedValues(final Option option);
    public List getUndefaultedValues(Option option) {
      // First grab the command line values
      List valueList = (List) values.get(option);

      // Finally use an empty list
      if (valueList == null) {
        valueList = Collections.EMPTY_LIST;
      }

      return valueList;
    }
