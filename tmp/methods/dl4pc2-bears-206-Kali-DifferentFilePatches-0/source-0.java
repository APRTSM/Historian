	public boolean runTest(String resource, BuilderConfig additionalBuilderConfiguration) throws IOException {
		String absResPath = this.resourcePath + resource + ".html";
		
		File override = new File(this.overridePath, resource + ".pdf");
		File primary = new File(this.primaryPath, resource + ".pdf");

		File testFile = override.exists() ? override : primary;
		
		byte[] htmlBytes = IOUtils
				.toByteArray(TestcaseRunner.class.getResourceAsStream(absResPath));
		String html = new String(htmlBytes, Charsets.UTF_8);

		StringBuilder sb = logToStringBuilder();
		byte[] actualPdfBytes = runRenderer(resourcePath, html, additionalBuilderConfiguration);
		
		if (actualPdfBytes == null) {
		    System.err.println("When running test (" + resource + "), rendering failed, writing log to failure file.");
			File output = new File(this.outputPath, resource + ".failure.txt");
			FileUtils.writeByteArrayToFile(output, sb.toString().getBytes(Charsets.UTF_8));
			return false;
		}

		if (!testFile.exists()) {
			System.err.println("When running test (" + resource + "), nothing to compare against as file (" + testFile.getCanonicalPath() + ") does not exist.");
			System.err.println("Writing generated PDF to file instead in output directory.");
			File output = new File(this.outputPath, resource + ".pdf");
			FileUtils.writeByteArrayToFile(output, actualPdfBytes);
			return false;
		}
		
		PDDocument docActual = PDDocument.load(actualPdfBytes);
		PDDocument docExpected = PDDocument.load(testFile);
		
		PDFRenderer rendActual = new PDFRenderer(docActual);
		PDFRenderer rendExpected = new PDFRenderer(docExpected);
		
		boolean problems = false;
		
		for (int i = 0; i < docActual.getNumberOfPages(); i++) {
			BufferedImage imgActual = i >= docActual.getNumberOfPages() ? ONE_PX_IMAGE : rendActual.renderImageWithDPI(i, 96f, ImageType.RGB);
			BufferedImage imgExpected = i >= docExpected.getNumberOfPages() ? ONE_PX_IMAGE : rendExpected.renderImageWithDPI(i, 96f, ImageType.RGB);

			if (imgActual.getWidth() != imgExpected.getWidth() ||
				imgActual.getHeight() != imgExpected.getHeight()) {
				System.err.println("When running test (" + resource + "), page sizes were different. Please check diff image in output directory.");
				problems = true;
			}
		
			BufferedImage diff = compareImages(imgActual, imgExpected);
			
			if (diff != null) {
				System.err.println("When running test (" + resource + "), differences were found. Please check diff images in output directory.");
				File output = new File(this.outputPath, resource + "---" + i + "---diff.png");
				ImageIO.write(diff, "png", output);
				
				output = new File(this.outputPath, resource + "---" + i + "---actual.png");
				ImageIO.write(imgActual, "png", output);
				
				output = new File(this.outputPath, resource + "---" + i + "---expected.png");
				ImageIO.write(imgExpected, "png", output);
				problems = true;
			}
		}
		
		docActual.close();
		docExpected.close();

		if (problems) {
			File outPdf = new File(this.outputPath, resource + ".pdf");
			FileUtils.writeByteArrayToFile(outPdf, actualPdfBytes);
			return false;
		}
		
		return true;
	}
