	public InlineEnclosure(final String id, final String childId)
	{
		super(id, childId);

		// ensure that the Enclosure is ready for ajax updates
		setOutputMarkupPlaceholderTag(true);
		setMarkupId(getId());
	}
