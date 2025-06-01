	protected void onComponentTag(final ComponentTag tag)
	{
		super.onComponentTag(tag);

		checkComponentTag(tag, "form");

		if (isRootForm())
		{
			String method = getMethod().toLowerCase(Locale.ENGLISH);
			tag.put("method", method);
			String url = getActionUrl().toString();
			if (encodeUrlInHiddenFields())
			{
				int i = url.indexOf('?');
				String action = (i > -1) ? url.substring(0, i) : "";
				tag.put("action", action);
				// alternatively, we could just put an empty string here, so
				// that mounted paths stay in good order. I decided against this
				// as I'm not sure whether that could have side effects with
				// other encoders
			}
			else
			{
				tag.put("action", url);
			}

			if (isMultiPart())
			{
				if (METHOD_GET.equalsIgnoreCase(method))
				{
					log.warn("Form with id '{}' is multipart. It should use method 'POST'!",
						getId());
					tag.put("method", METHOD_POST.toLowerCase(Locale.ENGLISH));
				}

				tag.put("enctype", ENCTYPE_MULTIPART_FORM_DATA);
				//
				// require the application-encoding for multipart/form-data to be sure to
				// get multipart-uploaded characters with the proper encoding on the following
				// request.
				//
				// for details see: http://stackoverflow.com/questions/546365
				//
				tag.put("accept-charset", getApplication().getRequestCycleSettings()
					.getResponseRequestEncoding());
			}
			else
			{
				// sanity check
				String enctype = (String)tag.getAttributes().get("enctype");
				if (ENCTYPE_MULTIPART_FORM_DATA.equalsIgnoreCase(enctype))
				{
					// though not set explicitly in Java, this is a multipart
					// form
					setMultiPart(true);
				}
			}
		}
		else
		{
			tag.setName("div");
			tag.remove("method");
			tag.remove("action");
			tag.remove("enctype");
		}
	}
	WebRequest createWebRequest(HttpServletRequest servletRequest, final String filterPath)
	{
		if (servletRequest.getCharacterEncoding() == null)
		{
			try
			{
				String wicketAjaxHeader = servletRequest.getHeader(WebRequest.HEADER_AJAX);
				if (Strings.isTrue(wicketAjaxHeader))
				{
					// WICKET-3908, WICKET-1816: Forms submitted with Ajax are always UTF-8 encoded
					servletRequest.setCharacterEncoding(CharEncoding.UTF_8);
				}
				else
				{
					String requestEncoding = getRequestCycleSettings().getResponseRequestEncoding();
					servletRequest.setCharacterEncoding(requestEncoding);
				}
			}
			catch (UnsupportedEncodingException e)
			{
				throw new WicketRuntimeException(e);
			}
		}

		if (hasFilterFactoryManager())
		{
			for (AbstractRequestWrapperFactory factory : getFilterFactoryManager())
			{
				servletRequest = factory.getWrapper(servletRequest);
			}
		}

		WebRequest webRequest = newWebRequest(servletRequest, filterPath);

		String contentType = servletRequest.getContentType();
		String method = servletRequest.getMethod();

		if (webRequest instanceof ServletWebRequest && Form.METHOD_POST.equalsIgnoreCase(method) &&
				Strings.isEmpty(contentType) == false && contentType.toLowerCase().startsWith(Form.ENCTYPE_MULTIPART_FORM_DATA))
		{
			try
			{
				return ((ServletWebRequest)webRequest).newMultipartWebRequest(
					getApplicationSettings().getDefaultMaximumUploadSize(), "externalForm");
			}
			catch (FileUploadException e)
			{
				throw new RuntimeException(e);
			}
		}

		return webRequest;
	}
