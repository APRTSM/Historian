	public void dequeue(DequeueContext dequeue)
	{
		while (dequeue.isAtOpenOrOpenCloseTag())
		{
			ComponentTag tag = dequeue.takeTag();
	
			// see if child is already added to parent

			Component child = get(tag.getId());

			if (child == null)
			{
				// the container does not yet have a child with this id, see if we can
				// dequeue
				
				child = dequeue.findComponentToDequeue(tag);

				if (child != null)
				{
					addDequeuedComponent(child, tag);
					if (child instanceof IQueueRegion)
					{
						((MarkupContainer)child).dequeue();
					}
				}
			}
			if (child == null || !(child instanceof MarkupContainer))
			{
				// could not dequeue, or does not contain children
	
				if (tag.isOpen())
				{
					dequeue.skipToCloseTag();
				}
			}
			else
			{
				MarkupContainer container = (MarkupContainer)child;
				if (container instanceof IQueueRegion)
				{
					// if this is a dequeue container we do not process its markup, it will do so
					// itself when it is dequeued for the first time
					if (tag.isOpen())
					{
						dequeue.skipToCloseTag();
					}
				}
				else if (tag.isOpen())
				{
					// this component has more markup and possibly more children to dequeue
					dequeue.pushContainer(container);
					container.dequeue(dequeue);
					dequeue.popContainer();
				}
			}

			if (tag.isOpen() && !tag.hasNoCloseTag())
			{
				// pull the close tag off
				ComponentTag close = dequeue.takeTag();
				if (!close.closes(tag))
				{
					// sanity check
					throw new IllegalStateException(String.format("Tag '%s' should be the closing one for '%s'", close, tag));
				}
			}
		}

	}
	private void removedComponent(final Component component)
	{
		// Notify Page that component is being removed
		final Page page = component.findPage();
		if (page != null)
		{
			page.componentRemoved(component);
		}

		component.detach();

		component.internalOnRemove();

		// Component is removed
		component.setParent(null);
	}
