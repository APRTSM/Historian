		public UrlResourceStream getReference()
		{
			try
			{
				return new UrlResourceStream(new URL(url));
			}
			catch (MalformedURLException e)
			{
				// should not ever happen. The cached url is created by previously existing URL
				// instance
				throw new WicketRuntimeException(e);
			}
		}
	public IResourceStream locate(Class<?> clazz, String path)
	{
		Key key = new Key(clazz.getName(), path, null, null, null);
		IResourceStreamReference resourceStreamReference = cache.get(key);

		final IResourceStream result;
		if (resourceStreamReference == null)
		{
			result = delegate.locate(clazz, path);

			updateCache(key, result);
		}
		else
		{
			result = resourceStreamReference.getReference();
		}

		return result;
	}
		public FileResourceStream getReference()
		{
			return new FileResourceStream(new File(fileName));
		}
		public IResourceStream getReference()
		{
			return null;
		}
	public IResourceStream locate(Class<?> scope, String path, String style, String variation,
		Locale locale, String extension, boolean strict)
	{
		Key key = new Key(scope.getName(), path, locale, style, variation);
		IResourceStreamReference resourceStreamReference = cache.get(key);

		final IResourceStream result;
		if (resourceStreamReference == null)
		{
			result = delegate.locate(scope, path, style, variation, locale, extension, strict);

			updateCache(key, result);
		}
		else
		{
			result = resourceStreamReference.getReference();
		}

		return result;
	}
