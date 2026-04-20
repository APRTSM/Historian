	public AjaxEventBehavior(String event)
	{
		Args.notEmpty(event, "event");

		onCheckEvent(event);

		this.event = event;
	}
	public String getEvent()
	{
		String events = event.toLowerCase();
		String[] splitEvents = events.split("\\s+");
		List<String> cleanedEvents = new ArrayList<>(splitEvents.length);
		for (String evt : splitEvents)
		{
			if (Strings.isEmpty(evt) == false)
			{
				if (evt.startsWith("on"))
				{
					String shortName = evt.substring(2);
					// TODO Wicket 8 Change this to throw an error in the milestone/RC versions and remove it for the final version
					LOGGER.warn("Since version 6.0.0 Wicket uses JavaScript event registration so there is no need of the leading " +
							"'on' in the event name '{}'. Please use just '{}'. Wicket 8.x won't manipulate the provided event " +
							"names so the leading 'on' may break your application."
							, evt, shortName);
					evt = shortName;
				}
				cleanedEvents.add(evt);
			}
		}

		return Strings.join(" ", cleanedEvents);
	}
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);

		String evt = getEvent();
		Checks.notEmpty(evt, "getEvent() should return non-empty event name(s)");
		attributes.setEventNames(evt);
	}
