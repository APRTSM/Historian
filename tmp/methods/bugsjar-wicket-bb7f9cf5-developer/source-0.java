	protected void onAfterRender()
	{
		// only in development mode validate the headers
		if (getApplication().usesDevelopmentConfig())
		{
			validateHeaders();
		}

		super.onAfterRender();
	}
