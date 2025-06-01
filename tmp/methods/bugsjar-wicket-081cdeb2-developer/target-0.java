	protected void removeCachingDecoration(Url url, PageParameters parameters)
	{
		final List<String> segments = url.getSegments();

		if (segments.isEmpty() == false)
		{
			// get filename (the last segment)
			final int lastSegmentAt = segments.size() - 1;
			String filename = segments.get(lastSegmentAt);

			// ignore requests with empty filename
			if(Strings.isEmpty(filename))
			{
				return;
			}

			// create resource url from filename and query parameters
			final ResourceUrl resourceUrl = new ResourceUrl(filename, parameters);

			// remove caching information from request
			getCachingStrategy().undecorateUrl(resourceUrl);
			
			// check for broken caching strategy (this must never happen)
			if (Strings.isEmpty(resourceUrl.getFileName()))
			{
				throw new IllegalStateException("caching strategy returned empty name for " + resourceUrl);
			}

			segments.set(lastSegmentAt, resourceUrl.getFileName());
		}
	}
