	public MergedMarkup(final Markup markup, final Markup baseMarkup, int extendIndex)
	{
		super(markup.getMarkupResourceStream());

		getMarkupResourceStream().setBaseMarkup(baseMarkup);

		if (log.isDebugEnabled())
		{
			String derivedResource = Strings.afterLast(markup.getMarkupResourceStream()
				.getResource()
				.toString(), '/');
			String baseResource = Strings.afterLast(baseMarkup.getMarkupResourceStream()
				.getResource()
				.toString(), '/');
			log.debug("Merge markup: derived markup: " + derivedResource + "; base markup: " +
				baseResource);
		}

		// Merge derived and base markup
		merge(markup, baseMarkup, extendIndex);

		if (log.isDebugEnabled())
		{
			log.debug("Merge markup: " + toString());
		}
	}
