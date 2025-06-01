	protected Link<?> newPagingNavigationLink(String id, IPageable pageable, int pageIndex)
	{
		return new PagingNavigationLink<Void>(id, pageable, pageIndex)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled()
			{
				return PagingNavigation.this.isEnabled() && PagingNavigation.this.isEnableAllowed();
			}
		};
	}
