	public String getModelValue()
	{
		final T object = getModelObject();
		if (object != null)
		{
			int index = getChoices().indexOf(object);
			return getChoiceRenderer().getIdValue(object, index);
		}
		Object noSelectionValue = getNoSelectionValue();
		return noSelectionValue != null ? noSelectionValue.toString() : null;
	}
	protected CharSequence getDefaultChoice(final Object selected)
	{

		final Object noSelectionValue = getNoSelectionValue();

		// Is null a valid selection value?
		if (isNullValid())
		{
			// Null is valid, so look up the value for it
			String option = getLocalizer().getStringIgnoreSettings(getNullValidKey(), this, null,
				null);
			if (Strings.isEmpty(option))
			{
				option = getLocalizer().getString("nullValid", this, "");
			}

			// The <option> tag buffer
			final AppendingStringBuffer buffer = new AppendingStringBuffer(64 + option.length());

			// Add option tag
			buffer.append("\n<option");

			// If null is selected, indicate that
			if (selected == noSelectionValue)
			{
				buffer.append(" selected=\"selected\"");
			}

			// Add body of option tag
			buffer.append(" value=\"" + noSelectionValue + "\">")
				.append(option)
				.append("</option>");
			return buffer;
		}
		else
		{
			// Null is not valid. Is it selected anyway?
			if ((selected == null) || selected.equals(noSelectionValue) ||
				selected.equals(EMPTY_STRING))
			{
				// Force the user to pick a non-null value
				String option = getLocalizer().getStringIgnoreSettings(getNullKey(), this, null,
					null);

				if (Strings.isEmpty(option))
				{
					option = getLocalizer().getString("null", this, CHOOSE_ONE);
				}

				return "\n<option selected=\"selected\" value=\"" + noSelectionValue + "\">" +
					option + "</option>";
			}
		}
		return "";
	}
