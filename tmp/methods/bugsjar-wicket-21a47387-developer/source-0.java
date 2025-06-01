	private HeaderItem getItemToBeRendered(HeaderItem item)
	{
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

		this.itemsToBeRendered = new LinkedHashMap<HeaderItem, RecordedHeaderItem>();
		this.domReadyItemsToBeRendered = new ArrayList<OnDomReadyHeaderItem>();
		this.loadItemsToBeRendered = new ArrayList<OnLoadHeaderItem>();
	}
		public RecordedHeaderItem(HeaderItem item)
		{
			this.item = item;
			this.locations = new ArrayList<RecordedHeaderItemLocation>();
		}
