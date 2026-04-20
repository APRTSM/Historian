	public Url getClientUrl()
	{
		if (errorAttributes != null && !Strings.isEmpty(errorAttributes.getRequestUri()))
		{
			String problematicURI = Url.parse(errorAttributes.getRequestUri(), getCharset())
				.toString();
			return getContextRelativeUrl(problematicURI, filterPrefix);
		}
		else if (forwardAttributes != null && !Strings.isEmpty(forwardAttributes.getRequestUri()))
		{
			String forwardURI = Url.parse(forwardAttributes.getRequestUri(), getCharset())
				.toString();
			return getContextRelativeUrl(forwardURI, filterPrefix);
		}
		else if (!isAjax())
		{
			return getContextRelativeUrl(httpServletRequest.getRequestURI(), filterPrefix);
		}
		else
		{
			String base = getHeader(HEADER_AJAX_BASE_URL);

			if (base == null)
			{
				base = getRequestParameters().getParameterValue(PARAM_AJAX_BASE_URL).toString(null);
			}

			if (base == null)
			{
				throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_BAD_REQUEST,
					"Current ajax request is missing the base url header or parameter");
			}

			return setParameters(Url.parse(base, getCharset()));
		}
	}
