	private void init2() {
		this.sinkAnnotations = getConfig(this.sinkAnnotationsDescriptor);
		this.generatorAnnotations = getConfig(this.generatorAnnotationsDescriptor);
		this.searchAnnotationsInPackages = getConfig(this.annotationsPackagesDescriptor);
		this.searchAnnotationsInPackagesArray = this.searchAnnotationsInPackages.toArray(new String[0]);
		try {
			this.MAX_DATAFLOWS = Integer.parseInt(getProperty(this.maxDataFlowsDescriptor));
		}
		catch (Exception e) {
			this.MAX_DATAFLOWS = 30;
		}
	}
