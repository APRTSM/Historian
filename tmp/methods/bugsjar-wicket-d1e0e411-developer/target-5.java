	protected void onAfterRenderChildren()
	{
		// Loop through child components
		for (Component child : this)
		{
			// Call end request on the child
			child.afterRender();
		}

		super.onAfterRenderChildren();
	}
	public XsltOutputTransformerContainer(final String id, final IModel<?> model,
		final String xslFilePath)
	{
		super(id);

		xslFile = xslFilePath;

		// The containers tag will be transformed as well. Thus we make sure that
		// the xml provided to the xsl processor is well formed (has a single
		// root element)
		setTransformBodyOnly(false);

		// Make the XSLT processor happy and allow him to handle the wicket
		// tags and attributes which are in the wicket namespace
		add(AttributeModifier.replace("xmlns:wicket",
			Model.of(MarkupResourceStream.WICKET_XHTML_DTD)));
	}
	public void onComponentTag(final Component component, final ComponentTag tag)
	{
		// Make the XSLT processor happy and allow it to handle the wicket tags
		// and attributes that are in the wicket namespace
		tag.put("xmlns:wicket", MarkupResourceStream.WICKET_XHTML_DTD);

		super.onComponentTag(component, tag);
	}
		protected void invoke(WebResponse response)
		{
			AppendingStringBuffer responseBuffer = new AppendingStringBuffer(builder);

			List<IResponseFilter> responseFilters = Application.get()
				.getRequestCycleSettings()
				.getResponseFilters();

			if (responseFilters != null)
			{
				for (IResponseFilter filter : responseFilters)
				{
					responseBuffer = filter.filter(responseBuffer);
				}
			}
			response.write(responseBuffer);
		}
