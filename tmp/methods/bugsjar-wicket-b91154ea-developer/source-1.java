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
