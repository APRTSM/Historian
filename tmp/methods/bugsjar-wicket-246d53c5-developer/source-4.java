	public final void error(final Serializable message)
	{
		getPage().getFeedbackMessages().error(this, message);
	}
	public final void info(final String message)
	{
		getPage().getFeedbackMessages().info(this, message);
	}
	public final boolean hasFeedbackMessage()
	{
		return getPage().getFeedbackMessages().hasMessageFor(this);
	}
	public final boolean hasErrorMessage()
	{
		return getPage().getFeedbackMessages().hasErrorMessageFor(this);
	}
	public final void debug(final String message)
	{
		getPage().getFeedbackMessages().debug(this, message);
	}
	public final void warn(final String message)
	{
		getPage().getFeedbackMessages().warn(this, message);
	}
	public final void fatal(final String message)
	{
		getPage().getFeedbackMessages().fatal(this, message);
	}
	public final FeedbackMessage getFeedbackMessage()
	{
		return getPage().getFeedbackMessages().messageForComponent(this);
	}
	public final void componentRendered(final Component component)
	{
		// Inform the page that this component rendered
		if (Application.get().getDebugSettings().getComponentUseCheck())
		{
			if (renderedComponents == null)
			{
				renderedComponents = new HashSet();
			}
			if (renderedComponents.add(component) == false)
			{
				throw new MarkupException(
						"The component "
								+ component
								+ " has the same wicket:id as another component already added at the same level");
			}
			if (log.isDebugEnabled())
			{
				log.debug("Rendered " + component);
			}
		}
	}
	protected final void internalOnModelChanged()
	{
		visitChildren(new Component.IVisitor()
		{
			public Object component(final Component component)
			{
				// If form component is using form model
				if (component.sameRootModel(Page.this))
				{
					component.modelChanged();
				}
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});
	}
	private final void endVersion()
	{
		// Any changes to the page after this point will be tracked by the
		// page's version manager. Since trackChanges is never set to false,
		// this effectively means that change tracking begins after the
		// first request to a page completes.
		setFlag(FLAG_TRACK_CHANGES, true);
		
		// If a new version was created
		if (getFlag(FLAG_NEW_VERSION))
		{
			// Reset boolean for next request
			setFlag(FLAG_NEW_VERSION, false);

			// We're done with this version
			if (versionManager != null)
			{
				versionManager.endVersion(getRequest().mergeVersion());
			}

			// Evict any page version(s) as need be
			getApplication().getSessionSettings().getPageMapEvictionStrategy().evict(getPageMap());
		}
	}
	public final Page rollbackPage(int numberOfVersions)
	{
		Page page =  versionManager == null? this : versionManager.rollbackPage(numberOfVersions);
		getSession().touch(page);
		return page;
	}
	public final void startComponentRender(Component component)
	{
		renderedComponents = null;
	}
	final void setFormComponentValuesFromCookies()
	{
		// Visit all Forms contained in the page
		visitChildren(Form.class, new Component.IVisitor()
		{
			// For each FormComponent found on the Page (not Form)
			public Object component(final Component component)
			{
				((Form)component).loadPersistentFormComponentValues();
				return CONTINUE_TRAVERSAL;
			}
		});
	}
	final void componentRemoved(final Component component)
	{
		checkHierarchyChange(component);

		dirty();
		if (mayTrackChangesFor(component, component.getParent()))
		{
			versionManager.componentRemoved(component);
		}
	}
	public String toString()
	{
		if(versionManager != null)
		{
			return "[Page class = " + getClass().getName() + ", id = " + getId() + 
				", version = " + versionManager.getCurrentVersionNumber()  + ", ajax = " + 
				versionManager.getAjaxVersionNumber() + "]";	
		}
		else
		{
			return "[Page class = " + getClass().getName() + ", id = " + getId() + ", version = " + 0 + "]";
		}
	}
	protected void onDetach()
	{
		if (log.isDebugEnabled())
		{
			log.debug("ending request for page " + this + ", request " + getRequest());
		}

		endVersion();
		
		super.onDetach();
	}
	public final void setNumericId(final int id)
	{
		this.numericId = (short)id;
	}
	final void componentModelChanging(final Component component)
	{
		checkHierarchyChange(component);

		dirty();
		if (mayTrackChangesFor(component, null))
		{
			versionManager.componentModelChanging(component);
		}
	}
	void setPageStateless(Boolean stateless)
	{
		this.stateless = stateless;
	}
	public final void ignoreVersionMerge()
	{
		if (getRequest().mergeVersion())
		{
			mayTrackChangesFor(this, null);
			if (versionManager != null)
			{
				versionManager.ignoreVersionMerge();
			}
		}
	}
	public Page getVersion(final int versionNumber)
	{
		// If we're still the original Page and that's what's desired
		if (versionManager == null)
		{
			if (versionNumber == 0 || versionNumber == LATEST_VERSION)
			{
				return this;
			}
			else
			{
				log.info("No version manager available to retrieve requested versionNumber "
						+ versionNumber);
				return null;
			}
		}
		else
		{
			// Save original change tracking state
			final boolean originalTrackChanges = getFlag(FLAG_TRACK_CHANGES);

			try
			{
				// While the version manager is potentially playing around with
				// the Page, it may change the page in order to undo changes and
				// we don't want change tracking going on while its doing this.
				setFlag(FLAG_TRACK_CHANGES, false);

				// Get page of desired version
				final Page page;
				if (versionNumber != LATEST_VERSION)
				{
					page = versionManager.getVersion(versionNumber);
				}
				else
				{
					page = versionManager.getVersion(getCurrentVersionNumber());
				}

				// If we went all the way back to the original page
				if (page != null && page.getCurrentVersionNumber() == 0 && page.getAjaxVersionNumber() == 0)
				{
					// remove version info
					page.versionManager = null;
				}

				return page;
			}
			finally
			{
				// Restore change tracking state
				setFlag(FLAG_TRACK_CHANGES, originalTrackChanges);
			}
		}
	}
	final void componentAdded(final Component component)
	{
		checkHierarchyChange(component);

		dirty();
		if (mayTrackChangesFor(component, component.getParent()))
		{
			versionManager.componentAdded(component);
		}
	}
	protected void onRender(final MarkupStream markupStream)
	{
		// Set page's associated markup stream
		final MarkupStream associatedMarkupStream = getAssociatedMarkupStream(true);
		setMarkupStream(associatedMarkupStream);

		// Configure response object with locale and content type
		configureResponse();

		// Render all the page's markup
		setFlag(FLAG_IS_RENDERING, true);
		try
		{
			renderAll(associatedMarkupStream);
		}
		finally
		{
			setFlag(FLAG_IS_RENDERING, false);
		}
	}
	public final FeedbackMessages getFeedbackMessages()
	{
		if (feedbackMessages == null)
		{
			feedbackMessages = new FeedbackMessages();
		}
		return feedbackMessages;
	}
	public void detachModels()
	{
//		// visit all this page's children to detach the models
//		visitChildren(new IVisitor()
//		{
//			public Object component(Component component)
//			{
//				try
//				{
//					// detach any models of the component
//					component.detachModels();
//				}
//				catch (Exception e) // catch anything; we MUST detach all models
//				{
//					log.error("detaching models of component " + component + " failed:", e);
//				}
//				return IVisitor.CONTINUE_TRAVERSAL;
//			}
//		});

		super.detachModels();
	}
	protected final IPageVersionManager newVersionManager()
	{
		return null;
	}
	public final void renderPage()
	{
		// first try to check if the page can be rendered:
		if (!isActionAuthorized(RENDER))
		{
			if (log.isDebugEnabled())
			{
				log.debug("Page not allowed to render: " + this);
			}
			throw new UnauthorizedActionException(this, Component.RENDER);
		}

		// Make sure it is really empty
		renderedComponents = null;

		// Reset it to stateless so that it can be tested again
		this.stateless = null;

		// Set form component values from cookies
		setFormComponentValuesFromCookies();

		// First, give priority to IFeedback instances, as they have to
		// collect their messages before components like ListViews
		// remove any child components
		visitChildren(IFeedback.class, new IVisitor()
		{
			public Object component(Component component)
			{
				((IFeedback)component).updateFeedback();
				component.attach();
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});

		if (this instanceof IFeedback)
		{
			((IFeedback)this).updateFeedback();
		}

		// Now, do the initialization for the other components
		attach();

		// Visit all this page's children to reset markup streams and check
		// rendering authorization, as appropriate. We set any result; positive
		// or negative as a temporary boolean in the components, and when a
		// authorization exception is thrown it will block the rendering of this
		// page

		// first the page itself
		setRenderAllowed(isActionAuthorized(RENDER));
		// children of the page
		visitChildren(new IVisitor()
		{
			public Object component(final Component component)
			{
				// Find out if this component can be rendered
				final boolean renderAllowed = component.isActionAuthorized(RENDER);

				// Authorize rendering
				component.setRenderAllowed(renderAllowed);
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});

		// Handle request by rendering page
		render(null);

		// Check rendering if it happened fully
		checkRendering(this);

		if (!isPageStateless())
		{
			// trigger creation of the actual session in case it was deferred
			Session.get().getSessionStore().getSessionId(RequestCycle.get().getRequest(), true);
			// Add/touch the response page in the session (its pagemap).
			getSession().touch(this);
		}
	}
	public final int getAjaxVersionNumber()
	{
		return versionManager == null ? 0 : versionManager.getAjaxVersionNumber();
	}
	final void componentStateChanging(final Component component, Change change)
	{
		checkHierarchyChange(component);

		dirty();
		if (mayTrackChangesFor(component, null))
		{
			versionManager.componentStateChanging(change);
		}
	}
	final void setPageMap(final IPageMap pageMap)
	{
		// Save transient reference to pagemap
		this.pageMap = pageMap;

		// Save name for restoring transient
		this.pageMapName = pageMap.getName();
	}
	protected void configureResponse()
	{
		// Get the response and application
		final RequestCycle cycle = getRequestCycle();
		final Application application = cycle.getApplication();
		final Response response = cycle.getResponse();

		// Determine encoding
		final String encoding = application.getRequestCycleSettings().getResponseRequestEncoding();

		// Set content type based on markup type for page
		response.setContentType("text/" + getMarkupType() + "; charset=" + encoding);

		// Write out an xml declaration if the markup stream and settings allow
		final MarkupStream markupStream = findMarkupStream();
		if ((markupStream != null) && (markupStream.getXmlDeclaration() != null)
				&& (application.getMarkupSettings().getStripXmlDeclarationFromOutput() == false))
		{
			response.write("<?xml version='1.0' encoding='");
			response.write(encoding);
			response.write("'?>");
		}

		// Set response locale from session locale
		response.setLocale(getSession().getLocale());
	}
	protected final void moveToPageMap(IPageMap map)
	{
		// TODO post 1.2 shouldn't we remove this page from the pagemap/session
		// if it would be in there?
		// This should be done if the page was not cloned first, but shouldn't
		// be done if it was cloned..
		setPageMap(map);
		numericId = (short)map.nextId();
	}
	private void detach()
	{
		// clean up target stack; calling detach has effects like
		// NOTE: don't remove the targets as testing code might need them
		// furthermore, the targets will be cg-ed with this cycle too
		for (Iterator iter = requestTargets.iterator(); iter.hasNext();)
		{
			IRequestTarget target = (IRequestTarget)iter.next();
			if (target != null)
			{
				try
				{
					target.detach(this);
				}
				catch (RuntimeException e)
				{
					log.error("there was an error cleaning up target " + target + ".", e);
				}
			}
		}

		// remove any rendered feedback messages from the session
		try
		{
			session.cleanupFeedbackMessages();
		}
		catch (RuntimeException re)
		{
			log.error("there was an error cleaning up the feedback messages", re);
		}

		if (updateSession)
		{
			// At the end of our response, we need to set any session
			// attributes that might be required to update the cluster
			try
			{
				session.update();
			}
			catch (RuntimeException re)
			{
				log.error("there was an error updating the session " + session + ".", re);
			}
		}

		try
		{
			IRequestLogger requestLogger = getApplication().getRequestLogger();
			if (requestLogger != null)
			{
				requestLogger.requestTime((System.currentTimeMillis() - startTime));
			}
		}
		catch (RuntimeException re)
		{
			log.error("there was an error in the RequestLogger ending.", re);
		}

		// clear the used pagemap for this thread,
		try
		{
			session.requestDetached();
		}
		catch (RuntimeException re)
		{
			log.error("there was an error detaching the request from the session " + session + ".",
					re);
		}

		if (getResponse() instanceof BufferedWebResponse)
		{
			try
			{
				((BufferedWebResponse)getResponse()).filter();
			}
			catch (RuntimeException re)
			{
				log.error("there was an error filtering the response.", re);
			}
		}


		try
		{
			onEndRequest();
		}
		catch (RuntimeException e)
		{
			log.error("Exception occurred during onEndRequest", e);
		}

		// Release thread local resources
		try
		{
			threadDetach();
		}
		catch (RuntimeException re)
		{
			log.error("Exception occurred during threadDetach", re);
		}
	}
	public final void touch(Page page)
	{
		// store it in a list, so that the pages are really pushed
		// to the pagemap when the session does it update/detaches.
		// all the pages are then detached
		List lst = (List)touchedPages.get();
		if(lst == null)
		{
			lst = new ArrayList();
			touchedPages.set(lst);
			lst.add(page);
		}
		else if (!lst.contains(page))
		{
			lst.add(page);
		}
	}
	public final IConverter getConverter(Class/*<?>*/ type)
	{
		if (converterSupplier == null)
		{
			// Let the factory create a new converter
			converterSupplier = getApplication().getApplicationSettings()
					.getConverterLocatorFactory().newConverterLocator();
		}
		return converterSupplier.getConverter(type);
	}
	protected void update()
	{
		List lst = (List)touchedPages.get();
		if(lst != null)
		{
			for (int i = 0; i < lst.size(); i++)
			{
				Page page = (Page)lst.get(i);
				page.getPageMap().put(page);
			}
			touchedPages.set(null);
		}
		
		// If state is dirty
		if (dirty)
		{
			if (log.isDebugEnabled())
			{
				log.debug("update: Session is dirty.  Replicating.");
			}

			// State is no longer dirty
			this.dirty = false;

			// Set attribute.
			setAttribute(SESSION_ATTRIBUTE_NAME, this);
		}
		else
		{
			if (log.isDebugEnabled())
			{
				log.debug("update: Session not dirty.");
			}
		}

		// Go through all dirty entries, replicating any dirty objects
		List dirtyObjects = (List)Session.dirtyObjects.get();
		if (dirtyObjects != null)
		{
			for (final Iterator iterator = dirtyObjects.iterator(); iterator.hasNext();)
			{
				String attribute = null;
				Object object = iterator.next();
				if (object instanceof Page)
				{
					final Page page = (Page)object;
					if (page.isStateless())
					{
						// check, can it be that stateless pages where added to
						// the session?
						// and should be removed now?
						continue;
					}
					attribute = page.getPageMap().attributeForId(page.getNumericId());
					if (getAttribute(attribute) == null)
					{
						// page removed by another thread. don't add it again.
						continue;
					}
					object = page.getPageMapEntry();
				}
				else if (object instanceof IPageMap)
				{
					attribute = attributeForPageMapName(((IPageMap)object).getName());
				}

				setAttribute(attribute, object);
			}
			Session.dirtyObjects.set(null);
		}
	}
	final void cleanupFeedbackMessages()
	{
		int size = feedbackMessages.size();
		feedbackMessages.clearRendered();

		// the session is dirty when the list of feedback messages was changed
		if (size != feedbackMessages.size())
		{
			dirty();
		}
	}
	public FeedbackMessages(List messagesList)
	{
		if(messagesList == null) throw new IllegalArgumentException("messages list can't be null");
		messages = messagesList;
	}
	public final int size()
	{
		return messages.size();
	}
	public final void add(Component reporter, String message, int level) {
		add(new FeedbackMessage(reporter, message, level));
	}
	public FeedbackMessages()
	{
		messages = new ArrayList();
	}
	public final void trimToSize()
	{
		if(messages instanceof ArrayList)
		{
			((ArrayList)messages).trimToSize();
		}
	}
	public final Iterator iterator()
	{
		return messages.iterator();
	}
	public final void clearRendered()
	{
		for(int i = messages.size() - 1; i >= 0; i--)
		{
			final FeedbackMessage msg = (FeedbackMessage)messages.get(i);
			if (msg.isRendered())
			{
				messages.remove(i);
			}
		}
		trimToSize();
	}
