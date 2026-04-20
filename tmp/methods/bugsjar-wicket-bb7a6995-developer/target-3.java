	public MockServletContext(final Application application, final String path)
	{
		this.application = application;

		webappRoot = null;
		if (path != null)
		{
			webappRoot = new File(path);
			if (!webappRoot.exists() || !webappRoot.isDirectory())
			{
				log.warn("WARNING: The webapp root directory is invalid: " + path);
				webappRoot = null;
			}
		}

		// assume we're running in maven or an eclipse project created by maven,
		// so the sessions directory will be created inside the target directory,
		// and will be cleaned up with a mvn clean

		File file = new File("target/work/");
		file.mkdirs();
		attributes.put("javax.servlet.context.tempdir", file);

		mimeTypes.put("html", "text/html");
		mimeTypes.put("htm", "text/html");
		mimeTypes.put("css", "text/css");
		mimeTypes.put("xml", "text/xml");
		mimeTypes.put("js", "text/javascript");
		mimeTypes.put("gif", "image/gif");
		mimeTypes.put("jpg", "image/jpeg");
		mimeTypes.put("png", "image/png");
	}
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
	protected ResourceResponse newResourceResponse(Attributes attributes)
	{
		final ResourceResponse resourceResponse = new ResourceResponse();

		if (resourceResponse.dataNeedsToBeWritten(attributes))
		{
			// get resource stream
			final IResourceStream resourceStream = getResourceStream();

			// bail out if resource stream could not be found
			if (resourceStream == null)
				return sendResourceError(resourceResponse, HttpServletResponse.SC_NOT_FOUND,
					"Unable to find resource");

			final String contentType;
			if (Application.exists())
			{
				contentType = Application.get().getMimeType(path);
			}
			else
			{
				contentType = resourceStream.getContentType();
			}
			// set Content-Type (may be null)
			resourceResponse.setContentType(contentType);

			// add Last-Modified header (to support HEAD requests and If-Modified-Since)
			final Time lastModified = resourceStream.lastModifiedTime();

			if (lastModified != null)
				resourceResponse.setLastModified(lastModified);

			try
			{
				// read resource data
				final byte[] bytes;

				try
				{
					bytes = IOUtils.toByteArray(resourceStream.getInputStream());
				}
				finally
				{
					resourceStream.close();
				}

				final byte[] processed = processResponse(attributes, bytes);

				// send Content-Length header
				resourceResponse.setContentLength(processed.length);

				// send response body with resource data
				resourceResponse.setWriteCallback(new WriteCallback()
				{
					@Override
					public void writeData(Attributes attributes)
					{
						attributes.getResponse().write(processed);
					}
				});
			}
			catch (IOException e)
			{
				log.debug(e.getMessage(), e);
				return sendResourceError(resourceResponse, 500, "Unable to read resource stream");
			}
			catch (ResourceStreamNotFoundException e)
			{
				log.debug(e.getMessage(), e);
				return sendResourceError(resourceResponse, 500, "Unable to open resource stream");
			}
		}

		return resourceResponse;
	}
	protected ResourceResponse newResourceResponse(Attributes attributes)
	{
		ResourceResponse data = new ResourceResponse();
		Time lastModifiedTime = stream.lastModifiedTime();
		if (lastModifiedTime != null)
		{
			data.setLastModified(lastModifiedTime);
		}

		// performance check; don't bother to do anything if the resource is still cached by client
		if (data.dataNeedsToBeWritten(attributes))
		{
			InputStream inputStream = null;
			if (stream instanceof IResourceStreamWriter == false)
			{
				try
				{
					inputStream = stream.getInputStream();
				}
				catch (ResourceStreamNotFoundException e)
				{
					data.setError(HttpServletResponse.SC_NOT_FOUND);
					close();
				}
			}

			data.setContentDisposition(contentDisposition);
			Bytes length = stream.length();
			if (length != null)
			{
				data.setContentLength(length.bytes());
			}
			data.setFileName(fileName);

			final String contentType;
			if (fileName != null && Application.exists())
			{
				contentType = Application.get().getMimeType(fileName);
			}
			else
			{
				contentType = stream.getContentType();
			}
			data.setContentType(contentType);
			data.setTextEncoding(textEncoding);

			if (stream instanceof IResourceStreamWriter)
			{
				data.setWriteCallback(new WriteCallback()
				{
					@Override
					public void writeData(Attributes attributes)
					{
						((IResourceStreamWriter)stream).write(attributes.getResponse());
						close();
					}
				});
			}
			else
			{
				final InputStream s = inputStream;
				data.setWriteCallback(new WriteCallback()
				{
					@Override
					public void writeData(Attributes attributes)
					{
						try
						{
							writeStream(attributes, s);
						}
						finally
						{
							close();
						}
					}
				});
			}
		}

		return data;
	}
