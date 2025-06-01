	public void postProcess(final Markup markup)
	{
		// If there's still a non-simple tag left, it's an error
		while (stack.size() > 0)
		{
			final ComponentTag top = stack.peek();

			if (!requiresCloseTag(top.getName()))
			{
				stack.pop();
				top.setHasNoCloseTag(true);
			}
			else
			{
				throw new MarkupException(markup, "Tag does not have a close tag", null);
			}
		}
	}
