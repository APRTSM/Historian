		public boolean isVisible()
		{
			if (!super.isVisible())
			{
				return false;
			}

			toolbars.configure();

			Boolean visible = toolbars.visitChildren(new IVisitor<Component, Boolean>()
			{
				public void component(Component object, IVisit<Boolean> visit)
				{
					object.configure();
					if (object.isVisible())
					{
						visit.stop(Boolean.TRUE);
					}
					else
					{
						visit.dontGoDeeper();
					}
				}
			});
			return visible == Boolean.TRUE;
		}
	private void addToolbar(final AbstractToolbar toolbar, final ToolbarsContainer container)
	{
		if (toolbar == null)
		{
			throw new IllegalArgumentException("argument [toolbar] cannot be null");
		}

		container.getRepeatingView().add(toolbar);
	}
		private ToolbarsContainer(final String id)
		{
			super(id);
			toolbars = new RepeatingView("toolbars");
			add(toolbars);
		}
		public RepeatingView getRepeatingView()
		{
			return toolbars;
		}
