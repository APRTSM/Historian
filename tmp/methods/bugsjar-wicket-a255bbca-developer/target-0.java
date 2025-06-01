	private void handleHeaderItemsTag(ComponentTag tag)
	{
		if ((tag.isOpen() || tag.isOpenClose()) && foundHeaderItemsTag)
		{
			throw new MarkupException(new MarkupStream(markup),
					"More than one <wicket:header-items/> detected in the <head> element. Only one is allowed.");
		}
		else if (foundClosingHead)
		{
			throw new MarkupException(new MarkupStream(markup),
					"Detected <wicket:header-items/> after the closing </head> element.");
		}

		foundHeaderItemsTag = true;
		tag.setId(HEADER_ID);
		tag.setAutoComponentTag(true);
		tag.setModified(true);
		tag.setAutoComponentFactory(HTML_HEADER_ITEMS_FACTORY);
	}
