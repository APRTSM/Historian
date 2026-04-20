	protected Class<? extends IRequestablePage> getPageClass(String name)
	{
		String cleanedClassName = cleanClassName(name);
		return WicketObjects.resolveClass(cleanedClassName);
	}
	protected String cleanClassName(String className)
	{
		Args.notEmpty(className, "className");

		if (Strings.indexOf(className, ';') > -1)
		{
			// remove any path parameters set manually by the user. WICKET-5500
			className = Strings.beforeFirst(className, ';');
		}

		return className;
	}
	protected UrlInfo parseRequest(Request request)
	{
		Url url = request.getUrl();
		if (url.getSegments().size() > mountSegments.length)
		{
			// try to extract page and component information from URL
			PageComponentInfo info = getPageComponentInfo(url);

			// load the page class
			String name = url.getSegments().get(mountSegments.length);
			String className = cleanClassName(name);

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
