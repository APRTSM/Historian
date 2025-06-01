	public InlineEnclosure(final String id, final String childId)
	{
		super(id, childId);

		// ensure that the Enclosure is ready for ajax updates
		setOutputMarkupPlaceholderTag(true);
		setMarkupId(getId());
	}
	protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException
	{
		// We only need ComponentTags
		if (tag instanceof WicketTag)
		{
			return tag;
		}

		// Has wicket:enclosure attribute?
		String enclosureAttr = getAttribute(tag, null);
		if (enclosureAttr != null)
		{
			if (tag.isOpen())
			{
				// Make sure 'wicket:id' and 'id' are consistent
				String htmlId = tag.getAttribute("id");
				if ((tag.getId() != null) && !Strings.isEmpty(htmlId) &&
					!htmlId.equals(tag.getId()))
				{
					throw new ParseException(
						"Make sure that 'id' and 'wicket:id' are the same if both are provided. Tag:" +
							tag.toString(), tag.getPos());
				}

				// if it doesn't have a wicket-id already, then assign one now.
				if (Strings.isEmpty(tag.getId()))
				{
					if (Strings.isEmpty(htmlId))
					{
						String id = getWicketNamespace() + "_" + INLINE_ENCLOSURE_ID_PREFIX +
							(counter++);
						tag.setId(id);
					}
					else
					{
						tag.setId(htmlId);
					}

					tag.setAutoComponentTag(true);
					tag.setModified(true);
				}

				// Put the enclosure on the stack. The most current one will be on top
				if (enclosures == null)
				{
					enclosures = new Stack<ComponentTag>();
				}
				enclosures.push(tag);
			}
			else
			{
				throw new ParseException(
					"Open-close tags don't make sense for InlineEnclosure. Tag:" + tag.toString(),
					tag.getPos());
			}
		}
		// Are we within an enclosure?
		else if ((enclosures != null) && (enclosures.size() > 0))
		{
			// In case the enclosure tag did not provide a child component id, then assign the
			// first ComponentTag's id found as the controlling child to the enclosure.
			if (tag.isOpen() && (tag.getId() != null) && !(tag instanceof WicketTag) &&
				!tag.isAutoComponentTag())
			{
				for (int i = enclosures.size() - 1; i >= 0; i--)
				{
					ComponentTag lastEnclosure = enclosures.get(i);
					String attr = getAttribute(lastEnclosure, null);
					if (Strings.isEmpty(attr) == true)
					{
						lastEnclosure.getAttributes().put(getInlineEnclosureAttributeName(null),
							tag.getId());
						lastEnclosure.setModified(true);
					}
				}
			}
			else if (tag.isClose() && tag.closes(enclosures.peek()))
			{
				ComponentTag lastEnclosure = enclosures.pop();
				String attr = getAttribute(lastEnclosure, null);
				if (Strings.isEmpty(attr) == true)
				{
					throw new ParseException("Did not find any child for InlineEnclosure. Tag:" +
						lastEnclosure.toString(), tag.getPos());
				}
			}
		}

		return tag;
	}
	public Component resolve(final MarkupContainer container, final MarkupStream markupStream,
		final ComponentTag tag)
	{
		String inlineEnclosureChildId = getAttribute(tag, markupStream);
		if (Strings.isEmpty(inlineEnclosureChildId) == false)
		{
			String id = tag.getId();

			// Yes, we handled the tag
			return new InlineEnclosure(id, inlineEnclosureChildId);
		}

		// We were not able to handle the tag
		return null;
	}
