	public final void error(final Serializable message)
	{
		Session.get().getFeedbackMessages().error(this, message);
	}
	public final void info(final String message)
	{
		Session.get().getFeedbackMessages().info(this, message);
	}
	public final boolean hasFeedbackMessage()
	{
		return Session.get().getFeedbackMessages().hasMessageFor(this);
	}
	public final boolean hasErrorMessage()
	{
		return Session.get().getFeedbackMessages().hasErrorMessageFor(this);
	}
	public final void debug(final String message)
	{
		Session.get().getFeedbackMessages().debug(this, message);
	}
	public final void warn(final String message)
	{
		Session.get().getFeedbackMessages().warn(this, message);
	}
	public final void fatal(final String message)
	{
		Session.get().getFeedbackMessages().fatal(this, message);
	}
	public final FeedbackMessage getFeedbackMessage()
	{
		return Session.get().getFeedbackMessages().messageForComponent(this);
	}
