	private void decrementFenceCount()
	{
		Integer count = fence.getMetaData(FENCE_KEY);
		count = (count == null || count == 1) ? null : count - 1;
		fence.setMetaData(FENCE_KEY, count);
	}
	protected FeedbackMessagesModel newFeedbackMessagesModel()
	{
		return new FeedbackMessagesModel(this)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected List<FeedbackMessage> collectMessages(Component panel,
					IFeedbackMessageFilter filter)
			{
				if (fence == null)
				{
					// this is the catch-all panel

					return new FeedbackCollector(panel.getPage())
					{
						@Override
						protected boolean shouldRecurseInto(Component component)
						{
							return !componentIsMarkedAsFence(component);
						}
					}.collect(filter);
				}
				else
				{
					// this is a fenced panel

					return new FeedbackCollector(fence)
					{
						@Override
						protected boolean shouldRecurseInto(Component component)
						{
							// only recurse into components that are not fences
							return !componentIsMarkedAsFence(component);
						}
					}.setIncludeSession(false).collect(filter);
				}
			}
		};
	}
	private void incrementFenceCount()
	{
		Integer count = fence.getMetaData(FENCE_KEY);
		count = count == null ? 1 : count + 1;
		fence.setMetaData(FENCE_KEY, count);
	}
	protected void onRemove()
	{
		super.onRemove();
		if (fence != null)
		{
			// decrement the fence count

			decrementFenceCount();
		}
	}
	protected void onReAdd()
	{
		if (this.fence != null)
		{
			// The fence mark is removed when the feedback panel is removed from the hierarchy.
			// see onRemove().
			// when the panel is re-added, we recreate the fence mark.
			incrementFenceCount();
		}
		super.onReAdd();
	}
	private boolean componentIsMarkedAsFence(Component component)
	{
		return component.getMetaData(FENCE_KEY) != null;
	}
	public FencedFeedbackPanel(String id, Component fence, IFeedbackMessageFilter filter)
	{
		super(id, filter);
		this.fence = fence;
		if (fence != null)
		{
			incrementFenceCount();
		}
	}
