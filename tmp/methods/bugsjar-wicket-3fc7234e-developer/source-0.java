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
