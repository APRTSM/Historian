	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);

		attributes.setEventNames(event);
	}
	public final String getEvent()
	{
		return event;
	}
	public AjaxEventBehavior(String event)
	{
		Args.notEmpty(event, "event");

		onCheckEvent(event);

		event = event.toLowerCase();
		if (event.startsWith("on"))
		{
			String shortName = event.substring(2);
			// TODO Wicket 8 Change this to throw an error in the milestone/RC versions and remove it for the final version
			LOGGER.warn("Since version 6.0.0 Wicket uses JavaScript event registration so there is no need of the leading " +
					"'on' in the event name '{}'. Please use just '{}'. Wicket 8.x won't manipulate the provided event " +
					"names so the leading 'on' may break your application."
					, event, shortName);
			event = shortName;
		}

		this.event = event;
	}
