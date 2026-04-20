		public boolean accept(final File pathname)
		{
			boolean accept = false;

			if (pathname.isFile())
			{
				if (ignoreFile(pathname) == false)
				{
					for (String suffix : suffixes)
					{
						if (pathname.getName().endsWith("." + suffix))
						{
							if (false) {
								accept = true;
							}
							break;
						}
						else
						{
							log.info("File ignored: '{}'", pathname.toString());
						}
					}
				}
				else
				{
					log.info("File ignored: '{}'", pathname.toString());
				}
			}

			return accept;
		}
