	public void sendRedirect(String url)
	{
		try
		{
			redirect = true;
			url = encodeRedirectURL(url);

			// wicket redirects should never be cached
			disableCaching();

			if (webRequest.isAjax())
			{
				httpServletResponse.addHeader("Ajax-Location", url);

				/*
				 * usually the Ajax-Location header is enough and we do not need to the redirect url
				 * into the response, but sometimes the response is processed via an iframe (eg
				 * using multipart ajax handling) and the headers are not available because XHR is
				 * not used and that is the only way javascript has access to response headers.
				 */
				httpServletResponse.getWriter().write(
					"<ajax-response><redirect><![CDATA[" + url + "]]></redirect></ajax-response>");

				setContentType("text/xml;charset=" +
					webRequest.getContainerRequest().getCharacterEncoding());
				disableCaching();
			}
			else
			{
				if (url.startsWith("./"))
				{
					/*
					 * WICKET-4260 Tomcat does not canonalize urls, which leads to problems with IE
					 * when url is relative and starts with a dot
					 */
					url = url.substring(2);
				}
				httpServletResponse.sendRedirect(url);
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
	}
