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
		response.write(getMarkupId());
		response.write("\" style=\"display:none\"></");
		if (ns != null)
		{
			response.write(ns);
		}
		response.write(tag.getName());
		response.write(">");
	}
