	public Url(final Url url)
	{
		Args.notNull(url, "url");

		protocol = url.protocol;
		host = url.host;
		port = url.port;
		segments = new ArrayList<String>(url.segments);
		parameters = new ArrayList<QueryParameter>(url.parameters);
		charsetName = url.charsetName;
		_charset = url._charset;
	}
	public void resolveRelative(final Url relative)
	{
		if (getSegments().size() > 0)
		{
			// strip the first non-folder segment (if it is not empty)
			getSegments().remove(getSegments().size() - 1);
		}

		// remove leading './' (current folder) and empty segments, process any ../ segments from
		// the relative url
		while (!relative.getSegments().isEmpty())
		{
			if (".".equals(relative.getSegments().get(0)))
			{
				relative.getSegments().remove(0);
			}
			else if ("".equals(relative.getSegments().get(0)))
			{
				relative.getSegments().remove(0);
			}
			else if ("..".equals(relative.getSegments().get(0)))
			{
				relative.getSegments().remove(0);
				if (getSegments().isEmpty() == false)
				{
					getSegments().remove(getSegments().size() - 1);
				}
			}
			else
			{
				break;
			}
		}

		if (!getSegments().isEmpty() && relative.getSegments().isEmpty())
		{
			getSegments().add("");
		}

		// append the remaining relative segments
		getSegments().addAll(relative.getSegments());

		// replace query params with the ones from relative
		parameters.clear();
		parameters.addAll(relative.getQueryParameters());
	}
	public Url canonical()
	{
		Url url = new Url(this);
		url.segments.clear();

		for (int i = 0; i < segments.size(); i++)
		{
			final String segment = segments.get(i);

			// drop '.' from path
			if (".".equals(segment))
			{
				continue;
			}

			// skip segment if following segment is a '..'
			if ((i + 1) < segments.size() && "..".equals(segments.get(i + 1)))
			{
				i++;
				continue;
			}

			url.segments.add(segment);
		}
		return url;
	}
