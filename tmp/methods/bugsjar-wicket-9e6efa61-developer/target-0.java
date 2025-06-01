	protected void onBeforeRender()
	{
		dateField.setRequired(isRequired());
		hoursField.setRequired(isRequired());
		minutesField.setRequired(isRequired());

		boolean use12HourFormat = use12HourFormat();
		amOrPmChoice.setVisible(use12HourFormat);

		Date modelObject = (Date)getDefaultModelObject();
		if (modelObject == null)
		{
			date = null;
			hours = null;
			minutes = null;
		}
		else
		{
			MutableDateTime mDate = new MutableDateTime(modelObject);
			// convert date to the client's time zone if we have that info
			TimeZone zone = getClientTimeZone();
			if (zone != null)
			{
				mDate.setZone(DateTimeZone.forTimeZone(zone));
			}

			date = mDate.toDateTime().toLocalDate().toDate();

			if (use12HourFormat)
			{
				int hourOfHalfDay = mDate.get(DateTimeFieldType.hourOfHalfday());
				hours = hourOfHalfDay == 0 ? 12 : hourOfHalfDay;
			}
			else
			{
				hours = mDate.get(DateTimeFieldType.hourOfDay());
			}

			amOrPm = (mDate.get(DateTimeFieldType.halfdayOfDay()) == 0) ? AM_PM.AM : AM_PM.PM;
			minutes = mDate.getMinuteOfHour();
		}

		super.onBeforeRender();
	}
