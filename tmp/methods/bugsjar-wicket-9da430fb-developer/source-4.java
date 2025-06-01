	protected ValueMap decodeParameters(String urlFragment, Map<String,Object> urlParameters)
	{
		// Hack off any leading slash
		if (urlFragment.startsWith("/"))
		{
			urlFragment = urlFragment.substring(1);
		}
		// Hack off any trailing slash
		if (urlFragment.length() > 0 && urlFragment.endsWith("/"))
		{
			urlFragment = urlFragment.substring(0, urlFragment.length() - 1);
		}

		if (urlFragment.length() == 0)
		{
			return new ValueMap(urlParameters != null ? urlParameters : Collections.EMPTY_MAP);
		}

		// Split into pairs
		final String[] pairs = urlFragment.split("/");

		// If we don't have an even number of pairs
		if (pairs.length % 2 != 0)
		{
			log.warn("URL fragment has unmatched key/value pairs, responding with 404. Fragment: " +
				urlFragment);
			throw new AbortWithWebErrorCodeException(404);
		}

		// Loop through pairs

		ValueMap parameters = new ValueMap();
		for (int i = 0; i < pairs.length; i += 2)
		{
			String value = pairs[i + 1];
			value = urlDecodePathComponent(value);
			parameters.add(pairs[i], value);
		}


		if (urlParameters != null)
		{
			parameters.putAll(urlParameters);
		}

		return parameters;
	}
	protected void appendParameters(AppendingStringBuffer url, Map<?,?> parameters)
	{
		if (parameters != null && parameters.size() > 0)
		{
			for (Entry<?, ?> entry1 : parameters.entrySet())
			{
				Object value = ((Entry<?, ?>) entry1).getValue();
				if (value != null)
				{
					if (value instanceof String[])
					{
						String[] values = (String[]) value;
						for (String value1 : values)
						{
							appendValue(url, ((Entry<?, ?>) entry1).getKey().toString(), value1);
						}
					} else
					{
						appendValue(url, ((Entry<?, ?>) entry1).getKey().toString(), value.toString());
					}
				}
			}
		}
	}
	private void appendValue(AppendingStringBuffer url, String key, String value)
	{
		String escapedValue = urlEncodePathComponent(value);
		if (!Strings.isEmpty(escapedValue))
		{
			if (!url.endsWith("/"))
			{
				url.append("/");
			}
			url.append(key).append("/").append(escapedValue).append("/");
		}
	}
	protected ValueMap decodeParameters(String urlFragment, Map urlParameters)
	{
		PageParameters params = new PageParameters();
		if (urlFragment == null)
		{
			return params;
		}
		if (urlFragment.startsWith("/"))
		{
			urlFragment = urlFragment.substring(1);
		}
		if (urlFragment.length() > 0 && urlFragment.endsWith("/"))
		{
			urlFragment = urlFragment.substring(0, urlFragment.length() - 1);
		}

		String[] parts = urlFragment.split("/");
		for (int i = 0; i < parts.length; i++)
		{
			if (WebRequestCodingStrategy.PAGEMAP.equals(parts[i]))
			{
				i++;
				params.put(WebRequestCodingStrategy.PAGEMAP, WebRequestCodingStrategy
						.decodePageMapName(urlDecodePathComponent(parts[i])));
			}
			else
			{
				params.put(String.valueOf(i), urlDecodePathComponent(parts[i]));
			}
		}
		return params;
	}
	protected void appendParameters(AppendingStringBuffer url, Map parameters)
	{
		int i = 0;
		while (parameters.containsKey(String.valueOf(i)))
		{
			String value = (String)parameters.get(String.valueOf(i));
			if (!url.endsWith("/"))
			{
				url.append("/");
			}
			url.append(urlEncodePathComponent(value)).append("/");
			i++;
		}

		String pageMap = (String)parameters.get(WebRequestCodingStrategy.PAGEMAP);
		if (pageMap != null)
		{
			i++;
			pageMap = WebRequestCodingStrategy.encodePageMapName(pageMap);
			if (!url.endsWith("/"))
			{
				url.append("/");
			}
			url.append(WebRequestCodingStrategy.PAGEMAP).append("/").append(urlEncodePathComponent(pageMap))
					.append("/");
		}

		if (i != parameters.size())
		{
			throw new WicketRuntimeException(
					"Not all parameters were encoded. Make sure all parameter names are integers in consecutive order starting with zero. Current parameter names are: " +
							parameters.keySet().toString());
		}
	}
	protected void appendParameters(AppendingStringBuffer url, Map parameters)
	{
		int i = 0;
		while (parameters.containsKey(String.valueOf(i)))
		{
			String value = null;
			Object parameter = parameters.get(String.valueOf(i));
			if (parameter instanceof String[] && ((String[])parameter).length > 0)
			{
				value = ((String[])parameter)[0];
			}
			else
			{
				value = parameter.toString();
			}

			if (!url.endsWith("/"))
			{
				url.append("/");
			}
			url.append(urlEncodePathComponent(value)).append("/");
			i++;
		}

		String pageMap = (String)parameters.get(WebRequestCodingStrategy.PAGEMAP);
		if (pageMap != null)
		{
			i++;
			pageMap = WebRequestCodingStrategy.encodePageMapName(pageMap);
			if (!url.endsWith("/"))
			{
				url.append("/");
			}
			url.append(WebRequestCodingStrategy.PAGEMAP).append("/").append(
				urlEncodePathComponent(pageMap)).append("/");
		}

		String intface = (String)parameters.get(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME);
		if (intface != null)
		{
			i++;
			if (!url.endsWith("/"))
			{
				url.append("/");
			}
			url.append(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME).append("/").append(
				urlEncodePathComponent(intface)).append("/");
		}
		if (i != parameters.size())
		{
			throw new WicketRuntimeException(
				"Not all parameters were encoded. Make sure all parameter names are integers in consecutive order starting with zero. Current parameter names are: " +
					parameters.keySet().toString());
		}
	}
	protected void appendParameters(AppendingStringBuffer url, Map parameters)
	{
		if (!url.endsWith("/"))
		{
			url.append("/");
		}

		Set parameterNamesToAdd = new HashSet(parameters.keySet());
		// Find index of last specified parameter
		boolean foundParameter = false;
		int lastSpecifiedParameter = parameterNames.length;
		while (lastSpecifiedParameter != 0 && !foundParameter)
		{
			foundParameter = parameters.containsKey(parameterNames[--lastSpecifiedParameter]);
		}

		if (foundParameter)
		{
			for (int i = 0; i <= lastSpecifiedParameter; i++)
			{
				String parameterName = parameterNames[i];
				final Object param = parameters.get(parameterName);
				String value = param instanceof String[] ? ((String[])param)[0] : (String)param;
				if (value == null)
				{
					value = "";
				}
				url.append(urlEncodePathComponent(value)).append("/");
				parameterNamesToAdd.remove(parameterName);
			}
		}

		if (!parameterNamesToAdd.isEmpty())
		{
			boolean first = true;
			final Iterator iterator = parameterNamesToAdd.iterator();
			while (iterator.hasNext())
			{
				url.append(first ? '?' : '&');
				String parameterName = (String)iterator.next();
				final Object param = parameters.get(parameterName);
				String value = param instanceof String[] ? ((String[])param)[0] : (String)param;
				url.append(urlEncodeQueryComponent(parameterName)).append("=").append(
					urlEncodeQueryComponent(value));
				first = false;
			}
		}
	}
	public final CharSequence encode(IRequestTarget requestTarget)
	{
		if (!(requestTarget instanceof IBookmarkablePageRequestTarget))
		{
			throw new IllegalArgumentException("this encoder can only be used with instances of " +
					IBookmarkablePageRequestTarget.class.getName());
		}
		AppendingStringBuffer url = new AppendingStringBuffer(40);
		url.append(getMountPath());
		IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget)requestTarget;
		url.append("/").append(Classes.simpleName(target.getPageClass())).append("/");

		PageParameters pageParameters = target.getPageParameters();
		if (target.getPageMapName() != null)
		{
			pageParameters.put(WebRequestCodingStrategy.PAGEMAP, WebRequestCodingStrategy
					.encodePageMapName(target.getPageMapName()));
		}

		appendParameters(url, pageParameters);
		return url;
	}
	public IRequestTarget decode(RequestParameters requestParameters)
	{
		String remainder = requestParameters.getPath().substring(getMountPath().length());
		final String parametersFragment;
		int ix = remainder.indexOf('/', 1);
		if (ix == -1)
		{
			ix = remainder.length();
			parametersFragment = "";
		}
		else
		{
			parametersFragment = remainder.substring(ix);
		}

		if (remainder.startsWith("/"))
		{
			remainder = remainder.substring(1);
			ix--;
		}
		else
		{
			// There is nothing after the mount path!
			return null;
		}

		final String bookmarkablePageClassName = packageName + "." + remainder.substring(0, ix);
		Class bookmarkablePageClass;
		try
		{
			bookmarkablePageClass = Session.get().getClassResolver().resolveClass(
					bookmarkablePageClassName);
		}
		catch (Exception e)
		{
			log.debug(e.getMessage());
			return null;
		}
		PageParameters parameters = new PageParameters(decodeParameters(parametersFragment,
				requestParameters.getParameters()));

		String pageMapName = (String)parameters.remove(WebRequestCodingStrategy.PAGEMAP);
		pageMapName = WebRequestCodingStrategy.decodePageMapName(pageMapName);
		requestParameters.setPageMapName(pageMapName);

		// do some extra work for checking whether this is a normal request to a
		// bookmarkable page, or a request to a stateless page (in which case a
		// wicket:interface parameter should be available
		final String interfaceParameter = (String)parameters
				.remove(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME);

		if (interfaceParameter != null)
		{
			WebRequestCodingStrategy.addInterfaceParameters(interfaceParameter, requestParameters);
			return new BookmarkableListenerInterfaceRequestTarget(pageMapName,
					bookmarkablePageClass, parameters, requestParameters.getComponentPath(),
					requestParameters.getInterfaceName(), requestParameters.getVersionNumber());
		}
		else
		{
			return new BookmarkablePageRequestTarget(pageMapName, bookmarkablePageClass, parameters);
		}
	}
