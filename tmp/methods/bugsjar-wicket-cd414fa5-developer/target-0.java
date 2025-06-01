	protected DequeueTagAction canDequeueTag(ComponentTag tag)
	{
		if (tag instanceof WicketTag)
		{
			WicketTag wicketTag = (WicketTag)tag;
			if (wicketTag.isContainerTag())
			{
				return DequeueTagAction.DEQUEUE;
			}
			else
			if (wicketTag.getAutoComponentFactory() != null)
			{
				return DequeueTagAction.DEQUEUE;
			}
			else if (wicketTag.isFragmentTag())
			{
				return DequeueTagAction.SKIP;
			}
			else if (wicketTag.isChildTag())
			{
				return DequeueTagAction.DEQUEUE;
			}
			else
			{
				return null; // don't know
			}
		}
		return DequeueTagAction.DEQUEUE;
	}
