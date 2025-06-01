	protected IMarkupFragment searchMarkupInTransparentResolvers(final MarkupContainer container,
		final Component child)
	{
		IMarkupFragment markup = null;

		for (Component ch : container)
		{
			if ((ch != child) && (ch instanceof MarkupContainer) &&
				(ch instanceof IComponentResolver))
			{
				markup = ((MarkupContainer)ch).getMarkup(child);
				if (markup != null)
				{
					break;
				}
			}
		}

		return markup;
	}
