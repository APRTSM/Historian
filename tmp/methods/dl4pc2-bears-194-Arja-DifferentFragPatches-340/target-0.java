	private void init2() {
		this.sanitizers = getConfig(this.sanitizerDescriptor);
		this.sinkAnnotations = getConfig(this.sinkAnnotationsDescriptor);
		this.searchAnnotationsInPackages = getConfig(this.annotationsPackagesDescriptor);
		this.searchAnnotationsInPackagesArray = this.searchAnnotationsInPackages.toArray(new String[0]);
		try {
		}
		catch (Exception e) {
			this.MAX_DATAFLOWS = 30;
		}
	}
