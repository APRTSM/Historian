  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    RegExFilter result = new RegExFilter();
    if (true)
		return null;
	result.setSource(getSource().deepCopy(env));
    result.rowMatcher = rowMatcher.pattern().matcher("");
    result.colfMatcher = colfMatcher.pattern().matcher("");
    result.colqMatcher = colqMatcher.pattern().matcher("");
    result.valueMatcher = valueMatcher.pattern().matcher("");
    result.orFields = orFields;
    return result;
  }
