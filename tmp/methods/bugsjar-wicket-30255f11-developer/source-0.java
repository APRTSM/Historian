	public static Url parse(final String url, Charset charset)
	{
		Args.notNull(url, "url");

		Url result = new Url(charset);

		// the url object resolved the charset, use that
		charset = result.getCharset();

		String segments;
		String query;

		int qIndex = url.indexOf('?');

		if (qIndex == -1)
		{
			segments = url;
			query = "";
		}
		else
		{
			segments = url.substring(0, qIndex);
			query = url.substring(qIndex + 1);
		}

		if (segments.length() > 0)
		{

			boolean removeLast = false;
			if (segments.endsWith("/"))
			{
				// we need to append something and remove it after splitting
				// because otherwise the
				// trailing slashes will be lost
				segments += "/x";
				removeLast = true;
			}

			String segmentArray[] = Strings.split(segments, '/');

			if (removeLast)
			{
				segmentArray[segmentArray.length - 1] = null;
			}

			for (String s : segmentArray)
			{
				if (s != null)
				{
					result.segments.add(decodeSegment(s, charset));
				}
			}
		}

		if (query.length() > 0)
		{
			String queryArray[] = Strings.split(query, '&');
			for (String s : queryArray)
			{
				result.parameters.add(parseQueryParameter(s, charset));
			}
		}

		return result;
	}
