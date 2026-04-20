	private void dequeueAutoComponents()
	{
		// dequeue auto components
		DequeueContext context = newDequeueContext();
		if (context != null && context.peekTag() != null)
		{
			for (ComponentTag tag = context.takeTag(); tag != null; tag = context.takeTag())
			{
				ComponentTag.IAutoComponentFactory autoComponentFactory = tag.getAutoComponentFactory();
				if (autoComponentFactory != null)
				{
					queue(autoComponentFactory.newComponent(tag));
				}
			}
		}
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
						String id = getWicketNamespace() + "_" + INLINE_ENCLOSURE_ID_PREFIX + (counter++);
						tag.setId(id);
					}
					else
					{
						tag.setId(htmlId);
					}

					tag.setAutoComponentTag(true);
					tag.setAutoComponentFactory(new ComponentTag.IAutoComponentFactory()
					{
						@Override
						public Component newComponent(ComponentTag tag)
						{
							String attributeName = getInlineEnclosureAttributeName(null);
							String childId = tag.getAttribute(attributeName);
							return new InlineEnclosure(tag.getId(), childId);
						}
					});
					tag.setModified(true);
				}

				// Put the enclosure on the stack. The most current one will be on top
				if (enclosures == null)
				{
					enclosures = new ArrayDeque<>();
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
				Iterator<ComponentTag> componentTagIterator = enclosures.descendingIterator();
				while (componentTagIterator.hasNext())
				{
					ComponentTag lastEnclosure = componentTagIterator.next();
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
	protected final MarkupElement onComponentTag(ComponentTag tag) throws ParseException
	{
		if (tag.isClose())
		{
			return tag;
		}

		String wicketIdAttr = getWicketNamespace() + ":" + "id";

		// Don't touch any wicket:id component and any auto-components
		if ((tag instanceof WicketTag) || (tag.isAutolinkEnabled() == true) ||
			(tag.getAttributes().get(wicketIdAttr) != null))
		{
			return tag;
		}

		// Work out whether we have any attributes that require us to add a
		// behavior that prepends the relative path.
		for (String attrName : attributeNames)
		{
			String attrValue = tag.getAttributes().getString(attrName);
			if ((attrValue != null) && (attrValue.startsWith("/") == false) &&
				(!attrValue.contains(":")) && !(attrValue.startsWith("#")))
			{
				if (tag.getId() == null)
				{
					tag.setId(getWicketRelativePathPrefix(null));
					tag.setAutoComponentTag(true);
				}
				tag.addBehavior(RELATIVE_PATH_BEHAVIOR);
				tag.setModified(true);
				break;
			}
		}

		return tag;
	}
