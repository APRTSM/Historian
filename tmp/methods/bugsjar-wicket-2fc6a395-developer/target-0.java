	public static String join(final String separator, final String... fragments)
	{
		if ((fragments == null) || (fragments.length < 1))
		{
			// no elements
			return "";
		}
		else if (fragments.length < 2)
		{
			// single element
			return fragments[0];
		}
		else
		{
			// two or more elements
			StringBuilder buff = new StringBuilder(128);
			if (fragments[0] != null)
			{
				buff.append(fragments[0]);
			}
			for (int i = 1; i < fragments.length; i++)
			{
				String fragment = fragments[i];
				if ((fragments[i - 1] != null) || (fragment != null))
				{
					boolean lhsClosed = fragments[i - 1].endsWith(separator);
					boolean rhsClosed = fragment.startsWith(separator);
					if (!Strings.isEmpty(separator) && lhsClosed && rhsClosed)
					{
						buff.append(fragment.substring(1));
					}
					else if (!lhsClosed && !rhsClosed)
					{
						if (!Strings.isEmpty(fragment))
						{
							buff.append(separator);
						}
						buff.append(fragment);
					}
					else
					{
						buff.append(fragment);
					}
				}
			}
			return buff.toString();
		}
	}
