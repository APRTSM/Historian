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
	 * @param child
	 *            The component to find the markup for.
	 * @return the markup fragment for the child, or {@code null}.
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
