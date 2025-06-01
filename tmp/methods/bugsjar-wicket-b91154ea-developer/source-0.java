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
