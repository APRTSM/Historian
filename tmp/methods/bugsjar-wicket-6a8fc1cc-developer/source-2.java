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

		// If the child has not been directly added to the container, but via a
		// TransparentWebMarkupContainer, then we are in trouble. In general Wicket iterates over
		// the markup elements and searches for associated components, not the other way around.
		// Because of TransparentWebMarkupContainer (or more generally resolvers), there is no
		// "synchronous" search possible.
		for (Component ch : container)
		{
			if ((ch != child) && (ch instanceof MarkupContainer) &&
				(ch instanceof IComponentResolver))
			{
				markup = ((MarkupContainer)ch).getMarkup(child);
				if (markup != null)
				{
					return markup;
				}
			}
		}

		// This is to make migration for Items from 1.4 to 1.5 more easy
		if (Character.isDigit(child.getId().charAt(0)))
		{
			String id = child.getId();
			boolean miss = false;
			for (int i = 1; i < id.length(); i++)
			{
				if (Character.isDigit(id.charAt(i)) == false)
				{
					miss = true;
					break;
				}
			}

			if (miss == false)
			{
				// The LoopItems markup is equal to the Loops markup
				markup = container.getMarkup();

				if (!(child instanceof AbstractItem) && log.isWarnEnabled())
				{
					log.warn("1.4 to 1.5 migration issue: the childs wicket-id contains decimals only. " +
						"By convention that " +
						"is only the case for children (Items) of Loop, ListView, " +
						"Tree etc.. To avoid the warning, the childs container should implement:\n" +
						"@Override public IMarkupFragment getMarkup(Component child) {\n" +
						"// The childs markup is always equal to the parents markup.\n" +
						"return getMarkup(); }\n" +
						"Child: " +
						child.toString() +
						"\nContainer: " +
						container.toString());
				}
			}
		}

		return markup;
	}
