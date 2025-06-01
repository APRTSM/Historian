	public String toString(StringMode mode, Charset charset)
	{
		StringBuilder result = new StringBuilder();
		final String path = getPath(charset);

		if (StringMode.FULL == mode)
		{
			if (Strings.isEmpty(host))
			{
				throw new IllegalStateException("Cannot render this url in " +
					StringMode.FULL.name() + " mode because it does not have a host set.");
			}

			if (Strings.isEmpty(protocol) == false)
			{
				result.append(protocol);
				result.append("://");
			}
			else if (Strings.isEmpty(protocol) && Strings.isEmpty(host) == false)
			{
				result.append("//");
			}
			result.append(host);

			if (port != null && port.equals(getDefaultPortForProtocol(protocol)) == false)
			{
				result.append(':');
				result.append(port);
			}

			if (segments.contains(".."))
			{
				throw new IllegalStateException("Cannot render this url in " +
					StringMode.FULL.name() + " mode because it has a `..` segment: " + toString());
			}

			if (!path.startsWith("/"))
			{
				result.append('/');
			}

		}


		result.append(path);
		result.append(getQueryString(charset));
		return result.toString();
	}
