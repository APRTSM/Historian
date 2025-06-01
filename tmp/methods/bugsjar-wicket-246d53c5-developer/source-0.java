	public final void error(final Serializable message)
	{
		getPage().getFeedbackMessages().error(this, message);
	}
	public final void info(final String message)
	{
		getPage().getFeedbackMessages().info(this, message);
	}
	public final boolean hasFeedbackMessage()
	{
		return getPage().getFeedbackMessages().hasMessageFor(this);
	}
	public final boolean hasErrorMessage()
	{
		return getPage().getFeedbackMessages().hasErrorMessageFor(this);
	}
	public final void debug(final String message)
	{
		getPage().getFeedbackMessages().debug(this, message);
	}
	public final void warn(final String message)
	{
		getPage().getFeedbackMessages().warn(this, message);
	}
	public final void fatal(final String message)
	{
		getPage().getFeedbackMessages().fatal(this, message);
	}
	public final FeedbackMessage getFeedbackMessage()
	{
		return getPage().getFeedbackMessages().messageForComponent(this);
	}
