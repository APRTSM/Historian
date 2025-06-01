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
			boolean groupPassed = true;
			for (String detectionString : detectionGroup)
			{
				if (!userAgent.contains(detectionString))
				{
					groupPassed = false;
					break;
				}
			}
			if (groupPassed)
			{
				return true;
			}
		}

		return false;
	}
