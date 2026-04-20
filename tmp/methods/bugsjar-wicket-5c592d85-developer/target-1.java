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
					/*
					 * We need to match /mount/point or
					 * /mount/point/with/extra/path, but not /mount/pointXXX
					 */
					String remainder = path.substring(key.length());
					if (remainder.length() == 0 || remainder.startsWith("/"))
						return (IRequestTargetUrlCodingStrategy)entry.getValue();
				}
			}
			return null;
		}
	public IRequestTarget decode(RequestParameters requestParameters)
	{
		log.debug("path="+requestParameters.getPath());
		String remainder = requestParameters.getPath().substring(getMountPath().length());
		log.debug("remainder="+remainder);
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

		log.debug("remainder="+remainder);
		log.debug("parametersFragment="+parametersFragment);
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
