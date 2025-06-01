	protected void internalInit()
	{
		settingsAccessible = true;
		IPageSettings pageSettings = getPageSettings();

		// Install default component resolvers
		pageSettings.addComponentResolver(new MarkupInheritanceResolver());
		pageSettings.addComponentResolver(new HtmlHeaderResolver());
		pageSettings.addComponentResolver(new WicketLinkTagHandler());
		pageSettings.addComponentResolver(new WicketMessageResolver());
		pageSettings.addComponentResolver(new WicketMessageTagHandler());
		pageSettings.addComponentResolver(new FragmentResolver());
		pageSettings.addComponentResolver(new RelativePathPrefixHandler());
		pageSettings.addComponentResolver(new EnclosureHandler());
		pageSettings.addComponentResolver(new InlineEnclosureHandler());
		pageSettings.addComponentResolver(new WicketContainerResolver());

		// Install button image resource factory
		getResourceSettings().addResourceFactory("buttonFactory",
			new DefaultButtonImageResourceFactory());

		String applicationKey = getApplicationKey();
		applicationKeyToApplication.put(applicationKey, this);

		converterLocator = newConverterLocator();

		setPageManagerProvider(new DefaultPageManagerProvider(this));
		resourceReferenceRegistry = newResourceReferenceRegistry();
		sharedResources = newSharedResources(resourceReferenceRegistry);
		resourceBundles = newResourceBundles(resourceReferenceRegistry);

		// set up default request mapper
		setRootRequestMapper(new SystemMapper(this));

		pageFactory = newPageFactory();

		requestCycleProvider = new DefaultRequestCycleProvider();
		exceptionMapperProvider = new DefaultExceptionMapperProvider();

		// add a request cycle listener that logs each request for the requestlogger.
		getRequestCycleListeners().add(new RequestLoggerRequestCycleListener());
	}
	protected MarkupFilterList initializeMarkupFilters(final Markup markup)
	{
		// MarkupFilterList is a simple extension of ArrayList providing few additional helpers
		final MarkupFilterList filters = new MarkupFilterList();

		MarkupResourceStream markupResourceStream = markup.getMarkupResourceStream();

		filters.add(new WicketTagIdentifier(markupResourceStream));
		filters.add(new HtmlHandler());
		filters.add(new WicketRemoveTagHandler());
		filters.add(new WicketLinkTagHandler());
		filters.add(new AutoLabelTagHandler());
		filters.add(new WicketNamespaceHandler(markupResourceStream));

		// Provided the wicket component requesting the markup is known ...
		if ((markupResourceStream != null) && (markupResourceStream.getResource() != null))
		{
			final ContainerInfo containerInfo = markupResourceStream.getContainerInfo();
			if (containerInfo != null)
			{
				filters.add(new WicketMessageTagHandler(markupResourceStream));

				// Pages require additional handlers
				if (Page.class.isAssignableFrom(containerInfo.getContainerClass()))
				{
					filters.add(new HtmlHeaderSectionHandler(markup));
				}

				filters.add(new HeadForceTagIdHandler(containerInfo.getContainerClass()));
			}
		}

		filters.add(new OpenCloseTagExpander());
		filters.add(new RelativePathPrefixHandler(markupResourceStream));
		filters.add(new EnclosureHandler());
		filters.add(new InlineEnclosureHandler());

		// Append it. See WICKET-4390
		filters.add(new StyleAndScriptIdentifier(), StyleAndScriptIdentifier.class);
		filters.add(new ConditionalCommentFilter());

		return filters;
	}
	protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException
	{
		// We only need ComponentTags
		if (tag instanceof WicketTag)
		{
			return tag;
		}

		// Has wicket:enclosure attribute?
		String enclosureAttr = getInlineEnclosureAttribute(tag);
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

				// if it doesn't have a wicket-id already, than assign one now.
				if (Strings.isEmpty(tag.getId()))
				{
					if (Strings.isEmpty(htmlId))
					{
						tag.setId(INLINE_ENCLOSURE_ID_PREFIX);
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
			// In case the enclosure tag did not provide a child component id, than assign the
			// first ComponentTag's id found as the controlling child to the enclosure.
			if (tag.isOpen() && (tag.getId() != null) && !(tag instanceof WicketTag) &&
				!tag.isAutoComponentTag())
			{
				for (int i = enclosures.size() - 1; i >= 0; i--)
				{
					ComponentTag lastEnclosure = enclosures.get(i);
					String attr = getInlineEnclosureAttribute(lastEnclosure);
					if (Strings.isEmpty(attr) == true)
					{
						lastEnclosure.getAttributes().put(INLINE_ENCLOSURE_ATTRIBUTE_NAME,
							tag.getId());
						lastEnclosure.setModified(true);
					}
				}
			}
			else if (tag.isClose() && tag.closes(enclosures.peek()))
			{
				ComponentTag lastEnclosure = enclosures.pop();
				String attr = getInlineEnclosureAttribute(lastEnclosure);
				if (Strings.isEmpty(attr) == true)
				{
					throw new ParseException("Did not find any child for InlineEnclosure. Tag:" +
						lastEnclosure.toString(), tag.getPos());
				}
			}
		}

		return tag;
	}
