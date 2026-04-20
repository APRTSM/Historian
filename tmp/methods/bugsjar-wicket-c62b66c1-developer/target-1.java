	public final String getAjaxRegionMarkupId()
	{
		String markupId = null;
		for (Behavior behavior : getBehaviors())
		{
			if (behavior instanceof IAjaxRegionMarkupIdProvider)
			{
				markupId = ((IAjaxRegionMarkupIdProvider)behavior).getAjaxRegionMarkupId(this);
			}
		}
		if (markupId == null)
		{
			if (this instanceof IAjaxRegionMarkupIdProvider)
			{
				markupId = ((IAjaxRegionMarkupIdProvider)this).getAjaxRegionMarkupId(this);
			}
		}
		if (markupId == null)
		{
			markupId = getMarkupId();
		}
		return markupId;
	}
	protected void renderPlaceholderTag(final ComponentTag tag, final Response response)
	{
		String ns = Strings.isEmpty(tag.getNamespace()) ? null : tag.getNamespace() + ":";

		response.write("<");
		if (ns != null)
		{
			response.write(ns);
		}
		response.write(tag.getName());
		response.write(" id=\"");
		response.write(getAjaxRegionMarkupId());
		response.write("\" style=\"display:none\"></");
		if (ns != null)
		{
			response.write(ns);
		}
		response.write(tag.getName());
		response.write(">");
	}
	private void respondComponents(Response response)
	{
		// TODO: We might need to call prepareRender on all components upfront

		// process component markup
		for (Map.Entry<String, Component> stringComponentEntry : markupIdToComponent.entrySet())
		{
			final Component component = stringComponentEntry.getValue();
			// final String markupId = stringComponentEntry.getKey();

			if (!containsAncestorFor(component))
			{
				respondComponent(response, component.getAjaxRegionMarkupId(), component);
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
