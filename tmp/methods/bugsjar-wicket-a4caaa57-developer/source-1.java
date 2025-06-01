	public final Object put(String key, Object value)
	{

		return super.put(key, unescapeHtml(value));
	}
	private boolean parseTagText(final XmlTag tag, final String tagText) throws ParseException
	{
		// Get the length of the tagtext
		final int tagTextLength = tagText.length();

		// If we match tagname pattern
		final TagNameParser tagnameParser = new TagNameParser(tagText);
		if (tagnameParser.matcher().lookingAt())
		{
			// Extract the tag from the pattern matcher
			tag.name = tagnameParser.getName();
			tag.namespace = tagnameParser.getNamespace();

			// Are we at the end? Then there are no attributes, so we just
			// return the tag
			int pos = tagnameParser.matcher().end(0);
			if (pos == tagTextLength)
			{
				return true;
			}

			// Extract attributes
			final VariableAssignmentParser attributeParser = new VariableAssignmentParser(tagText);
			while (attributeParser.matcher().find(pos))
			{
				// Get key and value using attribute pattern
				String value = attributeParser.getValue();

				// In case like <html xmlns:wicket> will the value be null
				if (value == null)
				{
					value = "";
				}

				// Set new position to end of attribute
				pos = attributeParser.matcher().end(0);

				// Chop off double quotes or single quotes
				if (value.startsWith("\"") || value.startsWith("\'"))
				{
					value = value.substring(1, value.length() - 1);
				}

				// Trim trailing whitespace
				value = value.trim();

				// Get key
				final String key = attributeParser.getKey();

				// Put the attribute in the attributes hash
				if (null != tag.getAttributes().put(key, value))
				{
					throw new ParseException("Same attribute found twice: " + key +
						getLineAndColumnText(), input.getPosition());
				}

				// The input has to match exactly (no left over junk after
				// attributes)
				if (pos == tagTextLength)
				{
					return true;
				}
			}

			return true;
		}

		return false;
	}
