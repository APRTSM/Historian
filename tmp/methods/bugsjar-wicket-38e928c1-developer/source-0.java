	protected void renderChildHeaders(final HtmlHeaderContainer headerContainer,
		final Component rootComponent)
	{
		Args.notNull(headerContainer, "headerContainer");
		Args.notNull(rootComponent, "rootComponent");

		if (rootComponent instanceof MarkupContainer)
		{
			new DeepChildFirstVisitor()
			{
				@Override
				public void component(final Component component, final IVisit<Void> visit)
				{
					component.renderHead(headerContainer);
				}

				@Override
				public boolean preCheck(Component component)
				{
					return component.isVisibleInHierarchy();
				}
			}.visit(rootComponent);
		}
	}
