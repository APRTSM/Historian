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
