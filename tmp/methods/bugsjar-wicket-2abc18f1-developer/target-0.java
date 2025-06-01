	public void afterRender(Component component)
	{
		Response response = component.getResponse();

		for (int i = 0; i < branches.length; i++)
		{
			if (i > 0)
			{
				response.write("</div>");
			}
			response.write("</div>");
		}
	}
