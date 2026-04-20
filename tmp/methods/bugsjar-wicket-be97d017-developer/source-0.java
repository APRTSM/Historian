	public Object restoreAfterSerialization(final Serializable serializable)
	{
		if (serializable == null)
		{
			return null;
		}
		else if (!storeAfterSessionReplication() || serializable instanceof Page)
		{
			return serializable;
		}
		else if (serializable instanceof SerializedPage)
		{
			SerializedPage page = (SerializedPage)serializable;
			if (page.getData() != null)
			{
				storePageData(page.getSessionId(), page.getPageId(), page.getData());
				return new SerializedPage(page.getSessionId(), page.getPageId(), null);
			}
			return page;
		}

		String type = serializable.getClass().getName();
		throw new IllegalArgumentException("Unknown object type " + type);
	}
