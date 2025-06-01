	public final Object getMarkupIdImpl()
	{
		String id = getMarkupIdFromMarkup();
		if (id != null)
		{
			return id;
		}

		if (generatedMarkupId != -1)
		{
			return generatedMarkupId;
		}

		return getMetaData(MARKUP_ID_KEY);
	}
