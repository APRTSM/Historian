	protected final Component getChild()
	{
		if (childComponent == null)
		{
			// try to find child when queued
			childComponent = resolveChild(this);
		}
		if (childComponent == null)
		{
			// try to find child when resolved
			childComponent = getChildComponent(new MarkupStream(getMarkup()), getEnclosureParent());
		}
		return childComponent;
	}
	public DequeueContext newDequeueContext()
	{
		IMarkupFragment markup = getMarkupSourcingStrategy().getMarkup(this, null);
		if (markup == null)
		{
			return null;
		}

		return new DequeueContext(markup, this, true);
	}
	private Component resolveChild(MarkupContainer container)
	{
		Component childController = container.get(childId);

		Iterator<Component> children = container.iterator();

		while (children.hasNext() && childController == null)
		{
			Component transparentChild = children.next();

			if(transparentChild instanceof TransparentWebMarkupContainer)
			{
				childController = resolveChild((MarkupContainer)transparentChild);
			}
		}

		return childController;
	}
