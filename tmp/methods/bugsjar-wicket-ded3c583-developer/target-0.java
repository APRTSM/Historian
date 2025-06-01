	protected Url encryptEntireUrl(final Url url)
	{
		String encryptedUrlString = getCrypt().encryptUrlSafe(url.toString());

		Url encryptedUrl = new Url(url.getCharset());
		encryptedUrl.getSegments().add(encryptedUrlString);

		int numberOfSegments = url.getSegments().size() - 1;
		HashedSegmentGenerator generator = new HashedSegmentGenerator(encryptedUrlString);
		for (int segNo = 0; segNo < numberOfSegments; segNo++)
		{
			encryptedUrl.getSegments().add(generator.next());
		}
		return encryptedUrl;
	}
	public IRequestHandler mapRequest(final Request request)
	{
		Url url = decryptUrl(request, request.getUrl());

		if (url == null)
		{
			return null;
		}

		Request decryptedRequest = request.cloneWithUrl(url);

		IRequestHandler handler = wrappedMapper.mapRequest(decryptedRequest);

		if (handler != null)
		{
			handler = new RequestSettingRequestHandler(decryptedRequest, handler);
		}

		return handler;
	}
	protected Url encryptUrl(final Url url)
	{
		if (url.getSegments().size() > 0
			&& url.getSegments().get(0).equals(Application.get().getMapperContext().getNamespace()))
		{
			return encryptEntireUrl(url);
		}
		else
		{
			return encryptRequestListenerParameter(url);
		}
	}
	protected Url encryptRequestListenerParameter(final Url url)
	{
		Url encryptedUrl = new Url(url);

		for (Iterator<Url.QueryParameter> it = encryptedUrl.getQueryParameters().iterator(); it.hasNext();)
		{
			Url.QueryParameter qp = it.next();

			if (Strings.isEmpty(qp.getValue()) == true && Strings.isEmpty(qp.getName()) == false)
			{
				if (PageComponentInfo.parse(qp.getName()) != null)
				{
					it.remove();
					String encryptedParameterValue = getCrypt().encryptUrlSafe(qp.getName());
					Url.QueryParameter encryptedParameter
						= new Url.QueryParameter(ENCRYPTED_PAGE_COMPONENT_INFO_PARAMETER, encryptedParameterValue);
					encryptedUrl.getQueryParameters().add(0, encryptedParameter);
					break;
				}
			}
		}

		return encryptedUrl;
	}
	public int getCompatibilityScore(final Request request)
	{
		Url decryptedUrl = decryptUrl(request, request.getUrl());

		if (decryptedUrl == null)
		{
			return 0;
		}

		Request decryptedRequest = request.cloneWithUrl(decryptedUrl);

		return wrappedMapper.getCompatibilityScore(decryptedRequest);
	}
	protected Url decryptUrl(final Request request, final Url encryptedUrl)
	{
		Url url = decryptEntireUrl(request, encryptedUrl);

		if (url == null)
		{
			if (encryptedUrl.getSegments().size() > 0
				&& encryptedUrl.getSegments().get(0).equals(Application.get().getMapperContext().getNamespace()))
			{
				/*
				 * This URL should have been encrypted, but was not. We should refuse to handle this, except when
				 * there is more than one CryptoMapper installed, and the request was decrypted by some other
				 * CryptoMapper.
				 */
				if (request.getOriginalUrl().getSegments().size() > 0
					&& request.getOriginalUrl().getSegments().get(0).equals(Application.get().getMapperContext().getNamespace()))
				{
					return null;
				}
				else
				{
					return encryptedUrl;
				}
			}
		}

		if (url == null)
		{
			url = decryptRequestListenerParameter(request, encryptedUrl);
		}

		return url;
	}
	protected Url decryptRequestListenerParameter(final Request request, Url encryptedUrl)
	{
		Url url = new Url(encryptedUrl);

		url.getQueryParameters().clear();

		for (Url.QueryParameter qp : encryptedUrl.getQueryParameters())
		{
			if (Strings.isEmpty(qp.getValue()) && Strings.isEmpty(qp.getName()) == false)
			{
				if (PageComponentInfo.parse(qp.getName()) != null)
				{
					/*
					 * Plain text request listener parameter found. This should have been encrypted, so we
					 * refuse to map the request unless the original URL did not include this parameter, which
					 * case there are likely to be multiple cryptomappers installed.
					 */
					if (request.getOriginalUrl().getQueryParameter(qp.getName()) == null)
					{
						url.getQueryParameters().add(qp);
					}
					else
					{
						return null;
					}
				}
			}
			else if (ENCRYPTED_PAGE_COMPONENT_INFO_PARAMETER.equals(qp.getName()))
			{
				String encryptedValue = qp.getValue();

				if (Strings.isEmpty(encryptedValue))
				{
					url.getQueryParameters().add(qp);
				}
				else
				{
					String decryptedValue = null;

					try
					{
						decryptedValue = getCrypt().decryptUrlSafe(encryptedValue);
					}
					catch (Exception e)
					{
						log.error("Error decrypting encrypted request listener query parameter", e);
					}

					if (Strings.isEmpty(decryptedValue))
					{
						url.getQueryParameters().add(qp);
					}
					else
					{
						Url.QueryParameter decryptedParamter = new Url.QueryParameter(decryptedValue, "");
						url.getQueryParameters().add(0, decryptedParamter);
					}
				}
			}
			else
			{
				url.getQueryParameters().add(qp);
			}
		}

		return url;
	}
	protected Url decryptEntireUrl(final Request request, final Url encryptedUrl)
	{
		Url url = new Url(request.getCharset());

		List<String> encryptedSegments = encryptedUrl.getSegments();

		if (encryptedSegments.isEmpty())
		{
			return null;
		}

		/*
		 * The first encrypted segment contains an encrypted version of the entire plain text url.
		 */
		String encryptedUrlString = encryptedSegments.get(0);
		if (Strings.isEmpty(encryptedUrlString))
		{
			return null;
		}

		String decryptedUrl;
		try
		{
			decryptedUrl = getCrypt().decryptUrlSafe(encryptedUrlString);
		}
		catch (Exception e)
		{
			log.error("Error decrypting URL", e);
			return null;
		}

		if (decryptedUrl == null)
		{
			return null;
		}

		Url originalUrl = Url.parse(decryptedUrl, request.getCharset());

		int originalNumberOfSegments = originalUrl.getSegments().size();
		int encryptedNumberOfSegments = encryptedUrl.getSegments().size();

		if (originalNumberOfSegments > 0)
		{
			/*
			 * This should always be true. Home page URLs are the only ones without
			 * segments, and we dont encrypt those with this method.
			 *
			 * We always add the first segment of the URL, because we encrypt a URL like:
			 *	/path/to/something
			 * to:
			 *	/encrypted_full/hash/hash
			 *
			 * Notice the consistent number of segments. If we applied the following relative URL:
			 *	../../something
			 * then the resultant URL would be:
			 *	/something
			 *
			 * Hence, the mere existence of the first, encrypted version of complete URL, segment
			 * tells us that the first segment of the original URL is still to be used.
			 */
			url.getSegments().add(originalUrl.getSegments().get(0));
		}

		HashedSegmentGenerator generator = new HashedSegmentGenerator(encryptedUrlString);
		int segNo = 1;
		for (; segNo < encryptedNumberOfSegments; segNo++)
		{
			if (segNo > originalNumberOfSegments)
			{
				break;
			}

			String next = generator.next();
			String encryptedSegment = encryptedSegments.get(segNo);
			if (!next.equals(encryptedSegment))
			{
				/*
				 * This segment received from the browser is not the same as the expected segment generated
				 * by the HashSegmentGenerator. Hence it, and all subsequent segments are considered plain
				 * text siblings of the original encrypted url.
				 */
				break;
			}

			/*
			 * This segments matches the expected checksum, so we add the corresponding segment from the
			 * original URL.
			 */
			url.getSegments().add(originalUrl.getSegments().get(segNo));
		}
		/*
		 * Add all remaining segments from the encrypted url as plain text segments.
		 */
		for (; segNo < encryptedNumberOfSegments; segNo++)
		{
			// modified or additional segment
			url.getSegments().add(encryptedUrl.getSegments().get(segNo));
		}

		url.getQueryParameters().addAll(originalUrl.getQueryParameters());
		// WICKET-4923 additional parameters
		url.getQueryParameters().addAll(encryptedUrl.getQueryParameters());

		return url;
	}
