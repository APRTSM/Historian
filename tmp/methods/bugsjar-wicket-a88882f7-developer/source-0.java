	public void resolveRelative(final Url relative)
	{
		if (getSegments().size() > 0)
		{
			// strip the first non-folder segment
			getSegments().remove(getSegments().size() - 1);
		}
		// remove all './' (current folder) from the relative url
		if (!relative.getSegments().isEmpty() && ".".equals(relative.getSegments().get(0)))
		{
			relative.getSegments().remove(0);
		}

		// process any ../ segments in the relative url
		while (!relative.getSegments().isEmpty() && "..".equals(relative.getSegments().get(0)))
		{
			relative.getSegments().remove(0);
			getSegments().remove(getSegments().size() - 1);
		}

		// append the remaining relative segments
		getSegments().addAll(relative.getSegments());

		// replace query params with the ones from relative
		parameters.clear();
		parameters.addAll(relative.getQueryParameters());
	}
