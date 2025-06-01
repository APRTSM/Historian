	protected void writeHeaderContribution(final Response response, final Component component)
	{
		headerRendering = true;

		// create the htmlheadercontainer if needed
		if (header == null)
		{
			header = new AjaxHtmlHeaderContainer(this);
			final Page parentPage = component.getPage();
			parentPage.addOrReplace(header);
		}

		RequestCycle requestCycle = component.getRequestCycle();

		// save old response, set new
		Response oldResponse = requestCycle.setResponse(encodingHeaderResponse);

		try {
			encodingHeaderResponse.reset();

			// render the head of component and all it's children

			component.renderHead(header);

			if (component instanceof MarkupContainer)
			{
				((MarkupContainer)component).visitChildren(new IVisitor<Component, Void>()
				{
					@Override
					public void component(final Component component, final IVisit<Void> visit)
					{
						if (component.isVisibleInHierarchy())
						{
							component.renderHead(header);
						}
						else
						{
							visit.dontGoDeeper();
						}
					}
				});
			}
		} finally {
			// revert to old response
			requestCycle.setResponse(oldResponse);
		}

		writeHeaderContribution(response);

		headerRendering = false;
	}
	public void renderHead(final Component component, final IHeaderResponse response)
	{
		super.renderHead(component, response);

		if (component.isEnabledInHierarchy())
		{
			CharSequence js = getCallbackScript(component);

			AjaxRequestTarget target = component.getRequestCycle().find(AjaxRequestTarget.class);
			if (target == null)
			{
				response.render(OnDomReadyHeaderItem.forScript(js.toString()));
			}
			else
			{
				target.appendJavaScript(js);
			}
		}
	}
