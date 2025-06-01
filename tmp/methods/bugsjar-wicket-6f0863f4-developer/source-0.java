	public void resolveRelative(final Url relative)
	{
		if (getSegments().size() > 0)
		{
			// strip the first non-folder segment
			getSegments().remove(getSegments().size() - 1);
		}

		// remove leading './' (current folder) and empty segments, process any ../ segments from the
		// relative url
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

		for (int i = 0; i < this.segments.size(); i++)
		{
			final String segment = this.segments.get(i);

			// drop '.' from path  
			if (".".equals(segment))
			{
				continue;
			}

			// skip segment if following segment is a '..'
			if ((i + 1) < this.segments.size() && "..".equals(this.segments.get(i + 1)))
			{
				i++;
				continue;
			}

			url.segments.add(segment);
		}
		return url;
	}
	public Url(final Url url)
	{
		Args.notNull(url, "url");

		this.protocol = url.protocol;
		this.host = url.host;
		this.port = url.port;
		this.segments = new ArrayList<String>(url.segments);
		this.parameters = new ArrayList<QueryParameter>(url.parameters);
		this.charsetName = url.charsetName;
		this._charset = url._charset;
	}
