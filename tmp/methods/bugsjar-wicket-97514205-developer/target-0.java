	protected Url buildUrl(UrlInfo info)
	{
		Class<? extends IRequestablePage> pageClass = info.getPageClass();
		PackageName pageClassPackageName = PackageName.forClass(pageClass);
		if (pageClassPackageName.equals(packageName))
		{
			Url url = new Url();

			String fullyQualifiedClassName = pageClass.getName();
			String packageRelativeClassName = fullyQualifiedClassName;
			int packageNameLength = packageName.getName().length();
			if (packageNameLength > 0)
			{
				packageRelativeClassName = fullyQualifiedClassName.substring(packageNameLength + 1);
			}
			url.getSegments().add(packageRelativeClassName);
			encodePageComponentInfo(url, info.getPageComponentInfo());
			return encodePageParameters(url, info.getPageParameters(), pageParametersEncoder);
		}

		return null;
	}
