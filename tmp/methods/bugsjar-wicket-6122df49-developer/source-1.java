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
