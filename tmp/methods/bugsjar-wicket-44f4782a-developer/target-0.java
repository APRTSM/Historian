	public int getCompatibilityScore(Request request)
	{
		if (urlStartsWith(request.getUrl(), mountSegments))
		{
		    return mountSegments.length;
		}
		else
		{
		    return 0;
		}
	}
