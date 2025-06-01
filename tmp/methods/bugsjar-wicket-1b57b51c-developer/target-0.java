	public final Object getMarkupIdImpl()
	{
		if (generatedMarkupId != -1)
		{
			return generatedMarkupId;
		}

		String id = getMetaData(MARKUP_ID_KEY);

		// if still no markup id is found, and the component has been attached to a page, try to
		// retrieve the id from the markup file.
		if (id == null && findPage() != null)
		{
			id = getMarkupIdFromMarkup();
		}
		return id;
	}
