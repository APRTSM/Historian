	public PageWindow createPageWindow(int pageId, int size)
	{
		int index = getWindowIndex(pageId);

		// if we found the page window, mark it as invalid
		if (index != -1)
		{
			removeWindowIndex(pageId);
			(windows.get(index)).pageId = -1;
		}

		// if we are not going to reuse a page window (because it's not on
		// indexPointer position or because we didn't find it), increment the
		// indexPointer
		if (index == -1 || index != indexPointer)
		{
			index = incrementIndexPointer();
		}

		PageWindowInternal window = allocatePageWindow(index, size);
		window.pageId = pageId;

		putWindowIndex(pageId, index);
		return new PageWindow(window);
	}
	private void putWindowIndex(int pageId, int windowIndex)
	{
		if (idToWindowIndex != null && pageId != -1 && windowIndex != -1)
		{
			Integer oldPageId = windowIndexToPageId.remove(windowIndex);
			if (oldPageId != null)
			{
				idToWindowIndex.remove(oldPageId);
			}
			idToWindowIndex.put(pageId, windowIndex);
			windowIndexToPageId.put(windowIndex, pageId);
		}
	}
	private void mergeWindowWithNext(int index)
	{
		if (index < windows.size() - 1)
		{
			PageWindowInternal window = windows.get(index);
			PageWindowInternal next = windows.get(index + 1);
			window.filePartSize += next.filePartSize;

			windows.remove(index + 1);
			idToWindowIndex = null; // reset index
			windowIndexToPageId = null;
		}
	}
	private void removeWindowIndex(int pageId)
	{
		Integer windowIndex = idToWindowIndex.remove(pageId);
		if (windowIndex != null)
		{
			windowIndexToPageId.remove(windowIndex);
		}
	}
	private void rebuildIndices()
	{
		idToWindowIndex = null;
		idToWindowIndex = new IntHashMap<Integer>();
		windowIndexToPageId = null;
		windowIndexToPageId = new IntHashMap<Integer>();
		for (int i = 0; i < windows.size(); ++i)
		{
			PageWindowInternal window = windows.get(i);
			putWindowIndex(window.pageId, i);
		}
	}
	private void splitWindow(int index, int size)
	{
		PageWindowInternal window = windows.get(index);
		int delta = window.filePartSize - size;

		if (index == windows.size() - 1)
		{
			// if this is last window
			totalSize -= delta;
			window.filePartSize = size;
		}
		else if (window.filePartSize != size)
		{
			PageWindowInternal newWindow = new PageWindowInternal();
			newWindow.pageId = -1;
			window.filePartSize = size;

			windows.add(index + 1, newWindow);

			newWindow.filePartOffset = getWindowFileOffset(index + 1);
			newWindow.filePartSize = delta;
		}

		idToWindowIndex = null;
		windowIndexToPageId = null;
	}
