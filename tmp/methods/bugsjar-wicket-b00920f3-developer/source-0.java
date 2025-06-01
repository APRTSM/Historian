	public IMarkupFragment getMarkup(final MarkupContainer container, final Component child)
	{
		// If the sourcing strategy did not provide one, than ask the component.
		// Get the markup for the container
		IMarkupFragment markup = container.getMarkup();
		if (markup == null)
		{
			return null;
		}

		if (child == null)
		{
			return markup;
		}

		// Find the child's markup
		markup = markup.find(child.getId());
		if (markup != null)
		{
			return markup;
		}

		markup = searchMarkupInTransparentResolvers(container, child);
		
		return markup;
	}
