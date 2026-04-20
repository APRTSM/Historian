	public void licenseHeaders()
	{
		licenseHeaderHandlers = new ILicenseHeaderHandler[] {
				new JavaLicenseHeaderHandler(javaIgnore),
				new JavaScriptLicenseHeaderHandler(javaScriptIgnore),
				new XmlLicenseHeaderHandler(xmlIgnore),
				new PropertiesLicenseHeaderHandler(propertiesIgnore),
				new HtmlLicenseHeaderHandler(htmlIgnore),
				new VelocityLicenseHeaderHandler(velocityIgnore),
				new XmlPrologHeaderHandler(xmlPrologIgnore),
				new CssLicenseHeaderHandler(cssIgnore), };

		final Map<ILicenseHeaderHandler, List<File>> badFiles = new HashMap<>();

		for (final ILicenseHeaderHandler licenseHeaderHandler : licenseHeaderHandlers)
		{
			visitFiles(licenseHeaderHandler.getSuffixes(), licenseHeaderHandler.getIgnoreFiles(),
				new FileVisitor()
				{
					@Override
					public void visitFile(final File file)
					{
						if (licenseHeaderHandler.checkLicenseHeader(file) == false)
						{
							if ((addHeaders == false) ||
								(licenseHeaderHandler.addLicenseHeader(file) == false))
							{
								List<File> files = badFiles.get(licenseHeaderHandler);

								if (files == null)
								{
									files = new ArrayList<>();
									badFiles.put(licenseHeaderHandler, files);
								}

								files.add(file);
							}
						}
					}
				});
		}

		failIncorrectLicenceHeaders(badFiles);
	}
