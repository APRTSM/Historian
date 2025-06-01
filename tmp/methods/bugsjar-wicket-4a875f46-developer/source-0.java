	private IResourceStream getCopyFromCache(Key key)
	{
		final IResourceStreamReference orig = cache.get(key);
		if (NullResourceStreamReference.INSTANCE == orig)
		{
			return null;
		}

		if (orig instanceof UrlResourceStreamReference)
		{
			UrlResourceStreamReference resourceStreamReference = (UrlResourceStreamReference)orig;
			String url = resourceStreamReference.getReference();
			try
			{
				return new UrlResourceStream(new URL(url));
			}
			catch (MalformedURLException e)
			{
				return null;
			}
		}

		if (orig instanceof FileResourceStreamReference)
		{
			FileResourceStreamReference resourceStreamReference = (FileResourceStreamReference)orig;
			String absolutePath = resourceStreamReference.getReference();
			return new FileResourceStream(new File(absolutePath));
		}

		return null;
	}
	public IResourceStream locate(Class<?> clazz, String path)
	{
		Key key = new Key(clazz.getName(), path, null, null, null);
		IResourceStream resourceStream = getCopyFromCache(key);

		if (resourceStream == null)
		{
			resourceStream = delegate.locate(clazz, path);

			updateCache(key, resourceStream);
		}

		return resourceStream;
	}
		public String getReference()
		{
			return fileName;
		}
		public String getReference()
		{
			return null;
		}
		public String getReference()
		{
			return url;
		}
	public IResourceStream locate(Class<?> scope, String path, String style, String variation,
		Locale locale, String extension, boolean strict)
	{
		Key key = new Key(scope.getName(), path, locale, style, variation);
		IResourceStream resourceStream = getCopyFromCache(key);

		if (resourceStream == null)
		{
			resourceStream = delegate.locate(scope, path, style, variation, locale, extension,
				strict);

			updateCache(key, resourceStream);
		}

		return resourceStream;
	}
