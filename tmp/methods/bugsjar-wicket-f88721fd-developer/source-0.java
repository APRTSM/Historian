	private static QueryParameter parseQueryParameter(final String qp, final Charset charset)
	{
		if (qp.indexOf('=') == -1)
		{
			return new QueryParameter(decodeParameter(qp, charset), "");
		}
		String parts[] = Strings.split(qp, '=');
		if (parts.length == 0)
		{
			return new QueryParameter("", "");
		}
		else if (parts.length == 1)
		{
			return new QueryParameter("", decodeParameter(parts[0], charset));
		}
		else
		{
			return new QueryParameter(decodeParameter(parts[0], charset), decodeParameter(parts[1],
				charset));
		}
	}
	public static Url parse(String url, Charset charset)
	{
		Args.notNull(url, "url");

		final Url result = new Url(charset);

		// the url object resolved the charset, use that
		charset = result.getCharset();

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
		final int protocolAt = absoluteUrl.indexOf("://");

		if (protocolAt != -1)
		{
			result.protocol = absoluteUrl.substring(0, protocolAt).toLowerCase(Locale.US);

			final String afterProto = absoluteUrl.substring(protocolAt + 3);
			final String hostAndPort;

			final int relativeAt = afterProto.indexOf('/');

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
				result.parameters.add(parseQueryParameter(s, charset));
			}
		}

		return result;
	}
