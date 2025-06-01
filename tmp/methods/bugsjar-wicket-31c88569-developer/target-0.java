	private void handleHeadTag(ComponentTag tag)
	{
		// we found <head>
		if (tag.isOpen())
		{
			if(foundHead)
			{
				throw new MarkupException(new MarkupStream(markup),
					"Tag <head> is not allowed at this position (do you have multiple <head> tags in your markup?).");
			}
			
			foundHead = true;

			if (tag.getId() == null)
			{
				tag.setId(HEADER_ID);
				tag.setAutoComponentTag(true);
				tag.setModified(true);
				tag.setAutoComponentFactory(HTML_HEADER_FACTORY);
			}
		}
		else if (tag.isClose())
		{
			if (foundHeaderItemsTag)
			{
				// revert the settings from above
				ComponentTag headOpenTag = tag.getOpenTag();
				// change the id because it is special. See HtmlHeaderResolver
				headOpenTag.setId(HEADER_ID + "-Ignored");
				headOpenTag.setAutoComponentTag(false);
				headOpenTag.setModified(false);
				headOpenTag.setFlag(ComponentTag.RENDER_RAW, true);
				headOpenTag.setAutoComponentFactory(null);
			}

			foundClosingHead = true;
		}
	}
	private void insertHeadTag()
	{
		// Note: only the open-tag must be a AutoComponentTag
		final ComponentTag openTag = new ComponentTag(HEAD, TagType.OPEN);
		openTag.setId(HEADER_ID);
		openTag.setAutoComponentTag(true);
		openTag.setModified(true);
		openTag.setAutoComponentFactory(HTML_HEADER_FACTORY);

		final ComponentTag closeTag = new ComponentTag(HEAD, TagType.CLOSE);
		closeTag.setOpenTag(openTag);
		closeTag.setModified(true);

		// insert the tags into the markup stream
		markup.addMarkupElement(openTag);
		markup.addMarkupElement(closeTag);
	}
	private void handleHeaderItemsTag(ComponentTag tag)
	{
		if (foundHeaderItemsTag)
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
