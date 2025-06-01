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
