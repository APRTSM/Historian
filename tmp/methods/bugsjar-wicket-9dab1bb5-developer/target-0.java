	public String toString(final Locale locale)
	{
		if (getMilliseconds() >= 0)
		{
			if (days() >= 1.0)
			{
				return unitString(days(), "day", locale);
			}

			if (hours() >= 1.0)
			{
				return unitString(hours(), "hour", locale);
			}

			if (minutes() >= 1.0)
			{
				return unitString(minutes(), "minute", locale);
			}

			if (seconds() >= 1.0)
			{
				return unitString(seconds(), "second", locale);
			}

			return unitString(getMilliseconds(), "millisecond", locale);
		}
		else
		{
			return "N/A";
		}
	}
