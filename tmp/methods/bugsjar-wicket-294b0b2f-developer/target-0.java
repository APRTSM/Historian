	protected String getFilterPathFromAnnotation(boolean isServlet)
	{
		String[] patterns = null;

		if (isServlet)
		{
			WebServlet servlet = getClass().getAnnotation(WebServlet.class);
			if (servlet != null)
			{
				if (servlet.urlPatterns().length > 0)
				{
					patterns = servlet.urlPatterns();
				}
				else
				{
					patterns = servlet.value();
				}
			}
		}
		else
		{
			WebFilter filter = getClass().getAnnotation(WebFilter.class);
			if (filter != null)
			{
				if (filter.urlPatterns().length > 0)
				{
					patterns = filter.urlPatterns();
				}
				else
				{
					patterns = filter.value();
				}
			}
		}

		if (patterns != null && patterns.length > 0)
		{
			String pattern = patterns[0];
			if (patterns.length > 1)
			{
				log.warn(
						"Multiple url patterns defined for Wicket filter/servlet, using the first: {}",
						pattern);
			}

			if ("/*".equals(pattern))
			{
				pattern = "";
			}

			if (pattern.endsWith("*"))
			{
				pattern = pattern.substring(0, pattern.length() - 1);
			}
			return pattern;
		}
		return null;
	}
