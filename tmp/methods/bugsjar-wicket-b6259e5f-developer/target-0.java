	private void copyRequestsInOrder(RequestData[] copy)
	{
		int destPos = 0;

		if (hasBufferRolledOver())
		{
			destPos = requestWindow.length - indexInWindow;

			// first copy the oldest requests stored behind the cursor into the copy
			arraycopy(requestWindow, indexInWindow, copy, 0, destPos);
		}

		arraycopy(requestWindow, 0, copy, destPos, indexInWindow);
	}
