		private PageParameters cleanPageParameters(final PageParameters originalParameters)
		{
			PageParameters cleanParameters = null;
			if (originalParameters != null)
			{
				cleanParameters = new PageParameters(originalParameters);

				// WICKET-4038: Ajax related parameters are set by wicket-ajax.js when needed.
				// They shouldn't be propagated to the next requests
				cleanParameters.remove(WebRequest.PARAM_AJAX);
				cleanParameters.remove(WebRequest.PARAM_AJAX_BASE_URL);
				cleanParameters.remove(WebRequest.PARAM_AJAX_REQUEST_ANTI_CACHE);
			}
			return cleanParameters;
		}
