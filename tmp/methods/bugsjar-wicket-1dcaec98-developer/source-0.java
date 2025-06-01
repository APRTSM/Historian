	public String parse(final String text)
	{
		if (Strings.isEmpty(text))
		{
			return text;
		}

		String work = text;
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
		return work;
	}
