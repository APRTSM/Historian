	public final int getCurrentPage()
	{
		// If first cell is out of range, bring page back into range
		while ((currentPage > 0) && ((currentPage * rowsPerPage) >= getList().size()))
		{
			currentPage--;
		}

		return currentPage;
	}
