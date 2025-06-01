	public InlineEnclosure(final String id, final String childId)
	{
		super(id, childId);

		enclosureMarkupAsString = null;

		// ensure that the Enclosure is ready for ajax updates
		setOutputMarkupPlaceholderTag(true);
		setMarkupId(getId());
	}
	public IMarkupFragment getMarkup()
	{
		IMarkupFragment enclosureMarkup = null;
		if (enclosureMarkupAsString == null)
		{
			IMarkupFragment markup = super.getMarkup();
			if (markup != null && markup != Markup.NO_MARKUP)
			{
				enclosureMarkup = markup;
				enclosureMarkupAsString = markup.toString(true);
			}
		}
		else
		{
			enclosureMarkup = Markup.of(enclosureMarkupAsString, getWicketNamespace());
		}

		return enclosureMarkup;
	}
