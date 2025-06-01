	public final void removeAll()
	{
		if (children != null)
		{
			addStateChange();

			// Loop through child components
			int size = children_size();
			for (int i = 0; i < size; i++)
			{
				Object childObject = children_get(i, false);
				if (childObject instanceof Component)
				{
					// Get next child
					final Component child = (Component)childObject;

					// Do not call remove() because the state change would than be
					// recorded twice.
					child.internalOnRemove();
					child.detachModel();
					child.setParent(null);
				}
			}

			children = null;
		}
	}
