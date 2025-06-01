	public MarkupContainer add(final Component... childs)
	{
		for (Component child : childs)
		{
			if (child == null)
			{
				throw new IllegalArgumentException("argument child may not be null");
			}

			checkHierarchyChange(child);

			if (log.isDebugEnabled())
			{
				log.debug("Add " + child.getId() + " to " + this);
			}

			// Add to map
			addedComponent(child);
			if (put(child) != null)
			{
				throw new IllegalArgumentException(exceptionMessage("A child with id '" +
					child.getId() + "' already exists"));
			}

			// Check if the markup is available after the child has been added to the parent
			try
			{
				// If not yet triggered, than do now (e.g. Pages)
				if (getMarkup() != null)
				{
					internalOnMarkupAttached();
				}

				if (child.getMarkup() != null)
				{
					child.internalOnMarkupAttached();

					// Tell all children of "component" as well
					if (child instanceof MarkupContainer)
					{
						MarkupContainer container = (MarkupContainer)child;
						container.visitChildren(new IVisitor<Component, Void>()
						{
							public void component(final Component component,
								final IVisit<Void> visit)
							{
								if (component.internalOnMarkupAttached())
								{
									visit.dontGoDeeper();
								}
							}
						});
					}
				}
			}
			catch (WicketRuntimeException exception)
			{
				// ignore
			}
		}
		return this;
	}
	private final void addedComponent(final Component child)
	{
		// Check for degenerate case
		if (child == this)
		{
			throw new IllegalArgumentException("Component can't be added to itself");
		}

		MarkupContainer parent = child.getParent();
		if (parent != null)
		{
			parent.remove(child);
		}

		// Set child's parent
		child.setParent(this);

		final IDebugSettings debugSettings = Application.get().getDebugSettings();
		if (debugSettings.isLinePreciseReportingOnAddComponentEnabled())
		{
			child.setMetaData(ADDED_AT_KEY, ComponentStrings.toString(child, new MarkupException(
				"added")));
		}

		final Page page = findPage();

		if (page != null)
		{
			child.initialize();
		}

		if (page != null)
		{
			// Tell the page a component has been added
			page.componentAdded(child);
		}

		// if the PREPARED_FOR_RENDER flag is set, we have already called
		// beforeRender on this component's children. So we need to initialize the newly added one
		if (isPreparedForRender())
		{
			child.beforeRender();
		}
	}
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		// enclosure's parent container
		MarkupContainer container = getEnclosureParent();

		Component controller = container.get(childId.toString());
		checkChildComponent(controller);

		// set the enclosure visibility
		boolean visible = controller.determineVisibility();

		// We want to know which components are rendered inside the enclosure
		final IComponentOnAfterRenderListener listener = new EnclosureListener(this);

		try
		{
			// register the listener
			getApplication().addComponentOnAfterRenderListener(listener);

			if (visible)
			{
				super.onComponentTagBody(markupStream, openTag);
			}
			else
			{
				RequestCycle cycle = getRequestCycle();
				Response response = cycle.getResponse();
				try
				{
					cycle.setResponse(NullResponse.getInstance());

					super.onComponentTagBody(markupStream, openTag);
				}
				finally
				{
					cycle.setResponse(response);
				}
			}
		}
		finally
		{
			// make sure we remove the listener
			getApplication().removeComponentOnAfterRenderListener(listener);
		}
	}
	private MarkupContainer getEnclosureParent()
	{
		MarkupContainer parent = getParent();
		while (parent.isAuto())
		{
			parent = parent.getParent();
		}

		if (parent == null)
		{
			throw new WicketRuntimeException(
				"Unable to find parent component which is not a transparent resolver");
		}
		return parent;
	}
	public static Component resolve(final MarkupContainer container,
		final MarkupStream markupStream, final ComponentTag tag)
	{
		// try to resolve using component hierarchy

		Component cursor = container;
		while (cursor != null)
		{
			if (cursor instanceof IComponentResolver)
			{
				IComponentResolver resolver = (IComponentResolver)cursor;
				Component component = resolver.resolve(container, markupStream, tag);
				if (component != null)
				{
					return component;
				}
			}
			cursor = cursor.getParent();
		}

		// fallback to application-level resolvers

		for (final IComponentResolver resolver : Application.get()
			.getPageSettings()
			.getComponentResolvers())
		{
			Component component = resolver.resolve(container, markupStream, tag);
			if (component != null)
			{
				return component;
			}
		}

		return null;
	}
	private static final void writeStream(final Response response, ByteArrayOutputStream stream)
	{
		final boolean copied[] = { false };
		try
		{
			// try to avoid copying the array
			stream.writeTo(new OutputStream()
			{
				@Override
				public void write(int b) throws IOException
				{

				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException
				{
					if (off == 0 && len == b.length)
					{
						response.write(b);
						copied[0] = true;
					}
				}
			});
		}
		catch (IOException e1)
		{
			throw new WicketRuntimeException(e1);
		}
		if (copied[0] == false)
		{
			response.write(stream.toByteArray());
		}
	}
