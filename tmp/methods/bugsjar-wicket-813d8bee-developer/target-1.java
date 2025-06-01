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

	}
	protected void onComponentTag(final ComponentTag tag)
	{
		// Default handling for component tag
		super.onComponentTag(tag);

		// must be attached to <input type="radio" .../> tag
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "radio");

		final String value = getValue();

		RadioGroup<?> group = getGroup();

		// assign name and value
		tag.put("name", group.getInputName());
		tag.put("value", value);

		// compare the model objects of the group and self, if the same add the
		// checked attribute, first check if there was a raw input on the group.
		if (group.hasRawInput())
		{
			String rawInput = group.getRawInput();
			if (rawInput != null && rawInput.equals(value))
			{
				tag.put("checked", "checked");
			}
		}
		else if (group.getModelComparator().compare(group, getDefaultModelObject()))
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

		if (!isEnabledInHierarchy())
		{
			tag.put(ATTR_DISABLED, ATTR_DISABLED);
		}

	}
