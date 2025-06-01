	public static DateTextField forShortStyle(String id, IModel<Date> model)
	{
		return new DateTextField(id, model, new StyleDateConverter(true));
	}
	public static DateTextField forShortStyle(String id)
	{
		return forShortStyle(id, null);
	}
