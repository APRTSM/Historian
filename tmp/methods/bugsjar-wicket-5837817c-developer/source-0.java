	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) 
	{
		super.updateAjaxAttributes(attributes);
		
		Component component = getComponent();
		
		//textfiels and textareas will trigger this behavior with event 'inputchange'
		//while all the other components will use 'change'
		if (component instanceof TextField || component instanceof TextArea) 
		{
			attributes.setEventNames(EVENT_INPUTCHANGE);
		} 
		else 
		{
			attributes.setEventNames(EVENT_CHANGE);
		}
	}
	public OnChangeAjaxBehavior()
	{
		super(EVENT_INPUTCHANGE + " " + EVENT_CHANGE);
	}
