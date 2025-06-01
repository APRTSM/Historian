	public String toString(final boolean detailed)
	{
		try
		{
			final StringBuilder buffer = new StringBuilder();
			buffer.append("[Component id = ").append(getId());

			if (detailed)
			{
				final Page page = findPage();
				if (page == null)
				{
					buffer.append(", page = <No Page>, path = ")
						.append(getPath())
						.append('.')
						.append(Classes.simpleName(getClass()));
				}
				else
				{
					buffer.append(", page = ")
						.append(getPage().getClass().getName())
						.append(", path = ")
						.append(getPath())
						.append('.')
						.append(Classes.simpleName(getClass()))
						.append(", isVisible = ")
						.append((determineVisibility()))
						.append(", isVersioned = ")
						.append(isVersioned());
				}

				if (markup != null)
				{
					buffer.append(", markup = ").append(new MarkupStream(getMarkup()).toString());
				}
			}

			buffer.append(']');

			return buffer.toString();
		}
		catch (Exception e)
		{
			log.warn("Error while building toString()", e);
			return String.format(
				"[Component id = %s <attributes are not available because exception %s was thrown during toString()>]",
				getId(), e.getClass().getName());
		}
	}
