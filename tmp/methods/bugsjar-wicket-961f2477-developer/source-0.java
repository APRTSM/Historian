	private static QueryParameter parseQueryParameter(final String qp, final Charset charset)
	{
		if (qp.indexOf('=') == -1)
		{
			// name => empty value
			return new QueryParameter(decodeParameter(qp, charset), "");
		}

		String parts[] = Strings.split(qp, '=');
		return new QueryParameter(decodeParameter(parts[0], charset), decodeParameter(parts[1],
			charset));
	}
