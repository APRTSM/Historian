	public final File writeToTempFile() throws IOException
	{
		File temp = File.createTempFile(Session.get().getId(),
			Files.cleanupFilename(item.getFieldName()));
		writeTo(temp);
		return temp;
	}
