	public void detachModels()
	{
		super.detachModels();

		if (fileNameModel != null)
		{
			fileNameModel.detach();
		}
	}
	public DownloadLink(String id, IModel<File> fileModel, IModel<String> fileNameModel)
	{
		super(id, fileModel);
		this.fileNameModel = wrap(fileNameModel);
	}
