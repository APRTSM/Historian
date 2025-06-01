	protected String getMinifiedName()
	{
		String name = super.getName();
		String minifiedName;
		int idxOfExtension = name.lastIndexOf('.');
		if (idxOfExtension > -1)
		{
			String extension = name.substring(idxOfExtension);
            final String baseName = name.substring(0, name.length() - extension.length() + 1);
            if (!".min".equals(extension) && !baseName.endsWith(".min."))
            {
                minifiedName = baseName + "min" + extension;
            } else
            {
                minifiedName = name;
            }
		} else
		{
			minifiedName = name + ".min";
		}
		return minifiedName;
	}
