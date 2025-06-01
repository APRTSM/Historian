	private void init2() {
		this.sinks = getConfig(this.sinkDescriptor);
		this.sanitizers = getConfig(this.sanitizerDescriptor);
		this.generatorAnnotations = getConfig(this.generatorAnnotationsDescriptor);
		this.searchAnnotationsInPackages = getConfig(this.annotationsPackagesDescriptor);
		try {
			this.MAX_DATAFLOWS = Integer.parseInt(getProperty(this.maxDataFlowsDescriptor));
		}
		catch (Exception e) {
			this.MAX_DATAFLOWS = 30;
		}
	}
