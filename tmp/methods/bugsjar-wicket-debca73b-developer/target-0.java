	public final void treeNodesInserted(TreeModelEvent e)
	{
		if (dirtyAll)
		{
			return;
		}

		// get the parent node of inserted nodes
		Object parentNode = e.getTreePath().getLastPathComponent();
		TreeItem parentItem = nodeToItemMap.get(parentNode);


		if (parentItem != null && isNodeVisible(parentNode))
		{
			List<?> eventChildren = Arrays.asList(e.getChildren());

			// parentNode was a leaf before this insertion event only if every one of
			// its current children is in the event's list of children
			boolean wasLeaf = true;
			int nodeChildCount = getChildCount(parentNode);
			for (int i = 0; wasLeaf && i < nodeChildCount; i++)
			{
				wasLeaf = eventChildren.contains(getChildAt(parentNode, i));
			}
			// if parent was a leaf, testing if wasn't an not presented root
			if (wasLeaf && !(parentItem.getParentItem() == null && isRootLess()))
			{
				// parentNode now has children for the first time, so we may need to invalidate
				// grandparent so that parentNode's junctionLink gets rebuilt with a plus/minus link
				Object grandparentNode = getParentNode(parentNode);
				// not invalidating if the grandparent is a not presented root
				if (!(getParentNode(grandparentNode) == null && isRootLess()))
				{
					invalidateNodeWithChildren(grandparentNode);
				}
				getTreeState().expandNode(parentNode);
			}
			else
			{
				if (isNodeExpanded(parentNode))
				{
					List<TreeItem> itemChildren = parentItem.getChildren();
					int childLevel = parentItem.getLevel() + 1;
					final int[] childIndices = e.getChildIndices();
					for (int i = 0; i < eventChildren.size(); ++i)
					{
						TreeItem item = newTreeItem(parentItem, eventChildren.get(i), childLevel);
						itemContainer.add(item);

						if (itemChildren != null)
						{
							itemChildren.add(childIndices[i], item);
							markTheLastButOneChildDirty(parentItem, item);
						}

						if (!dirtyItems.contains(item))
						{
							dirtyItems.add(item);
						}

						if (!dirtyItemsCreateDOM.contains(item) &&
							!item.hasParentWithChildrenMarkedToRecreation())
						{
							dirtyItemsCreateDOM.add(item);
						}
					}
				}
			}
		}
	}
