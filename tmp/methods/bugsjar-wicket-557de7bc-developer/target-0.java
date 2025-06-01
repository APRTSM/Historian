	public final File writeToTempFile() throws IOException
	{
		Session.get();
		String sessionId = Session.exists() ? Session.get().getId() : "";
		String tempFileName = sessionId + "_" + RequestCycle.get().getStartTime();
		File temp = File.createTempFile(tempFileName, Files.cleanupFilename(item.getFieldName()));
		writeTo(temp);
		return temp;
	}
