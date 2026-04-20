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
			else
			{
				return null; // dont know
			}
		}
		return DequeueTagAction.DEQUEUE;
	}
