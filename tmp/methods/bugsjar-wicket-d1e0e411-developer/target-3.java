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
