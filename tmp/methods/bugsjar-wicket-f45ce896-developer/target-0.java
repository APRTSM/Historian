	public void unmount(String path)
	{
		final Url url = Url.parse(path);
		final Request request = createRequest(url);

		for (IRequestMapper mapper : this)
		{
			if (mapper instanceof ICompoundRequestMapper)
			{
				ICompoundRequestMapper inner = (ICompoundRequestMapper) mapper;
				inner.unmount(path);
			}
			else if (mapper.mapRequest(request) != null)
			{
				remove(mapper);
			}
		}
	}
