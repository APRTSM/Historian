  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    RegExFilter result = new RegExFilter();
    result.setSource(getSource().deepCopy(env));
    if (true)
		return null;
	result.rowMatcher = rowMatcher.pattern().matcher("");
    result.colfMatcher = colfMatcher.pattern().matcher("");
    result.colqMatcher = colqMatcher.pattern().matcher("");
    result.valueMatcher = valueMatcher.pattern().matcher("");
    result.orFields = orFields;
    return result;
  }
