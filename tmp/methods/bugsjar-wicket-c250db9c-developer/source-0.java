	protected boolean isValidAuthority(String authority)
	{
		if (authority == null)
		{
			return false;
		}

		Matcher authorityMatcher = Pattern.compile(AUTHORITY_PATTERN).matcher(authority);
		if (!authorityMatcher.matches())
		{
			return false;
		}

		boolean ipV4Address = false;
		boolean hostname = false;
		// check if authority is IP address or hostname
		String hostIP = authorityMatcher.group(PARSE_AUTHORITY_HOST_IP);
		Matcher matchIPV4Pat = Pattern.compile(IP_V4_DOMAIN_PATTERN).matcher(hostIP);
		ipV4Address = matchIPV4Pat.matches();

		if (ipV4Address)
		{
			// this is an IP address so check components
			for (int i = 1; i <= 4; i++)
			{
				String ipSegment = matchIPV4Pat.group(i);
				if (ipSegment == null || ipSegment.length() <= 0)
				{
					return false;
				}

				try
				{
					if (Integer.parseInt(ipSegment) > 255)
					{
						return false;
					}
				}
				catch (NumberFormatException e)
				{
					return false;
				}

			}
		}
		else
		{
			// Domain is hostname name
			hostname = Pattern.compile(DOMAIN_PATTERN).matcher(hostIP).matches();
		}

		// rightmost hostname will never start with a digit.
		if (hostname)
		{
			// LOW-TECH FIX FOR VALIDATOR-202
			// TODO: Rewrite to use ArrayList and .add semantics: see
			// VALIDATOR-203
			char[] chars = hostIP.toCharArray();
			int size = 1;
			for (char ch : chars)
			{
				if (ch == '.')
				{
					size++;
				}
			}
			String[] domainSegment = new String[size];
			boolean match = true;
			int segmentCount = 0;
			int segmentLength = 0;

			while (match)
			{
				Matcher atomMatcher = Pattern.compile(ATOM_PATTERN).matcher(hostIP);
				match = atomMatcher.find();
				if (match)
				{
					domainSegment[segmentCount] = atomMatcher.group(1);
					segmentLength = domainSegment[segmentCount].length() + 1;
					hostIP = (segmentLength >= hostIP.length()) ? ""
						: hostIP.substring(segmentLength);

					segmentCount++;
				}
			}

			if (segmentCount > 1)
			{
				String topLevel = domainSegment[segmentCount - 1];
				if (topLevel.length() < 2 || topLevel.length() > 4)
				{
					return false;
				}

				// First letter of top level must be a alpha
				Matcher alphaMatcher = Pattern.compile(ALPHA_PATTERN).matcher(
					topLevel.substring(0, 1));
				if (!alphaMatcher.matches())
				{
					return false;
				}
			}
		}

		if (!hostname && !ipV4Address)
		{
			return false;
		}

		String port = authorityMatcher.group(PARSE_AUTHORITY_PORT);
		if (port != null)
		{
			Matcher portMatcher = Pattern.compile(PORT_PATTERN).matcher(port);
			if (!portMatcher.matches())
			{
				return false;
			}
		}

		String extra = authorityMatcher.group(PARSE_AUTHORITY_EXTRA);
		if (!isBlankOrNull(extra))
		{
			return false;
		}

		return true;
	}
