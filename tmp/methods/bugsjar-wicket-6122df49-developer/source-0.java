	public CssPackageResource getResource()
	{
		return new CssPackageResource(getScope(), getName(), getLocale(), getStyle(),
			getVariation());
	}
