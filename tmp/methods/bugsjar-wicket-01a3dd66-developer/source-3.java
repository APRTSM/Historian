	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		AppendingStringBuffer asb = new AppendingStringBuffer();
		asb.append("function attachChoiceHandlers(markupId, callbackScript) {\n");
		asb.append(" var inputNodes = wicketGet(markupId).getElementsByTagName('input');\n");
		asb.append(" for (var i = 0 ; i < inputNodes.length ; i ++) {\n");
		asb.append(" var inputNode = inputNodes[i];\n");
		asb.append(" if (!inputNode.type) continue;\n");
		asb.append(" var inputType = inputNode.type.toLowerCase();\n");
		asb.append(" if (inputType == 'checkbox' || inputType == 'radio') {\n");
		asb.append(" Wicket.Event.add(inputNode, 'click', callbackScript);\n");
		asb.append(" }\n");
		asb.append(" }\n");
		asb.append("}\n");

		response.renderJavascript(asb, "attachChoice");

		response.renderOnLoadJavascript("attachChoiceHandlers('" + getComponent().getMarkupId() +
			"', function() {" + getEventHandler() + "});");

	}
	protected void onComponentTag(final ComponentTag tag)
	{
		// Default handling for component tag
		super.onComponentTag(tag);

		// must be attached to <input type="checkbox" .../> tag
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "checkbox");

		CheckGroup<?> group = this.group;
		if (group == null)
		{
			group = findParent(CheckGroup.class);
			if (group == null)
			{
				throw new WicketRuntimeException("Check component [" + getPath() +
					"] cannot find its parent CheckGroup");
			}
		}

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
			final String[] input = group.getInputAsArray();

			if (input != null)
			{
				for (int i = 0; i < input.length; i++)
				{
					if (uuid.equals(input[i]))
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
			CharSequence url = group.urlFor(IOnChangeListener.INTERFACE);

			Form<?> form = group.findParent(Form.class);
			if (form != null)
			{
				RequestContext rc = RequestContext.get();
				if (rc.isPortletRequest())
				{
					// restore url back to real wicket path as its going to be interpreted by the
					// form itself
					url = ((PortletRequestContext)rc).getLastEncodedPath();
				}
				tag.put("onclick", form.getJsForInterfaceUrl(url));
			}
			else
			{
				// TODO: following doesn't work with portlets, should be posted to a dynamic hidden
				// form
				// with an ActionURL or something
				// NOTE: do not encode the url as that would give invalid
				// JavaScript
				tag.put("onclick", "window.location.href='" + url +
					(url.toString().indexOf('?') > -1 ? "&amp;" : "?") + group.getInputName() +
					"=' + this.value;");
			}
		}

		if (!isActionAuthorized(ENABLE) || !isEnabled() || !group.isEnabled())
		{
			tag.put(ATTR_DISABLED, ATTR_DISABLED);
		}


	}
	public Check(String id, IModel<T> model, CheckGroup<T> group)
	{
		super(id, model);
		this.group = group;
	}
	protected final void onComponentTagBody(final MarkupStream markupStream,
		final ComponentTag openTag)
	{
		// Iterate through choices
		final List<? extends T> choices = getChoices();

		// Buffer to hold generated body
		final AppendingStringBuffer buffer = new AppendingStringBuffer(70 * (choices.size() + 1));

		// Value of this choice
		final String selected = getValue();

		// Loop through choices
		for (int index = 0; index < choices.size(); index++)
		{
			// Get next choice
			final T choice = choices.get(index);

			Object displayValue = getChoiceRenderer().getDisplayValue(choice);
			Class<?> objectClass = displayValue == null ? null : displayValue.getClass();
			// Get label for choice
			String label = "";
			if (objectClass != null && objectClass != String.class)
			{
				IConverter converter = getConverter(objectClass);
				label = converter.convertToString(displayValue, getLocale());
			}
			else if (displayValue != null)
			{
				label = displayValue.toString();
			}

			// If there is a display value for the choice, then we know that the
			// choice is automatic in some way. If label is /null/ then we know
			// that the choice is a manually created checkbox tag at some random
			// location in the page markup!
			if (label != null)
			{
				// Append option suffix
				buffer.append(getPrefix());

				String id = getChoiceRenderer().getIdValue(choice, index);
				final String idAttr = getInputName() + "_" + id;

				// Add checkbox element
				buffer.append("<input name=\"")
					.append(getInputName())
					.append("\"")
					.append(" type=\"checkbox\"")
					.append((isSelected(choice, index, selected) ? " checked=\"checked\"" : ""))
					.append((isEnabled() ? "" : " disabled=\"disabled\""))
					.append(" value=\"")
					.append(id)
					.append("\" id=\"")
					.append(idAttr)
					.append("\"/>");

				// Add label for checkbox
				String display = label;
				if (localizeDisplayValues())
				{
					display = getLocalizer().getString(label, this, label);
				}

				CharSequence escaped;
				if (getEscapeModelStrings())
				{
					escaped = Strings.escapeMarkup(display, false, true);
				}
				else
				{
					escaped = display;
				}

				buffer.append("<label for=\"");
				buffer.append(idAttr);
				buffer.append("\">").append(escaped).append("</label>");

				// Append option suffix
				buffer.append(getSuffix());
			}
		}

		// Replace body
		replaceComponentTagBody(markupStream, openTag, buffer);
	}
	public Radio(String id, IModel<T> model, RadioGroup<T> group)
	{
		super(id, model);
		this.group = group;
	}
	protected void onComponentTag(final ComponentTag tag)
	{
		// Default handling for component tag
		super.onComponentTag(tag);

		// must be attached to <input type="radio" .../> tag
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "radio");

		final String value = getValue();

		RadioGroup<?> group = this.group;
		if (group == null)
		{
			group = findParent(RadioGroup.class);
			if (group == null)
			{
				throw new WicketRuntimeException(
					"Radio component [" +
						getPath() +
						"] cannot find its parent RadioGroup. All Radio components must be a child of or below in the hierarchy of a RadioGroup component.");
			}
		}


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
		else if (Objects.equal(group.getDefaultModelObject(), getDefaultModelObject()))
		{
			tag.put("checked", "checked");
		}

		if (group.wantOnSelectionChangedNotifications())
		{
			// url that points to this components IOnChangeListener method
			CharSequence url = group.urlFor(IOnChangeListener.INTERFACE);

			Form<?> form = group.findParent(Form.class);
			if (form != null)
			{
				RequestContext rc = RequestContext.get();
				if (rc.isPortletRequest())
				{
					// restore url back to real wicket path as its going to be interpreted by the
					// form itself
					url = ((PortletRequestContext)rc).getLastEncodedPath();
				}
				tag.put("onclick", form.getJsForInterfaceUrl(url));
			}
			else
			{
				// TODO: following doesn't work with portlets, should be posted to a dynamic hidden
				// form
				// with an ActionURL or something
				// NOTE: do not encode the url as that would give invalid
				// JavaScript
				tag.put("onclick", "window.location.href='" + url +
					(url.toString().indexOf('?') > -1 ? "&amp;" : "?") + group.getInputName() +
					"=' + this.value;");
			}
		}


		if (!isEnabledInHierarchy())
		{
			tag.put(ATTR_DISABLED, ATTR_DISABLED);
		}
	}
