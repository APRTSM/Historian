	protected void onAfterRender()
	{
		// only in development mode validate the headers
		if (getApplication().usesDevelopmentConfig())
		{
			// check headers only when page was completely rendered
			if (wasRendered(this)) {
				validateHeaders();
			}
		}

		super.onAfterRender();
	}
