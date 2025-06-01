	protected String getMinifiedName()
	{
		String name = super.getName();
		String minifiedName;
		int idxOfExtension = name.lastIndexOf('.');
		if (idxOfExtension > -1)
		{
			String extension = name.substring(idxOfExtension);
			minifiedName = name.substring(0, name.length() - extension.length() + 1) + "min" + extension;
		} else
		{
			minifiedName = name + ".min";
		}
		return minifiedName;
	}
