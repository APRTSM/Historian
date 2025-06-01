	public IMarkupFragment getMarkup(final MarkupContainer container, final Component child)
	{
		// Get the markup to search for the fragment markup
		IMarkupFragment markup = chooseMarkup(container);
		if (markup == null)
		{
			throw new MarkupException("The fragments markup provider has no associated markup. " +
				"No markup to search for fragment markup with id: " + markupId);
		}

		// Search for the fragment markup
		IMarkupFragment childMarkup = markup.find(markupId);
		if (childMarkup == null)
		{
			// There is one more option if the markup provider has associated markup
			MarkupContainer markupProvider = getMarkupProvider(container);
			Markup associatedMarkup = markupProvider.getAssociatedMarkup();
			if (associatedMarkup != null)
			{
				markup = associatedMarkup;
				if (markup != null)
				{
					childMarkup = markup.find(markupId);
				}
			}
		}

		if (childMarkup == null)
		{
			throw new MarkupNotFoundException("No Markup found for Fragment " + markupId +
				" in providing markup container " + getMarkupProvider(container));
		}
		else
		{
			MarkupElement fragmentTag = childMarkup.get(0);
			if ((fragmentTag instanceof WicketTag && ((WicketTag)fragmentTag).isFragementTag()) == false)
			{
				throw new MarkupNotFoundException("Markup found for Fragment '" + markupId
					+ "' in providing markup container " + getMarkupProvider(container)
					+ " is not a fragment tag");
			}
		}

		if (child == null)
		{
			return childMarkup;
		}

		// search for the child inside the fragment markup
		return childMarkup.find(child.getId());
	}
