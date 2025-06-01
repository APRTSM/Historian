	public void renderPage()
	{
		// page id is frozen during the render
		final boolean frozen = setFreezePageId(true);
		try
		{
			++renderCount;
			render();

			// stateless = null;
		}
		finally
		{
			setFreezePageId(frozen);
		}
	}
	protected void onBeforeRender()
	{
		// Make sure it is really empty
		renderedComponents = null;

		// rendering might remove or add stateful components, so clear flag to force reevaluation
		stateless = null;

		super.onBeforeRender();

		// If any of the components on page is not stateless, we need to bind the session
		// before we start rendering components, as then jsessionid won't be appended
		// for links rendered before first stateful component
		if (getSession().isTemporary() && !peekPageStateless())
		{
			getSession().bind();
		}
	}
