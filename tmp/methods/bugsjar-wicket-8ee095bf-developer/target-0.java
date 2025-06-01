	public void processEvents(RequestCycle requestCycle)
	{
		Page page = getPage();
		if (page == null)
		{
			page = Session.get().getPage(getPageMapName(), componentPath, -1);
			if (page != null && page.getClass() == getPageClass())
			{
				setPage(page);
			}
			else
			{
				page = getPage(requestCycle);
			}
		}

		if (page == null)
		{
			throw new PageExpiredException(
				"Request cannot be processed. The target page does not exist anymore.");
		}

		final String pageRelativeComponentPath = Strings.afterFirstPathComponent(componentPath,
			Component.PATH_SEPARATOR);
		Component component = page.get(pageRelativeComponentPath);
		if (component == null)
		{
			// this is quite a hack to get components in repeater work.
			// But it still can fail if the repeater is a paging one or on every render
			// it will generate new index for the items...
			page.prepareForRender(false);
			component = page.get(pageRelativeComponentPath);
			if (component == null)
			{
				throw new WicketRuntimeException(
					"unable to find component with path " +
						pageRelativeComponentPath +
						" on stateless page " +
						page +
						" it could be that the component is inside a repeater make your component return false in getStatelessHint()");
			}
		}
		RequestListenerInterface listenerInterface = RequestListenerInterface.forName(interfaceName);
		if (listenerInterface == null)
		{
			throw new WicketRuntimeException("unable to find listener interface " + interfaceName);
		}
		listenerInterface.invoke(page, component);
	}
	public BookmarkableListenerInterfaceRequestTarget(String pageMapName,
		Class<? extends Page> pageClass, PageParameters pageParameters, Component component,
		RequestListenerInterface listenerInterface)
	{
		this(pageMapName, pageClass, pageParameters, component.getPath(),
			listenerInterface.getName(), component.getPage().getCurrentVersionNumber());

		int version = component.getPage().getCurrentVersionNumber();
		setPage(component.getPage());

		// add the wicket:interface param to the params.
		// pagemap:(pageid:componenta:componentb:...):version:interface:behavior:urlDepth
		AppendingStringBuffer param = new AppendingStringBuffer(4 + componentPath.length() +
			interfaceName.length());
		if (pageMapName != null)
		{
			param.append(pageMapName);
		}
		param.append(Component.PATH_SEPARATOR);
		param.append(getComponentPath());
		param.append(Component.PATH_SEPARATOR);
		if (version != 0)
		{
			param.append(version);
		}
		// Interface
		param.append(Component.PATH_SEPARATOR);
		param.append(getInterfaceName());

		// Behavior (none)
		param.append(Component.PATH_SEPARATOR);

		// URL depth (not required)
		param.append(Component.PATH_SEPARATOR);

		pageParameters.put(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME, param.toString());
	}
	public void respond(RequestCycle requestCycle)
	{
		Page page = getPage(requestCycle);
		// if the listener call wanted to redirect
		// then do that if the page is not stateless.
		if (requestCycle.isRedirect() && !page.isPageStateless())
		{
			requestCycle.redirectTo(page);
		}
		else
		{
			// else render the page directly
			page.renderPage();
		}
	}
	public String getComponentPath()
	{
		return componentPath;
	}
	public String getInterfaceName()
	{
		return interfaceName;
	}
	public BookmarkableListenerInterfaceRequestTarget(String pageMapName,
		Class<? extends Page> pageClass, PageParameters pageParameters, String componentPath,
		String interfaceName, int versionNumber)
	{
		super(pageMapName, pageClass, pageParameters);
		this.componentPath = componentPath;
		this.interfaceName = interfaceName;
	}
