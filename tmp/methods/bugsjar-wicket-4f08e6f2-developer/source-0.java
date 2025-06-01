	private Url decryptUrl(final Request request, final Url encryptedUrl)
	{
		if (encryptedUrl.getSegments().isEmpty() && encryptedUrl.getQueryParameters().isEmpty())
		{
			return encryptedUrl;
		}

		List<String> encryptedSegments = encryptedUrl.getSegments();
		if (encryptedSegments.size() < 1)
		{
			return null;
		}

		Url url = new Url(request.getCharset());
		try
		{
			String encryptedUrlString = encryptedSegments.get(0);
			if (Strings.isEmpty(encryptedUrlString))
			{
				return null;
			}

			String decryptedUrl = getCrypt().decryptUrlSafe(encryptedUrlString);
			if (decryptedUrl == null)
			{
				return null;
			}
			Url originalUrl = Url.parse(decryptedUrl, request.getCharset());

			int originalNumberOfSegments = originalUrl.getSegments().size();
			int encryptedNumberOfSegments = encryptedUrl.getSegments().size();

			HashedSegmentGenerator generator = new HashedSegmentGenerator(encryptedUrlString);
			int segNo = 1;
			for (; segNo < encryptedNumberOfSegments; segNo++)
			{
				if (segNo > originalNumberOfSegments ||
					!generator.next().equals(encryptedSegments.get(segNo)))
				{
					break;
				}

				// unmodified segment
				url.getSegments().add(originalUrl.getSegments().get(segNo - 1));
			}
			for (; segNo < encryptedNumberOfSegments; segNo++)
			{
				// modified or additional segment
				url.getSegments().add(encryptedUrl.getSegments().get(segNo));
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
