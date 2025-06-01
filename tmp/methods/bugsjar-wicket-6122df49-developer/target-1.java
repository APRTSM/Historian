	public CssPackageResource getResource()
	{
        final CssPackageResource resource = new CssPackageResource(getScope(), getName(), getLocale(), getStyle(),
                getVariation());
        removeCompressFlagIfUnnecessary(resource);
        return resource;
	}
	public JavaScriptPackageResource getResource()
	{
        final JavaScriptPackageResource resource = new JavaScriptPackageResource(getScope(), getName(), getLocale(), getStyle(),
                getVariation());
        removeCompressFlagIfUnnecessary(resource);
        return resource;
	}
