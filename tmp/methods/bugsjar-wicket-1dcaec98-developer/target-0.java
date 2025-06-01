	public String parse(final String text)
	{
		if (Strings.isEmpty(text))
		{
			return text;
		}

		String work = text;

		// don't try to parse markup. just plain text. WICKET-4099
		if (work.indexOf('<') == -1)
		{
			for (String pattern : renderStrategies.keySet())
			{
				ILinkRenderStrategy strategy = renderStrategies.get(pattern);

				Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(work);
				StringBuffer buffer = new StringBuffer();
				while (matcher.find())
				{
					String str = matcher.group();
					matcher.appendReplacement(buffer, strategy.buildLink(str));
				}
				matcher.appendTail(buffer);
				work = buffer.toString();
			}
		}
		return work;
	}
