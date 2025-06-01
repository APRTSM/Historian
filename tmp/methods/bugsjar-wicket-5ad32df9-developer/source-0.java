	public final MarkupContainer add(final Component... childs)
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

			// One of the key pre-requisites to successfully load markup, is the availability of the
			// file extension. Which in turn is part of MarkupType which by default requires the
			// Page.
			if (getMarkupType() != null)
			{
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
			child.setMetaData(ADDED_AT_KEY,
				ComponentStrings.toString(child, new MarkupException("added")));
		}

		final Page page = findPage();
		if (page != null)
		{
			// tell the page a component has been added first, to allow it to initialize
			page.componentAdded(child);

			// initialize the component
			if (page.isInitialized())
			{
				child.internalInitialize();
			}
		}

		// if the PREPARED_FOR_RENDER flag is set, we have already called
		// beforeRender on this component's children. So we need to initialize the newly added one
		if (isPreparedForRender())
		{
			child.beforeRender();
		}
	}
