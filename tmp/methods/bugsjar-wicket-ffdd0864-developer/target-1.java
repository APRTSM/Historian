	protected IMarkupFragment searchMarkupInTransparentResolvers(MarkupContainer container,
		IMarkupFragment containerMarkup, Component child)
	{
		IMarkupFragment childMarkupFound = null;
		Iterator<Component> siblingsIterator = container.iterator();

		while (siblingsIterator.hasNext() && childMarkupFound == null)
		{
			Component sibling = siblingsIterator.next();

			if(sibling == child || !sibling.isVisible())
			{
				continue;
			}

			IMarkupFragment siblingMarkup = containerMarkup.find(sibling.getId());

			if (siblingMarkup != null && sibling instanceof MarkupContainer)
			{
				IMarkupFragment childMarkup  = siblingMarkup.find(child.getId());
				
				if (childMarkup != null && sibling instanceof IComponentResolver)
				{
					IComponentResolver componentResolver = (IComponentResolver)sibling;
					MarkupStream stream = new MarkupStream(childMarkup);
					ComponentTag tag = stream.getTag();

					Component resolvedComponent = sibling.get(tag.getId());
					if (resolvedComponent == null)
					{
						resolvedComponent = componentResolver.resolve((MarkupContainer)sibling, stream, tag);
					}

					if (child == resolvedComponent)
					{
						childMarkupFound = childMarkup;
					}
				}
				else
				{
					childMarkupFound = searchMarkupInTransparentResolvers((MarkupContainer)sibling, siblingMarkup, child);
				}
			}
		}
		return childMarkupFound;
	}
	public abstract IMarkupFragment getMarkup(final MarkupContainer container, final Component child);

	/**
	 * If the child has not been directly added to the container, but via a
	 * TransparentWebMarkupContainer, then we are in trouble. In general Wicket iterates over the
	 * markup elements and searches for associated components, not the other way around. Because of
	 * TransparentWebMarkupContainer (or more generally resolvers), there is no "synchronous" search
	 * possible.
	 * 
	 * @param container
	 *            the parent container.
	 * @param
	 * 		  containerMarkup
	 * 			  the markup of the container.
	 * @param child
	 *            The component to find the markup for.
	 * @return the markup fragment for the child, or {@code null}.
	public IMarkupFragment getMarkup(final MarkupContainer parent, final Component child)
	{
		Args.notNull(tagName, "tagName");

		IMarkupFragment associatedMarkup = parent.getAssociatedMarkup();
		if (associatedMarkup == null)
		{
			throw new MarkupNotFoundException("Failed to find markup file associated. " +
				Classes.simpleName(parent.getClass()) + ": " + parent.toString());
		}

		// Find <wicket:panel>
		IMarkupFragment markup = MarkupUtil.findStartTag(associatedMarkup, tagName);
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

		associatedMarkup = searchMarkupInTransparentResolvers(parent, markup, child);
		if (associatedMarkup != null)
		{
			return associatedMarkup;
		}

		return findMarkupInAssociatedFileHeader(parent, child);
	}
