		public int hashCode()
		{
			int result = super.hashCode();
			result = 31 * result + (extension != null ? extension.hashCode() : 0);
			return result;
		}
	public IResourceStream locate(Class<?> scope, String path, String style, String variation,
		Locale locale, String extension, boolean strict)
	{
		CacheKey key = new CacheKey(scope.getName(), path, extension, locale, style, variation);
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
		private CacheKey(String scope, String name, String extension, Locale locale, String style, String variation)
		{
			super(scope, name, locale, style, variation);

			this.extension = extension;
		}
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;

			CacheKey cacheKey = (CacheKey) o;

			return !(extension != null ? !extension.equals(cacheKey.extension) : cacheKey.extension != null);

		}
	public IResourceStream locate(Class<?> clazz, String path)
	{
		CacheKey key = new CacheKey(clazz.getName(), path, null, null, null, null);
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
