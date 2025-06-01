	public String toAbsoluteString(final Charset charset)
	{
		StringBuilder result = new StringBuilder();

		// output scheme://host:port if specified
		if(protocol != null && Strings.isEmpty(host) == false)
		{
			result.append(protocol);
			result.append("://");
			result.append(host);
			
			if(port != null && port.equals(getDefaultPortForProtocol(protocol)) == false)
			{
				result.append(':');
				result.append(port);
			}
		}
		// append relative part
		result.append(this.toString());
	
		// return url string
		return result.toString();
	}
	public Url(final List<String> segments, final Charset charset)
	{
		this(segments, Collections.<QueryParameter>emptyList(), charset);
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
	public void resolveRelative(final Url relative)
	{
		// strip the first non-folder segment
		getSegments().remove(getSegments().size() - 1);

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
