	protected void onBeforeRender()
	{
		// Make sure it is really empty
		renderedComponents = null;

		// if the page is stateless, reset the flag so that it is tested again
		if (Boolean.TRUE.equals(stateless))
		{
			stateless = null;
		}

		super.onBeforeRender();

		// If any of the components on page is not stateless, we need to bind the session
		// before we start rendering components, as then jsessionid won't be appended
		// for links rendered before first stateful component
		if (getSession().isTemporary() && !peekPageStateless())
		{
			getSession().bind();
		}
	}
	public void renderPage()
	{
		// page id is frozen during the render
		final boolean frozen = setFreezePageId(true);
		try
		{
			++renderCount;
			render();
		}
		finally
		{
			setFreezePageId(frozen);
		}
	}
