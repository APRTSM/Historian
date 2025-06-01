	public final void respond(final IRequestCycle requestCycle)
	{
		// do not increment page id during ajax processing
		boolean frozen = page.setFreezePageId(true);

		try
		{
			RequestCycle rc = (RequestCycle)requestCycle;

			final WebResponse response = (WebResponse)requestCycle.getResponse();

			if (markupIdToComponent.values().contains(page))
			{
				// the page itself has been added to the request target, we simply issue a redirect
				// back to the page
				IRequestHandler handler = new RenderPageRequestHandler(new PageProvider(page));
				final String url = rc.urlFor(handler).toString();
				response.sendRedirect(url);
				return;
			}

			for (ITargetRespondListener listener : respondListeners)
			{
				listener.onTargetRespond(this);
			}

			final Application app = Application.get();

			page.send(app, Broadcast.BREADTH, this);

			// Determine encoding
			final String encoding = app.getRequestCycleSettings().getResponseRequestEncoding();

			// Set content type based on markup type for page
			response.setContentType("text/xml; charset=" + encoding);

			// Make sure it is not cached by a client
			response.disableCaching();

			response.write("<?xml version=\"1.0\" encoding=\"");
			response.write(encoding);
			response.write("\"?>");
			response.write("<ajax-response>");

			// invoke onbeforerespond event on listeners
			fireOnBeforeRespondListeners();

			// normal behavior
			Iterator<CharSequence> it = prependJavaScripts.iterator();
			while (it.hasNext())
			{
				CharSequence js = it.next();
				respondInvocation(response, js);
			}

			// process added components
			respondComponents(response);

			fireOnAfterRespondListeners(response);

			// execute the dom ready javascripts as first javascripts
			// after component replacement
			it = domReadyJavaScripts.iterator();
			while (it.hasNext())
			{
				CharSequence js = it.next();
				respondInvocation(response, js);
			}
			it = appendJavaScripts.iterator();
			while (it.hasNext())
			{
				CharSequence js = it.next();
				respondInvocation(response, js);
			}

			response.write("</ajax-response>");
		}
		finally
		{
			page.setFreezePageId(frozen);
		}
	}
	private void respondComponents(WebResponse response)
	{
		// TODO: We might need to call prepareRender on all components upfront

		// process component markup
		for (Map.Entry<String, Component> stringComponentEntry : markupIdToComponent.entrySet())
		{
			final Component component = stringComponentEntry.getValue();
			// final String markupId = stringComponentEntry.getKey();

			if (!containsAncestorFor(component))
			{
				respondComponent(response, getAjaxRegionMarkupId(component), component);
			}
		}

		if (header != null)
		{
			// some header responses buffer all calls to render*** until close is called.
			// when they are closed, they do something (i.e. aggregate all JS resource urls to a
			// single url), and then "flush" (by writing to the real response) before closing.
			// to support this, we need to allow header contributions to be written in the close
			// tag, which we do here:
			headerRendering = true;
			// save old response, set new
			Response oldResponse = RequestCycle.get().setResponse(encodingHeaderResponse);
			encodingHeaderResponse.reset();

			// now, close the response (which may render things)
			header.getHeaderResponse().close();

			// revert to old response
			RequestCycle.get().setResponse(oldResponse);

			// write the XML tags and we're done
			writeHeaderContribution(response);
			headerRendering = false;
		}
	}
	private void fireOnAfterRespondListeners(final WebResponse response)
	{
		// invoke onafterresponse event on listeners
		if (listeners != null)
		{
			final Map<String, Component> components = Collections.unmodifiableMap(markupIdToComponent);

			// create response that will be used by listeners to append
			// javascript
			final IJavaScriptResponse jsresponse = new IJavaScriptResponse()
			{
				public void addJavaScript(String script)
				{
					respondInvocation(response, script);
				}
			};

			for (IListener listener : listeners)
			{
				listener.onAfterRespond(components, jsresponse);
			}
		}
	}
		protected void invoke(WebResponse response)
		{
			response.write(builder);
		}
	public String toString()
	{
		return charSequenceAction.builder.toString();
	}
