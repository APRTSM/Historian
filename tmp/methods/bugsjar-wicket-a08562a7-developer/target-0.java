	public <T> HeadersToolbar(final DataTable<T> table, final ISortStateLocator stateLocator)
	{
		super(table);

		RepeatingView headers = new RepeatingView("headers");
		add(headers);

		final List<IColumn<T>> columns = table.getColumns();
		for (final IColumn<T> column : columns)
		{
			AbstractItem item = new AbstractItem(headers.newChildId());
			headers.add(item);

			WebMarkupContainer header = null;
			if (column.isSortable())
			{
				header = newSortableHeader("header", column.getSortProperty(), stateLocator);
			}
			else
			{
				header = new WebMarkupContainer("header");
			}

			if (column instanceof IStyledColumn)
			{
				header.add(new DataTable.CssAttributeBehavior()
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected String getCssClass()
					{
						return ((IStyledColumn<?>)column).getCssClass();
					}
				});
			}

			item.add(header);
			item.setRenderBodyOnly(true);
			if (header instanceof Border)
			{
				((Border)header).addToBody(column.getHeader("label"));
			}
			else
			{
				header.add(column.getHeader("label"));
			}

		}
	}
