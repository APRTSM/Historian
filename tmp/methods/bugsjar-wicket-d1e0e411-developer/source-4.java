	protected void onAfterRenderChildren()
	{
		// Loop through child components
		final Iterator<? extends Component> iter = iterator();
		while (iter.hasNext())
		{
			// Get next child
			final Component child = iter.next();

			// Call end request on the child
			child.afterRender();
		}
		super.onAfterRenderChildren();
	}
	public void onComponentTag(final Component component, final ComponentTag tag)
	{
		tag.put("xmlns:wicket", "http://wicket.apache.org");
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
			Model.of("http://wicket.apache.org/dtds.data/wicket-xhtml1.3-strict.dtd")));
	}
	public void onComponentTag(final Component component, final ComponentTag tag)
	{
		tag.put("xmlns:wicket", "http://wicket.apache.org/dtds.data/wicket-xhtml1.3-strict.dtd");

		// Make the XSLT processor happy and allow it to handle the wicket tags
		// and attributes that are in the wicket namespace
		super.onComponentTag(component, tag);
	}
