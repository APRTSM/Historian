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

				tag.put("enctype", "multipart/form-data");
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
				if ("multipart/form-data".equalsIgnoreCase(enctype))
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
