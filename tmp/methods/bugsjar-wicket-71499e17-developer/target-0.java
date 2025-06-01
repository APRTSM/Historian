	protected static String encodeResourceReferenceAttributes(
		ResourceReference.UrlAttributes attributes)
	{
		if (attributes == null ||
			(attributes.getLocale() == null && attributes.getStyle() == null && attributes.getVariation() == null))
		{
			return null;
		}
		else
		{
			StringBuilder res = new StringBuilder();
			if (attributes.getLocale() != null)
			{
				res.append(attributes.getLocale().toString());
			}
			boolean styleEmpty = Strings.isEmpty(attributes.getStyle());
			if (!styleEmpty)
			{
				res.append('-');
				res.append(attributes.getStyle());
			}
			if (!Strings.isEmpty(attributes.getVariation()))
			{
				if (styleEmpty)
				{
					res.append("--");
				}
				else
				{
					res.append('-');
				}
				res.append(attributes.getVariation());
			}
			return res.toString();
		}
	}
