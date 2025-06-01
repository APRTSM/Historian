		public IRequestTargetUrlCodingStrategy strategyForPath(String path)
		{
			if (path == null)
			{
				throw new IllegalArgumentException("Argument [[path]] cannot be null");
			}
			if (caseSensitiveMounts == false)
			{
				path = path.toLowerCase();
			}
			for (final Iterator it = map.entrySet().iterator(); it.hasNext();)
			{
				final Map.Entry entry = (Entry)it.next();
				final String key = (String)entry.getKey();
				if (path.startsWith(key))
				{
					/*
					 * We need to match /mount/point or
					 * /mount/point/with/extra/path, but not /mount/pointXXX
					 */
					String remainder = path.substring(key.length());
					if (remainder.length() == 0 || remainder.startsWith("/"))
						return (IRequestTargetUrlCodingStrategy)entry.getValue();
				}
			}
			return null;
		}
