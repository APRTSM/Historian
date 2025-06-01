	protected void onRender()
	{
		// Force multi-part on if any child form component is multi-part
		visitFormComponents(new FormComponent.AbstractVisitor()
		{
			@Override
			public void onFormComponent(FormComponent<?> formComponent)
			{
				if (formComponent.isVisible() && formComponent.isMultiPart())
				{
					setMultiPart(true);
				}
			}
		});

		super.onRender();
	}
	public void setMultiPart(boolean multiPart)
	{
		this.multiPart = multiPart;
	}
	private boolean isMultiPart()
	{
		if (multiPart)
		{
			return true;
		}
		else
		{
			final boolean[] anyEmbeddedMultipart = new boolean[] { false };
			visitChildren(Form.class, new IVisitor<Form<?>>()
			{

				public Object component(Form<?> form)
				{
					if (form.multiPart)
					{
						anyEmbeddedMultipart[0] = true;
						return STOP_TRAVERSAL;
					}
					else
					{
						return CONTINUE_TRAVERSAL;
					}
				}

			});
			return anyEmbeddedMultipart[0];
		}
	}
	protected void onBeforeRender()
	{
		super.onBeforeRender();

		// auto toggle form's multipart property
		Form<?> form = findParent(Form.class);
		if (form == null)
		{
			// woops
			throw new IllegalStateException("Component " + getClass().getName() + " must have a " +
				Form.class.getName() + " component above in the hierarchy");
		}
		form.setMultiPart(true);
	}
	public MultipartServletWebRequest(HttpServletRequest request, Bytes maxSize,
		FileItemFactory factory) throws FileUploadException
	{
		super(request);

		if (maxSize == null)
		{
			throw new IllegalArgumentException("argument maxSize must be not null");
		}

		// Check that request is multipart
		final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart)
		{
			throw new IllegalStateException("ServletRequest does not contain multipart content");
		}


		// Configure the factory here, if desired.
		ServletFileUpload upload = new ServletFileUpload(factory);

		// The encoding that will be used to decode the string parameters
		// It should NOT be null at this point, but it may be
		// if the older Servlet API 2.2 is used
		String encoding = request.getCharacterEncoding();

		// set encoding specifically when we found it
		if (encoding != null)
		{
			upload.setHeaderEncoding(encoding);
		}

		upload.setSizeMax(maxSize.bytes());

		final List<FileItem> items;

		if (wantUploadProgressUpdates())
		{
			ServletRequestContext ctx = new ServletRequestContext(request)
			{
				@Override
				public InputStream getInputStream() throws IOException
				{
					return new CountingInputStream(super.getInputStream());
				}
			};
			totalBytes = request.getContentLength();

			onUploadStarted(totalBytes);
			items = upload.parseRequest(ctx);
			onUploadCompleted();

		}
		else
		{
			items = upload.parseRequest(request);
		}

		// Loop through items
		for (Iterator<FileItem> i = items.iterator(); i.hasNext();)
		{
			// Get next item
			final FileItem item = i.next();

			// If item is a form field
			if (item.isFormField())
			{
				// Set parameter value
				final String value;
				if (encoding != null)
				{
					try
					{
						value = item.getString(encoding);
					}
					catch (UnsupportedEncodingException e)
					{
						throw new WicketRuntimeException(e);
					}
				}
				else
				{
					value = item.getString();
				}

				addParameter(item.getFieldName(), value);
			}
			else
			{
				// Add to file list
				files.put(item.getFieldName(), item);
			}
		}
	}
