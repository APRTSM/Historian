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
		mimeTypes.put("js", "text/plain");
		mimeTypes.put("gif", "image/gif");
		mimeTypes.put("jpg", "image/jpeg");
		mimeTypes.put("png", "image/png");
	}
	protected ResourceResponse newResourceResponse(final Attributes attributes)
	{
		final ResourceResponse response = new ResourceResponse();

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
