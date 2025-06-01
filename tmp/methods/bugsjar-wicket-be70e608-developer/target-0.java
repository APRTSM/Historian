	final void setMarkupId(Component comp)
	{
		Args.notNull(comp, "comp");

		generatedMarkupId = comp.generatedMarkupId;
		setMetaData(MARKUP_ID_KEY, comp.getMetaData(MARKUP_ID_KEY));
		setOutputMarkupId(comp.getOutputMarkupId());
		return;
	}
