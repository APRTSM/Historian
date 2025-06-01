	private final void checkRendering(final MarkupContainer renderedContainer)
	{
		// If the application wants component uses checked and
		// the response is not a redirect
		final IDebugSettings debugSettings = Application.get().getDebugSettings();
		if (debugSettings.getComponentUseCheck() && !getResponse().isRedirect())
		{
			final List<Component> unrenderedComponents = new ArrayList<Component>();
			final StringBuffer buffer = new StringBuffer();
			renderedContainer.visitChildren(new IVisitor<Component>()
			{
				public Object component(final Component component)
				{
					// If component never rendered
					if (renderedComponents == null || !renderedComponents.contains(component))
					{
						// If auto component ...
						if (!component.isAuto() && component.isVisibleInHierarchy())
						{
							// Increase number of unrendered components
							unrenderedComponents.add(component);

							// Add to explanatory string to buffer
							buffer.append(Integer.toString(unrenderedComponents.size()) + ". " +
								component + "\n");
							String metadata = component.getMetaData(Component.CONSTRUCTED_AT_KEY);
							if (metadata != null)
							{
								buffer.append(metadata);
							}
							metadata = component.getMetaData(Component.ADDED_AT_KEY);
							if (metadata != null)
							{
								buffer.append(metadata);
							}
						}
						else
						{
							// if the component is not visible in hierarchy we
							// should not visit its children since they are also
							// not visible
							return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
						}
					}
					return CONTINUE_TRAVERSAL;
				}
			});

			// Throw exception if any errors were found
			if (unrenderedComponents.size() > 0)
			{
				// Get rid of set
				renderedComponents = null;

				Iterator<Component> iterator = unrenderedComponents.iterator();

				while (iterator.hasNext())
				{
					Component component = iterator.next();
					// Now first test if the component has a sibling that is a transparent resolver.

					Iterator<? extends Component> iterator2 = component.getParent().iterator();
					while (iterator2.hasNext())
					{
						Component sibling = iterator2.next();
						if (!sibling.isVisible())
						{
							boolean isTransparentMarkupContainer = sibling instanceof MarkupContainer &&
								((MarkupContainer)sibling).isTransparentResolver();
							boolean isComponentResolver = sibling instanceof IComponentResolver;
							if (isTransparentMarkupContainer || isComponentResolver)
							{
								// we found a transparent container that isn't visible
								// then ignore this component and only do a debug statement here.
								log.debug(
									"Component {} wasn't rendered but most likely it has a transparent parent: {}",
									component, sibling);
								iterator.remove();
								break;
							}
						}
					}

					// Check if this component is a child of a border whose body is invisible and if
					// so ignore it
					Border border = component.findParent(Border.class);
					if (border != null && !border.getBodyContainer().isVisibleInHierarchy())
					{

						// Suppose:
						//						  
						// <div wicket:id="border"><div wicket:id="label"></div> suppose
						// border->label and border's body is hidden.
						//						  
						// The label is added to border not to its hidden body so as far as wicket
						// is concerned label is visible in hierarchy, but when rendering label wont
						// be rendered because in the markup it is inside the border's hidden body.
						// Thus component use check will fail even though it shouldnt - make sure it
						// doesnt.
						//						 

						// TODO it would be more accurate to determine that this component is inside
						// the border parent's markup not the border's itself
						iterator.remove();
					}
				}
				// if still > 0
				if (unrenderedComponents.size() > 0)
				{
					// Throw exception
					throw new WicketRuntimeException(
						"The component(s) below failed to render. A common problem is that you have added a component in code but forgot to reference it in the markup (thus the component will never be rendered).\n\n" +
							buffer.toString());
				}
			}
		}

		// Get rid of set
		renderedComponents = null;
	}
	public boolean isBookmarkable()
	{
		Boolean bookmarkable = pageClassToBookmarkableCache.get(getClass().getName());
		if (bookmarkable == null)
		{
			try
			{

				if (getClass().getConstructor(new Class[] {}) != null)
				{
					bookmarkable = Boolean.TRUE;
				}

			}
			catch (Exception ignore)
			{
				try
				{
					if (getClass().getConstructor(new Class[] { PageParameters.class }) != null)
					{
						bookmarkable = Boolean.TRUE;
					}
				}
				catch (Exception ignore2)
				{
				}
			}
			if (bookmarkable == null)
			{
				bookmarkable = Boolean.FALSE;
			}
			pageClassToBookmarkableCache.put(getClass().getName(), bookmarkable);
		}
		return bookmarkable.booleanValue();

	}
