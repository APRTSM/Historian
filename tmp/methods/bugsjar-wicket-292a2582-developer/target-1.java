	public static DateTextField forShortStyle(String id, IModel<Date> model,
		boolean applyTimeZoneDifference)
	{
		return new DateTextField(id, model, new StyleDateConverter(applyTimeZoneDifference));
	}
	public static DateTextField forShortStyle(String id)
	{
		return forShortStyle(id, null, true);
	}
	protected DateTextField newDateTextField(String id, PropertyModel<Date> dateFieldModel)
	{
		return DateTextField.forShortStyle(id, dateFieldModel, false);
	}
