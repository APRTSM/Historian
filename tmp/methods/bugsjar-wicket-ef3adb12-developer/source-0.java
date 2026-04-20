	private boolean isTabVisible(final int tabIndex)
	{
		if (tabsVisibilityCache == null)
		{
			tabsVisibilityCache = new Boolean[tabs.size()];
		}

		if (tabsVisibilityCache.length < tabIndex + 1)
		{
			Boolean[] resized = new Boolean[tabIndex + 1];
			System.arraycopy(tabsVisibilityCache, 0, resized, 0, tabsVisibilityCache.length);
			tabsVisibilityCache = resized;
		}

		if (tabsVisibilityCache.length > 0)
		{
			Boolean visible = tabsVisibilityCache[tabIndex];
			if (visible == null)
			{
				visible = tabs.get(tabIndex).isVisible();
				tabsVisibilityCache[tabIndex] = visible;
			}
			return visible;
		}
		else
		{
			return false;
		}
	}
	private void setCurrentTab(int index)
	{
		if (this.currentTab == index)
		{
			// already current
			return;
		}
		this.currentTab = index;

		final Component component;

		if (currentTab == -1 || (tabs.size() == 0) || !isTabVisible(currentTab))
		{
			// no tabs or the current tab is not visible
			component = newPanel();
		}
		else
		{
			// show panel from selected tab
			T tab = tabs.get(currentTab);
			component = tab.getPanel(TAB_PANEL_ID);
			if (component == null)
			{
				throw new WicketRuntimeException("ITab.getPanel() returned null. TabbedPanel [" +
					getPath() + "] ITab index [" + currentTab + "]");
			}
		}

		if (!component.getId().equals(TAB_PANEL_ID))
		{
			throw new WicketRuntimeException(
				"ITab.getPanel() returned a panel with invalid id [" +
					component.getId() +
					"]. You must always return a panel with id equal to the provided panelId parameter. TabbedPanel [" +
					getPath() + "] ITab index [" + currentTab + "]");
		}

		addOrReplace(component);
	}
	protected void onBeforeRender()
	{
		int index = getSelectedTab();

		if ((index == -1) || (isTabVisible(index) == false))
		{
			// find first visible tab
			index = -1;
			for (int i = 0; i < tabs.size(); i++)
			{
				if (isTabVisible(i))
				{
					index = i;
					break;
				}
			}

			if (index != -1)
			{
				/*
				 * found a visible tab, so select it
				 */
				setSelectedTab(index);
			}
		}

		setCurrentTab(index);

		super.onBeforeRender();
	}
	protected void onDetach()
	{
		tabsVisibilityCache = null;
		super.onDetach();
	}
	protected LoopItem newTabContainer(final int tabIndex)
	{
		return new LoopItem(tabIndex)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(final ComponentTag tag)
			{
				super.onComponentTag(tag);
				String cssClass = tag.getAttribute("class");
				if (cssClass == null)
				{
					cssClass = " ";
				}
				cssClass += " tab" + getIndex();

				if (getIndex() == getSelectedTab())
				{
					cssClass += ' ' + getSelectedTabCssClass();
				}
				if (getIndex() == getTabs().size() - 1)
				{
					cssClass += ' ' + getLastTabCssClass();
				}
				tag.put("class", cssClass.trim());
			}

			@Override
			public boolean isVisible()
			{
				return getTabs().get(tabIndex).isVisible();
			}
		};
	}
