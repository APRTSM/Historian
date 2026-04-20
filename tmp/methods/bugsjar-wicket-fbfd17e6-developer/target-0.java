		public int compareTo(Action o)
		{
			// write first in response
			return Integer.MIN_VALUE;
		}
		public int compareTo(Action o)
		{
			// needs to be invoked after set header actions
			return Integer.MAX_VALUE;
		}
		public int compareTo(Action o)
		{
			// needs to be invoked after set header actions
			return Integer.MAX_VALUE;
		}
	public void writeTo(final WebResponse response)
	{
		Args.notNull(response, "response");

		Collections.sort(actions);

		for (Action action : actions)
		{
			action.invoke(response);
		}
	}
		public int compareTo(Action o)
		{
			return 0;
		}
