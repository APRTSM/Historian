	protected IMarkupFragment searchMarkupInTransparentResolvers(final MarkupContainer container,
		final Component child)
	{
		return container.visitChildren(MarkupContainer.class, new IVisitor<MarkupContainer, IMarkupFragment>()
		{
			@Override
			public void component(MarkupContainer resolvingContainer, IVisit<IMarkupFragment> visit)
			{
				//prevents possible searching loops
				if (child == resolvingContainer) 
				{
					visit.dontGoDeeper();
					return;
				}
				
				if (resolvingContainer instanceof IComponentResolver)
				{
					visit.dontGoDeeper();

					IMarkupFragment childMarkup = resolvingContainer.getMarkup(child);

					if (childMarkup != null && childMarkup.size() > 0)
					{
						IComponentResolver componentResolver = (IComponentResolver)resolvingContainer;

						MarkupStream stream = new MarkupStream(childMarkup);

						ComponentTag tag = stream.getTag();

						Component resolvedComponent = resolvingContainer.get(tag.getId());
						if (resolvedComponent == null)
						{
							resolvedComponent = componentResolver.resolve(resolvingContainer, stream, tag);
						}

						if (child == resolvedComponent)
						{
							visit.stop(childMarkup);
						}
					}
				}				
			}
		});
	}
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

		associatedMarkup = searchMarkupInTransparentResolvers(parent, child);
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
		
		markup = searchMarkupInTransparentResolvers(container, child);
		
		return markup;
	}
