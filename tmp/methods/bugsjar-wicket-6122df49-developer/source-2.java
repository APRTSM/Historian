	public CssPackageResource getResource()
	{
		return new CssPackageResource(getScope(), getName(), getLocale(), getStyle(),
			getVariation());
	}
	public JavaScriptPackageResource getResource()
	{
		return new JavaScriptPackageResource(getScope(), getName(), getLocale(), getStyle(),
			getVariation());
	}
	public PackageResource getResource()
	{
		final String extension = getExtension();

		final PackageResource resource;

		if (CSS_EXTENSION.equals(extension))
		{
			resource = new CssPackageResource(getScope(), getName(), getLocale(), getStyle(),
				getVariation());
		}
		else if (JAVASCRIPT_EXTENSION.equals(extension))
		{
			resource = new JavaScriptPackageResource(getScope(), getName(), getLocale(), getStyle(),
				getVariation());
		}
		else
		{
			resource = new PackageResource(getScope(), getName(), getLocale(), getStyle(),
				getVariation());
		}

		String minifiedName = MINIFIED_NAMES_CACHE.get(this);
		if (minifiedName != null && minifiedName != NO_MINIFIED_NAME)
		{
			resource.setCompress(false);
		}

		return resource;
	}
	private ResourceReference.UrlAttributes getUrlAttributes(Locale locale, String style, String variation)
	{
		IResourceStreamLocator locator = Application.get()
			.getResourceSettings()
			.getResourceStreamLocator();

		String absolutePath = Packages.absolutePath(getScope(), getName());

		IResourceStream stream = locator.locate(getScope(), absolutePath, style, variation, locale,
			null, false);

		if (stream == null)
			return new ResourceReference.UrlAttributes(null, null, null);

		return new ResourceReference.UrlAttributes(stream.getLocale(), stream.getStyle(), stream.getVariation());
	}
