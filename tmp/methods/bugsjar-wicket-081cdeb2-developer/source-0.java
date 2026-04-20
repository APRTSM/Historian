	protected void removeCachingDecoration(Url url, PageParameters parameters)
	{
		final List<String> segments = url.getSegments();

		if (segments.isEmpty() == false)
		{
			final int lastSegmentAt = segments.size() - 1;
			final ResourceUrl resourceUrl = new ResourceUrl(segments.get(lastSegmentAt), parameters);

			getCachingStrategy().undecorateUrl(resourceUrl);
			
			if (Strings.isEmpty(resourceUrl.getFileName()))
			{
				throw new IllegalStateException("caching strategy returned empty name for " + resourceUrl);
			}

			segments.set(lastSegmentAt, resourceUrl.getFileName());
		}
	}
