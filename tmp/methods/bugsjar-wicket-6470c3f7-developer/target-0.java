	public static Url parse(CharSequence _url, Charset charset)
	{
		Args.notNull(_url, "_url");

		final Url result = new Url(charset);

		// the url object resolved the charset, use that
		charset = result.getCharset();

		String url = _url.toString();
		// extract query string part
		final String queryString;
		final String absoluteUrl;

		final int queryAt = url.indexOf('?');

		if (queryAt == -1)
		{
			queryString = "";
			absoluteUrl = url;
		}
		else
		{
			absoluteUrl = url.substring(0, queryAt);
			queryString = url.substring(queryAt + 1);
		}

		// get absolute / relative part of url
		String relativeUrl;

		// absolute urls contain a scheme://
		final int idxOfFirstSlash = absoluteUrl.indexOf('/');
		final int protocolAt = absoluteUrl.indexOf("://");

		if (protocolAt > -1 && (protocolAt < idxOfFirstSlash))
		{
			result.protocol = absoluteUrl.substring(0, protocolAt).toLowerCase(Locale.US);

			final String afterProto = absoluteUrl.substring(protocolAt + 3);
			final String hostAndPort;

			int relativeAt = afterProto.indexOf('/');
			if (relativeAt == -1)
			{
				relativeAt = afterProto.indexOf(';');
			}
			if (relativeAt == -1)
			{
				relativeUrl = "";
				hostAndPort = afterProto;
			}
			else
			{
				relativeUrl = afterProto.substring(relativeAt);
				hostAndPort = afterProto.substring(0, relativeAt);
			}

			final int portAt = hostAndPort.lastIndexOf(':');

			if (portAt == -1)
			{
				result.host = hostAndPort;
				result.port = getDefaultPortForProtocol(result.protocol);
			}
			else
			{
				result.host = hostAndPort.substring(0, portAt);
				result.port = Integer.parseInt(hostAndPort.substring(portAt + 1));
			}

			if (relativeAt < 0)
			{
				relativeUrl = "/";
			}
		}
		else
		{
			relativeUrl = absoluteUrl;
		}

		if (relativeUrl.length() > 0)
		{
			boolean removeLast = false;
			if (relativeUrl.endsWith("/"))
			{
				// we need to append something and remove it after splitting
				// because otherwise the
				// trailing slashes will be lost
				relativeUrl += "/x";
				removeLast = true;
			}

			String segmentArray[] = Strings.split(relativeUrl, '/');

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

		if (queryString.length() > 0)
		{
			String queryArray[] = Strings.split(queryString, '&');
			for (String s : queryArray)
			{
				if (Strings.isEmpty(s) == false)
				{
					result.parameters.add(parseQueryParameter(s, charset));
				}
			}
		}

		return result;
	}
