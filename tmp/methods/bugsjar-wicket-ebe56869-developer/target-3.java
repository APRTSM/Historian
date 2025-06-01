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
	public MarkupContainer add(final Component... childs)
	{
		for (Component child : childs)
		{
			if (child == null)
			{
				throw new IllegalArgumentException("argument child may not be null");
			}

			MarkupContainer parent = getParent();
			while (parent != null)
			{
				if (child == parent)
				{
					String msg = "You can not add a component's parent as child to the component (loop): Component: " +
						this.toString(false) + "; parent == child: " + parent.toString(false);
					if (child instanceof Border.BorderBodyContainer)
					{
						msg += ". Please consider using Border.addToBorder(new " +
							this.getClass().getSimpleName() + "(\"" + this.getId() +
							"\", ...) instead of add(...)";
					}
					throw new WicketRuntimeException(msg);
				}
				parent = parent.getParent();
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
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		// TODO this is where I wish we had something like "enum(TAG, BODY, NONE, ALL) isVisible()"
		// set the enclosure visibility
		boolean visible = childComponent.determineVisibility();

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
	public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
		if (childId.equals(tag.getId()))
		{
			return childComponent;
		}
		return getEnclosureParent().get(tag.getId());
	}
	private MarkupContainer getEnclosureParent()
	{
		MarkupContainer parent = getParent();
		while ((parent != null) && parent.isAuto())
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
	private Component getChildComponent(final MarkupStream markupStream, MarkupContainer container)
	{
		Component controller = container.get(childId.toString());
		if (controller == null)
		{
			int orgIndex = markupStream.getCurrentIndex();
			try
			{
				while (markupStream.hasMore())
				{
					markupStream.next();
					if (markupStream.skipUntil(ComponentTag.class))
					{
						ComponentTag tag = markupStream.getTag();
						if ((tag != null) && (tag.isOpen() || tag.isOpenClose()))
						{
							if (childId.equals(tag.getId()))
							{
								controller = ComponentResolvers.resolveByComponentHierarchy(
									container, markupStream, tag);
								break;
							}
						}
					}
				}
			}
			finally
			{
				markupStream.setCurrentIndex(orgIndex);
			}
		}
		return controller;
	}
	protected void onInitialize()
	{
		super.onInitialize();

		// enclosure's parent container
		MarkupContainer container = getEnclosureParent();

		// clear the cache
		childComponent = null;

		// get Child Component. If not "added", ask a resolver to find it.
		childComponent = getChildComponent(new MarkupStream(getMarkup()), container);
		checkChildComponent(childComponent);
	}
	public static Component resolve(final MarkupContainer container,
		final MarkupStream markupStream, final ComponentTag tag)
	{
		// try to resolve using component hierarchy
		Component component = resolveByComponentHierarchy(container, markupStream, tag);

		if (component == null)
		{
			// fallback to application-level resolvers
			component = resolveByApplication(container, markupStream, tag);
		}

		return component;
	}
	public static Component resolveByApplication(final MarkupContainer container,
		final MarkupStream markupStream, final ComponentTag tag)
	{
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
	public static Component resolveByComponentHierarchy(final MarkupContainer container,
		final MarkupStream markupStream, final ComponentTag tag)
	{
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

		return null;
	}
	public String toString()
	{
		return charSequenceAction.builder.toString();
	}
