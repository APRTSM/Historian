	private static QueryParameter parseQueryParameter(final String qp, final Charset charset)
	{
		int idxOfEquals = qp.indexOf('=');
		if (idxOfEquals == -1)
		{
			// name => empty value
			return new QueryParameter(decodeParameter(qp, charset), "");
		}

		String parameterName = qp.substring(0, idxOfEquals);
		String parameterValue = qp.substring(idxOfEquals + 1);
		return new QueryParameter(decodeParameter(parameterName, charset), decodeParameter(parameterValue, charset));
	}
