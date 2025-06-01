	private void copyRequestsInOrder(RequestData[] copy)
	{
		if (hasBufferRolledOver())
		{
			// first copy the oldest requests stored behind the cursor into the copy
			int oldestPos = indexInWindow + 1;
			if (oldestPos < requestWindow.length)
				arraycopy(requestWindow, oldestPos, copy, 0, requestWindow.length - oldestPos);

			// then append the newer requests stored from index 0 til the cursor position.
			arraycopy(requestWindow, 0, copy, requestWindow.length - oldestPos, indexInWindow);
		}
		else
		{
			arraycopy(requestWindow, 0, copy, 0, indexInWindow);
		}
	}
