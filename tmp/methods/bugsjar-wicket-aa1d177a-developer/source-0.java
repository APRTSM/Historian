	private void addToolbar(final AbstractToolbar toolbar, final RepeatingView container)
	{
		if (toolbar == null)
		{
			throw new IllegalArgumentException("argument [toolbar] cannot be null");
		}

		container.add(toolbar);
	}
		private ToolbarsContainer(final String id)
		{
			super(id);
		}
