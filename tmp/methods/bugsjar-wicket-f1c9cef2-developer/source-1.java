	public String toString(final boolean detailed)
	{
		try
		{
			if (detailed)
			{
				final Page page = findPage();
				if (page == null)
				{
					return new StringBuilder("[Component id = ").append(getId())
						.append(", page = <No Page>, path = ")
						.append(getPath())
						.append('.')
						.append(Classes.simpleName(getClass()))
						.append(']')
						.toString();
				}
				else
				{
					return new StringBuilder("[Component id = ").append(getId())
						.append(", page = ")
						.append(getPage().getClass().getName())
						.append(", path = ")
						.append(getPath())
						.append('.')
						.append(Classes.simpleName(getClass()))
						.append(", isVisible = ")
						.append((determineVisibility()))
						.append(", isVersioned = ")
						.append(isVersioned())
						.append(']')
						.toString();
				}
			}
			else
			{
				return "[Component id = " + getId() + ']';
			}
		}
		catch (Exception e)
		{
			log.warn("Error while building toString()", e);
			return String.format(
				"[Component id = %s <attributes are not available because exception %s was thrown during toString()>]",
				getId(), e.getClass().getName());
		}
	}
	public String toString(final boolean detailed)
	{
		final StringBuilder buffer = new StringBuilder();
		buffer.append("[").append(this.getClass().getSimpleName()).append(" ");
		buffer.append(super.toString(detailed));
		if (detailed)
		{
			if (getMarkup() != null)
			{
				buffer.append(", markup = ").append(new MarkupStream(getMarkup()).toString());
			}

			if (children_size() != 0)
			{
				buffer.append(", children = ");

				// Loop through child components
				final int size = children_size();
				for (int i = 0; i < size; i++)
				{
					// Get next child
					final Component child = children_get(i);
					if (i != 0)
					{
						buffer.append(' ');
					}
					buffer.append(child.toString());
				}
			}
		}
		buffer.append(']');
		return buffer.toString();
	}
