	public void respond(RequestCycle requestCycle)
	{
		Url currentUrl = requestCycle.getUrlRenderer().getBaseUrl();
		Url targetUrl = requestCycle.mapUrlFor(getRenderPageRequestHandler());

		//
		// the code below is little hairy but we have to handle 3 redirect policies,
		// 3 rendering strategies and two kind of requests (ajax and normal)
		//

		if (shouldRenderPageAndWriteResponse(requestCycle, currentUrl, targetUrl))
		{
			BufferedWebResponse response = renderPage(currentUrl, requestCycle);
			if (response != null)
			{
				response.writeTo((WebResponse)requestCycle.getResponse());
			}
		}
		else if (shouldRedirectToTargetUrl(requestCycle, currentUrl, targetUrl))
		{
			
			redirectTo(targetUrl, requestCycle);

			// note: if we had session here we would render the page to buffer and then
			// redirect to URL generated *after* page has been rendered (the statelessness
			// may change during render). this would save one redirect because now we have
			// to render to URL generated *before* page is rendered, render the page, get
			// URL after render and if the URL is different (meaning page is not stateless),
			// save the buffer and redirect again (which is pretty much what the next step
			// does)
		}
		else
		{
			if (isRedirectToBuffer() == false && logger.isDebugEnabled())
			{
				String details = String
					.format(
						"redirect strategy: '%s', isAjax: '%s', redirect policy: '%s', "
							+ "current url: '%s', target url: '%s', is new: '%s', is stateless: '%s', is temporary: '%s'",
						Application.get().getRequestCycleSettings().getRenderStrategy(),
						isAjax(requestCycle), getRedirectPolicy(), currentUrl, targetUrl,
						isNewPageInstance(), isPageStateless(), isSessionTemporary());
				logger
					.debug("Falling back to Redirect_To_Buffer render strategy because none of the conditions "
						+ "matched. Details: " + details);
			}

			// force creation of possible stateful page to get the final target url
			getPage();

			Url beforeRenderUrl = requestCycle.mapUrlFor(getRenderPageRequestHandler());

			// redirect to buffer
			BufferedWebResponse response = renderPage(beforeRenderUrl, requestCycle);

			if (response == null)
			{
				return;
			}

			// the url might have changed after page has been rendered (e.g. the
			// stateless flag might have changed because stateful components
			// were added)
			final Url afterRenderUrl = requestCycle
				.mapUrlFor(getRenderPageRequestHandler());

			if (beforeRenderUrl.getSegments().equals(afterRenderUrl.getSegments()) == false)
			{
				// the amount of segments is different - generated relative URLs
				// will not work, we need to rerender the page. This can happen
				// with IRequestHandlers that produce different URLs with
				// different amount of segments for stateless and stateful pages
				response = renderPage(afterRenderUrl, requestCycle);
			}

			if (currentUrl.equals(afterRenderUrl))
			{
				// no need to redirect when both urls are exactly the same
				response.writeTo((WebResponse)requestCycle.getResponse());
			}
			// if page is still stateless after render
			else if (isPageStateless() && !enableRedirectForStatelessPage())
			{
				// we don't want the redirect to happen for stateless page
				// example:
				// when a normal mounted stateful page is hit at /mount/point
				// wicket renders the page to buffer and redirects to /mount/point?12
				// but for stateless page the redirect is not necessary
				// also for listener interface on stateful page we want to redirect
				// after the listener is invoked, but on stateless page the user
				// must ask for redirect explicitly
				response.writeTo((WebResponse)requestCycle.getResponse());
			}
			else
			{
				storeBufferedResponse(afterRenderUrl, response);

				redirectTo(afterRenderUrl, requestCycle);
			}
		}
	}
