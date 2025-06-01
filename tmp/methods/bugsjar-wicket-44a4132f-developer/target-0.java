	protected Url buildUrl(UrlInfo info)
	{
		Url url = new Url();
		for (String s : mountSegments)
		{
			url.getSegments().add(s);
		}
		encodePageComponentInfo(url, info.getPageComponentInfo());

		PageParameters copy = new PageParameters(info.getPageParameters());

		int dropped = 0;
		for (int i = 0; i < mountSegments.length; ++i)
		{
			String placeholder = getPlaceholder(mountSegments[i]);
			String optionalPlaceholder = getOptionalPlaceholder(mountSegments[i]);
			if (placeholder != null)
			{
				if (!copy.getNamedKeys().contains(placeholder))
				{
					// no value for placeholder - cannot mount
					return null;
				}
				url.getSegments().set(i - dropped, copy.get(placeholder).toString(""));
				copy.remove(placeholder);
			}
			else if (optionalPlaceholder != null)
			{
				if (copy.getNamedKeys().contains(optionalPlaceholder))
				{
					url.getSegments().set(i - dropped, copy.get(optionalPlaceholder).toString(""));
					copy.remove(optionalPlaceholder);
				}
				else
				{
					url.getSegments().remove(i - dropped);
					dropped++;
				}
			}
		}

		return encodePageParameters(url, copy, pageParametersEncoder);
	}
