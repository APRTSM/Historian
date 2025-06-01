		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey)obj;
			if (extension == null)
			{
				if (other.extension != null)
					return false;
			}
			else if (!extension.equals(other.extension))
				return false;
			if (strict != other.strict)
				return false;
			return true;
		}
	public IResourceStream locate(Class<?> scope, String path, String style, String variation,
		Locale locale, String extension, boolean strict)
	{
		CacheKey key = new CacheKey(scope.getName(), path, extension, locale, style, variation, strict);
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
		public int hashCode()
		{
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((extension == null) ? 0 : extension.hashCode());
			result = prime * result + (strict ? 1231 : 1237);
			return result;
		}
		private CacheKey(String scope, String name, String extension, Locale locale, String style, String variation, boolean strict)
		{
			super(scope, name, locale, style, variation);

			this.extension = extension;
			this.strict = strict;
		}
	public IResourceStream locate(Class<?> clazz, String path)
	{
		CacheKey key = new CacheKey(clazz.getName(), path, null, null, null, null, true);
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
