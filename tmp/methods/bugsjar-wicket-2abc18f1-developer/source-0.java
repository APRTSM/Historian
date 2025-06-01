	public void afterRender(Component component)
	{
		Response response = component.getResponse();

		for (int i = 0; i < branches.length; i++)
		{
			response.write("</div>");
		}
	}
