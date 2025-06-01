	protected void redirectTo(Url url, RequestCycle requestCycle)
	{
		bindSessionIfNeeded();

		WebResponse response = (WebResponse)requestCycle.getResponse();
		String relativeUrl = requestCycle.getUrlRenderer().renderUrl(url);
		response.sendRedirect(relativeUrl);
	}
	private void bindSessionIfNeeded()
	{
		// check for session feedback messages only
		FeedbackCollector collector = new FeedbackCollector();
		List<FeedbackMessage> feedbackMessages = collector.collect();
		if (feedbackMessages.size() > 0)
		{
			Session.get().bind();
		}
	}
