	public DownloadLink(String id, IModel<File> fileModel, IModel<String> fileNameModel)
	{
		super(id, fileModel);
		this.fileNameModel = fileNameModel;
	}
