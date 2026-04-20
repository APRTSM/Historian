		public IRequestTargetUrlCodingStrategy strategyForPath(String path)
		{
			if (path == null)
			{
				throw new IllegalArgumentException("Argument [[path]] cannot be null");
			}
			if (caseSensitiveMounts == false)
			{
				path = path.toLowerCase();
			}
			for (final Iterator it = map.entrySet().iterator(); it.hasNext();)
			{
				final Map.Entry entry = (Entry)it.next();
				final String key = (String)entry.getKey();
				if (path.startsWith(key))
				{
					return (IRequestTargetUrlCodingStrategy)entry.getValue();
				}
			}
			return null;
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

		final String bookmarkablePageClassName = packageName + "." + remainder.substring(0, ix);
		Class bookmarkablePageClass = Session.get().getClassResolver().resolveClass(
				bookmarkablePageClassName);
		PageParameters parameters = new PageParameters(decodeParameters(parametersFragment,
				requestParameters.getParameters()));

		final String pageMapName = (String)parameters.remove(WebRequestCodingStrategy.PAGEMAP);
		requestParameters.setPageMapName(pageMapName);

		BookmarkablePageRequestTarget target = new BookmarkablePageRequestTarget(pageMapName,
				bookmarkablePageClass, parameters);
		return target;
	}
