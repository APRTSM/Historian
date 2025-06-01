	protected Class<? extends IRequestablePage> getPageClass(String name)
	{
		Args.notEmpty(name, "name");

		return WicketObjects.resolveClass(name);
	}
	protected UrlInfo parseRequest(Request request)
	{
		Url url = request.getUrl();
		if (url.getSegments().size() > mountSegments.length)
		{
			// try to extract page and component information from URL
			PageComponentInfo info = getPageComponentInfo(url);

			// load the page class
			String className = url.getSegments().get(mountSegments.length);

			if (isValidClassName(className) == false)
			{
				return null;
			}

			className = transformFromUrl(className);
			String fullyQualifiedClassName = packageName.getName() + '.' + className;
			Class<? extends IRequestablePage> pageClass = getPageClass(fullyQualifiedClassName);

			if (pageClass != null && Modifier.isAbstract(pageClass.getModifiers()) == false &&
				IRequestablePage.class.isAssignableFrom(pageClass))
			{
				// extract the PageParameters from URL if there are any
				Url urlWithoutPageSegment = new Url(url);
				urlWithoutPageSegment.getSegments().remove(mountSegments.length);
				Request requestWithoutPageSegment = request.cloneWithUrl(urlWithoutPageSegment);
				PageParameters pageParameters = extractPageParameters(requestWithoutPageSegment, urlWithoutPageSegment);

				return new UrlInfo(info, pageClass, pageParameters);
			}
		}
		return null;
	}
