	protected Url buildUrl(UrlInfo info)
	{
		Class<? extends IRequestablePage> pageClass = info.getPageClass();
		if (PackageName.forClass(pageClass).equals(packageName))
		{
			Url url = new Url();
			url.getSegments().add(pageClass.getSimpleName());
			encodePageComponentInfo(url, info.getPageComponentInfo());
			return encodePageParameters(url, info.getPageParameters(), pageParametersEncoder);
		}

		return null;
	}
