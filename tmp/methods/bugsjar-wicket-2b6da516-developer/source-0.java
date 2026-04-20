	private Url encryptUrl(final Url url)
	{
		if (url.getSegments().isEmpty() && url.getQueryParameters().isEmpty())
		{
			return url;
		}
		String encryptedUrlString = getCrypt().encryptUrlSafe(url.toString());

		Url encryptedUrl = new Url(url.getCharset());
		encryptedUrl.getSegments().add(encryptedUrlString);

		int numberOfSegments = url.getSegments().size();
		if (numberOfSegments == 0 && !url.getQueryParameters().isEmpty())
		{
			numberOfSegments = 1;
		}
		char[] encryptedChars = encryptedUrlString.toCharArray();
		int hash = 0;
		for (int segNo = 0; segNo < numberOfSegments; segNo++)
		{
			char a = encryptedChars[Math.abs(hash % encryptedChars.length)];
			hash++;
			char b = encryptedChars[Math.abs(hash % encryptedChars.length)];
			hash++;
			char c = encryptedChars[Math.abs(hash % encryptedChars.length)];

			String segment = "" + a + b + c;
			hash = hashString(segment);

			segment += String.format("%02x", Math.abs(hash % 256));
			encryptedUrl.getSegments().add(segment);
			hash = hashString(segment);
		}
		return encryptedUrl;
	}
	private Url decryptUrl(final Request request, final Url encryptedUrl)
	{
		if (encryptedUrl.getSegments().isEmpty() && encryptedUrl.getQueryParameters().isEmpty())
		{
			return encryptedUrl;
		}

		List<String> segments = encryptedUrl.getSegments();
		if (segments.size() < 2)
		{
			return null;
		}

		Url url = new Url(request.getCharset());
		try
		{
			String encryptedUrlString = segments.get(0);
			if (Strings.isEmpty(encryptedUrlString))
			{
				return null;
			}

			String decryptedUrl = getCrypt().decryptUrlSafe(encryptedUrlString);
			Url originalUrl = Url.parse(decryptedUrl, request.getCharset());

			int originalNumberOfSegments = originalUrl.getSegments().size();
			if (originalNumberOfSegments == 0 &&
				originalUrl.getQueryParameters().isEmpty() == false)
			{
				originalNumberOfSegments = 1;
			}
			int numberOfSegments = encryptedUrl.getSegments().size();

			char[] encryptedChars = encryptedUrlString.toCharArray();
			int hash = 0;

			int segNo;
			for (segNo = 1; segNo < numberOfSegments && segNo < originalNumberOfSegments + 1; segNo++)
			{
				char a = encryptedChars[Math.abs(hash % encryptedChars.length)];
				hash++;
				char b = encryptedChars[Math.abs(hash % encryptedChars.length)];
				hash++;
				char c = encryptedChars[Math.abs(hash % encryptedChars.length)];

				String segment = "" + a + b + c;
				hash = hashString(segment);

				segment += String.format("%02x", Math.abs(hash % 256));
				hash = hashString(segment);

				if (segment.equals(segments.get(segNo)) &&
					originalUrl.getSegments().size() >= segNo)
				{
					url.getSegments().add(originalUrl.getSegments().get(segNo - 1));
				}
				else
				{
					break;
				}
			}

			url.getQueryParameters().addAll(originalUrl.getQueryParameters());
		}
		catch (Exception e)
		{
			log.error("Error decrypting URL", e);
			url = null;
		}

		return url;
	}
