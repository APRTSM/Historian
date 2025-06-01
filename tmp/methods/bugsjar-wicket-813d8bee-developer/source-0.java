	protected void onComponentTag(final ComponentTag tag)
	{
		// Default handling for component tag
		super.onComponentTag(tag);

		// must be attached to <input type="checkbox" .../> tag
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "checkbox");

		CheckGroup<?> group = getGroup();

		final String uuid = getValue();

		// assign name and value
		tag.put("name", group.getInputName());
		tag.put("value", uuid);

		// check if the model collection of the group contains the model object.
		// if it does check the check box.
		Collection<?> collection = (Collection<?>)group.getDefaultModelObject();

		// check for npe in group's model object
		if (collection == null)
		{
			throw new WicketRuntimeException("CheckGroup [" + group.getPath() +
				"] contains a null model object, must be an object of type java.util.Collection");
		}

		if (group.hasRawInput())
		{
			final String raw = group.getRawInput();
			if (!Strings.isEmpty(raw))
			{
				final String[] values = raw.split(FormComponent.VALUE_SEPARATOR);
				for (String value : values)
				{
					if (uuid.equals(value))
					{
						tag.put("checked", "checked");
					}
				}
			}
		}
		else if (collection.contains(getDefaultModelObject()))
		{
			tag.put("checked", "checked");
		}

		if (group.wantOnSelectionChangedNotifications())
		{
			// url that points to this components IOnChangeListener method
			CharSequence url = group.urlFor(IOnChangeListener.INTERFACE, new PageParameters());

			Form<?> form = group.findParent(Form.class);
			if (form != null)
			{
				tag.put("onclick", form.getJsForInterfaceUrl(url));
			}
			else
			{
				// NOTE: do not encode the url as that would give invalid JavaScript
				tag.put("onclick", "window.location.href='" + url +
					(url.toString().indexOf('?') > -1 ? "&" : "?") + group.getInputName() +
					"=' + this.value;");
			}
		}

		if (!isActionAuthorized(ENABLE) || !isEnabledInHierarchy() || !group.isEnabledInHierarchy())
		{
			tag.put(ATTR_DISABLED, ATTR_DISABLED);
		}

		// put group id into the class so we can easily identify all radios belonging to the group
		final String marker = "wicket-" + getGroup().getMarkupId();
		String clazz = tag.getAttribute("class");
		if (Strings.isEmpty(clazz))
		{
			clazz = marker;
		}
		else
		{
			clazz = clazz + " " + marker;
		}
		tag.put("class", clazz);

	}
