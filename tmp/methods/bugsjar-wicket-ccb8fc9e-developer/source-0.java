	public Component resolve(final MarkupContainer container, final MarkupStream markupStream,
		final ComponentTag tag)
	{
		String inlineEnclosureChildId = getInlineEnclosureAttribute(tag);
		if (Strings.isEmpty(inlineEnclosureChildId) == false)
		{
			String id = tag.getId();
			if (id.equals(INLINE_ENCLOSURE_ID_PREFIX))
			{
				id = id + container.getPage().getAutoIndex();
			}
			// Yes, we handled the tag
			return new InlineEnclosure(id, inlineEnclosureChildId);
		}

		// We were not able to handle the tag
		return null;
	}
