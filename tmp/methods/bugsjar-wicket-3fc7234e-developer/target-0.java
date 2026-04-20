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
				// skip
			}
			else if ("..".equals(segment) && url.segments.isEmpty() == false)
			{
				url.segments.remove(url.segments.size() - 1);
			}
			// skip segment if following segment is a '..'
			else if ((i + 1) < segments.size() && "..".equals(segments.get(i + 1)))
			{
				i++;
			}
			else
			{
				url.segments.add(segment);
			}
		}
		return url;
	}
