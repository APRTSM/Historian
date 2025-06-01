	private void checkChildComponent(final Component controller)
	{
		if (controller == null)
		{
			throw new WicketRuntimeException("Could not find child with id: " + childId +
				" in the wicket:enclosure");
		}
		else if (controller == this)
		{
			throw new WicketRuntimeException(
				"Programming error: childComponent == enclose component; endless loop");
		}
	}
	protected final Component getChild()
	{
		if (childComponent == null)
		{
			// try to find child when queued
			childComponent = get(childId);
		}
		if (childComponent == null)
		{
			// try to find child when resolved
			childComponent = getChildComponent(new MarkupStream(getMarkup()), getEnclosureParent());
		}
		return childComponent;
	}
