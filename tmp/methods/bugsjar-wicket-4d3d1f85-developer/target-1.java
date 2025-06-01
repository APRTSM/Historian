	public SystemMapper(final Application application)
	{
		this.application = application;
		add(new PageInstanceMapper());
		add(new BookmarkableMapper());
		add(new HomePageMapper(new HomePageProvider<Page>(application)));
		add(new ResourceReferenceMapper(new PageParametersEncoder(),
			new ParentFolderPlaceholderProvider(application), getResourceCachingStrategy()));
		add(RestartResponseAtInterceptPageException.MAPPER);
		add(new BufferedResponseMapper());
	}
