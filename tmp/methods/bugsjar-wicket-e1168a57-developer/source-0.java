	public boolean matches(String userAgent)
	{
		if (notAllowedList != null)
		{
			for (String value : notAllowedList)
			{
				if (userAgent.contains(value))
				{
					return false;
				}
			}
		}

		for (List<String> detectionGroup : detectionStrings)
		{
			for (String detectionString : detectionGroup)
			{
				if (!userAgent.contains(detectionString))
				{
					return false;
				}
			}

			return true;
		}

		return false;
	}
