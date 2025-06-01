	protected WebMarkupContainer newBodyContainer(final String id)
	{
		return new WebMarkupContainer(id)
		{
			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(getRowCount() > 0);
			}
		};
	}
