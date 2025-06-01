	protected boolean isValidPath(String path)
	{
		if (path == null)
		{
			return false;
		}

		Matcher pathMatcher = Pattern.compile(PATH_PATTERN).matcher(path);

		if (!pathMatcher.matches())
		{
			return false;
		}

		int slash2Count = countToken("//", path);
		if (isOff(ALLOW_2_SLASHES) && (slash2Count > 0))
		{
			return false;
		}

		int slashCount = countToken("/", path);
		int dot2Count = countToken("/..", path);
		if (dot2Count > 0)
		{
			if ((slashCount - slash2Count - 1) <= dot2Count)
			{
				return false;
			}
		}

		return true;
	}
