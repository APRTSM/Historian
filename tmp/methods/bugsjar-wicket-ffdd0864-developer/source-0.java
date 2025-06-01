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
