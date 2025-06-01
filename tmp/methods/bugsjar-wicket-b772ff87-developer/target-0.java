	protected String newValue(String currentValue, String appendValue)
	{
		// Short circuit when one of the values is empty: return the other value.
		if (Strings.isEmpty(currentValue))
			return appendValue != null ? appendValue : null;
		else if (Strings.isEmpty(appendValue))
			return currentValue != null ? currentValue : null;

		StringBuilder sb = new StringBuilder(currentValue);
		sb.append((getSeparator() == null ? "" : getSeparator()));
		sb.append(appendValue);
		return sb.toString();
	}
