	private HeaderItem getItemToBeRendered(HeaderItem item)
	{
		while (item instanceof IWrappedHeaderItem)
		{
			item = ((IWrappedHeaderItem)item).getWrapped();
		}
		if (getRealResponse().wasRendered(item))
		{
			return NoHeaderItem.get();
		}

		getRealResponse().markRendered(item);
		HeaderItem bundle = Application.get().getResourceBundles().findBundle(item);
		if (bundle == null)
		{
			return item;
		}

		for (HeaderItem curProvided : bundle.getProvidedResources())
		{
			getRealResponse().markRendered(curProvided);
		}

		return bundle;
	}
	public ResourceAggregator(IHeaderResponse real)
	{
		super(real);

		itemsToBeRendered = new LinkedHashMap<HeaderItem, RecordedHeaderItem>();
		domReadyItemsToBeRendered = new ArrayList<OnDomReadyHeaderItem>();
		loadItemsToBeRendered = new ArrayList<OnLoadHeaderItem>();
	}
		public RecordedHeaderItem(HeaderItem item)
		{
			this.item = item;
			locations = new ArrayList<RecordedHeaderItemLocation>();
		}
