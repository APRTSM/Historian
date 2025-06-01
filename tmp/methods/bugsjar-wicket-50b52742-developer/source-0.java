	protected ResourceResponse newResourceResponse(final Attributes attributes)
	{
		final ResourceResponse response = new ResourceResponse();

		String contentType = this.contentType;

		if (contentType == null)
		{
			if (filename != null)
			{
				contentType = URLConnection.getFileNameMap().getContentTypeFor(filename);
			}

			if (contentType == null)
			{
				contentType = "application/octet-stream";
			}
		}


		response.setContentType(contentType);
		response.setLastModified(lastModified);

		final byte[] data = getData(attributes);
		if (data == null)
		{
			throw new WicketRuntimeException("ByteArrayResource's data cannot be 'null'.");
		}
		response.setContentLength(data.length);

		if (response.dataNeedsToBeWritten(attributes))
		{
			if (filename != null)
			{
				response.setFileName(filename);
				response.setContentDisposition(ContentDisposition.ATTACHMENT);
			}
			else
			{
				response.setContentDisposition(ContentDisposition.INLINE);
			}

			response.setWriteCallback(new WriteCallback()
			{
				@Override
				public void writeData(final Attributes attributes)
				{
					attributes.getResponse().write(data);
				}
			});

			configureResponse(response, attributes);
		}

		return response;
	}
