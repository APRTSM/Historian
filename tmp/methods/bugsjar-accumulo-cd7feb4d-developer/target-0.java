  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    RegExFilter result = new RegExFilter();
    result.setSource(getSource().deepCopy(env));
    result.rowMatcher = copyMatcher(rowMatcher);
    result.colfMatcher = copyMatcher(colfMatcher);
    result.colqMatcher = copyMatcher(colqMatcher);
    result.valueMatcher = copyMatcher(valueMatcher);
    result.orFields = orFields;
    return result;
  }
  private Matcher copyMatcher(Matcher m)
  {
	  if(m == null)
		  return m;
	  else
		  return m.pattern().matcher("");
  }
