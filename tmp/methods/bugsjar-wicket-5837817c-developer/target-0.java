	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) 
	{
		super.updateAjaxAttributes(attributes);

		Component component = getComponent();

		// textfiels and textareas will trigger this behavior with either 'inputchange' or 'change' events
		// all the other components will use just 'change'
		if (!(component instanceof TextField || component instanceof TextArea))
		{
			attributes.setEventNames(EVENT_CHANGE);
		}
	}
	public OnChangeAjaxBehavior()
	{
		super(EVENT_NAME);
	}
