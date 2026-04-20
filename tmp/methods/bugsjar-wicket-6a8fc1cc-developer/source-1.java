	public IMarkupFragment getMarkup(final MarkupContainer parent, final Component child)
	{
		Args.notNull(tagName, "tagName");

		IMarkupFragment associatedMarkup = parent.getAssociatedMarkup();
		if (associatedMarkup == null)
		{
			throw new MarkupNotFoundException("Failed to find markup file associated. " +
				parent.getClass().getSimpleName() + ": " + parent.toString());
		}

		// Find <wicket:panel>
		IMarkupFragment markup = findStartTag(associatedMarkup);
		if (markup == null)
		{
			throw new MarkupNotFoundException("Expected to find <wicket:" + tagName +
				"> in associated markup file. Markup: " + associatedMarkup.toString());
		}

		// If child == null, than return the markup fragment starting with <wicket:panel>
		if (child == null)
		{
			return markup;
		}

		// Find the markup for the child component
		associatedMarkup = markup.find(child.getId());
		if (associatedMarkup != null)
		{
			return associatedMarkup;
		}

		return findMarkupInAssociatedFileHeader(parent, child);
	}
