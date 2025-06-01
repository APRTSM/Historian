	public void setRequestToComponent(final Component component)
	{
		final IPageMap pageMap = component.getPage().getPageMap();
		final String pageMapName = pageMap.isDefault() ? "" : pageMap.getName();
		if (component instanceof BookmarkablePageLink)
		{
			final Class<? extends Page> clazz = ((BookmarkablePageLink<?>)component).getPageClass();
			parameters.put(WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME, pageMapName +
				':' + clazz.getName());
		}
		else
		{
			int version = component.getPage().getCurrentVersionNumber();
			Class<?> clazz = null;
			if (component instanceof IRedirectListener)
			{
				clazz = IRedirectListener.class;
			}
			else if (component instanceof IResourceListener)
			{
				clazz = IResourceListener.class;
			}
			else if (component instanceof IFormSubmitListener)
			{
				clazz = IFormSubmitListener.class;
			}
			else if (component instanceof ILinkListener)
			{
				clazz = ILinkListener.class;
			}
			else if (component instanceof IOnChangeListener)
			{
				clazz = IOnChangeListener.class;
			}
			else
			{
				throw new IllegalArgumentException(
					"The component class doesn't seem to implement any of the known *Listener interfaces: " +
						component.getClass());
			}

			// manually create the url using default strategy and format
			parameters.put(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME, pageMapName + ':' +
				component.getPath() + ':' + (version == 0 ? "" : "" + version) + ':' +
				Classes.simpleName(clazz) + "::");

			// see if we can replace our manual listener url with a properly generated one...

			try
			{
				RequestListenerInterface rli = (RequestListenerInterface)clazz.getField("INTERFACE")
					.get(clazz);

				String auto = component.getRequestCycle().urlFor(component, rli).toString();
				int idx = auto.indexOf(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME);
				if (idx >= 0)
				{
					auto = auto.substring(idx +
						WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME.length() + 1);
				}
				else
				{
					// additional check for crypted strategy
					idx = auto.indexOf("x=6*");
					if (idx >= 0)
					{
						auto = auto.substring(idx + 4);
					}
				}

				idx = auto.indexOf("&");
				if (idx >= 0)
				{
					auto = auto.substring(0, idx);
				}

				parameters.put(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME, auto);

			}
			catch (Exception e)
			{
				// noop
			}


			if (component.isStateless() && component.getPage().isBookmarkable())
			{
				parameters.put(WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME,
					pageMapName + ':' + component.getPage().getClass().getName());
			}
		}
	}
