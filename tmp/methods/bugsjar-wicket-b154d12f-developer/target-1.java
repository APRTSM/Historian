	protected Link<?> newPagingNavigationLink(String id, IPageable pageable, int pageIndex)
	{
		return new PagingNavigationLink<Void>(id, pageable, pageIndex)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled()
			{
				return super.isEnabled() && PagingNavigation.this.isEnabled() &&
					PagingNavigation.this.isEnableAllowed();
			}
		};
	}
	protected Link<?> newPagingNavigationLink(String id, IPageable pageable, int pageNumber)
	{
		return new PagingNavigationLink<Void>(id, pageable, pageNumber)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled()
			{
				return super.isEnabled() && PagingNavigator.this.isEnabled() &&
					PagingNavigator.this.isEnableAllowed();
			}
		};

	}
	protected Link<?> newPagingNavigationIncrementLink(String id, IPageable pageable, int increment)
	{
		return new PagingNavigationIncrementLink<Void>(id, pageable, increment)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled()
			{
				return super.isEnabled() && PagingNavigator.this.isEnabled() &&
					PagingNavigator.this.isEnableAllowed();
			}
		};
	}
	protected PagingNavigation newNavigation(final IPageable pageable,
		final IPagingLabelProvider labelProvider)
	{
		return new PagingNavigation("navigation", pageable, labelProvider)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled()
			{
				return super.isEnabled() && PagingNavigator.this.isEnabled() &&
					PagingNavigator.this.isEnableAllowed();
			}
		};
	}
