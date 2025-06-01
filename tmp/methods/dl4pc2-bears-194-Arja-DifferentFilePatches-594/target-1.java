	protected void init() {
		if (!this.initialized) {
			this.safeTypes = getConfig(this.safeTypesDescriptor);
			this.initialized = true;
		}
	}
	private void init2() {
		this.sinks = getConfig(this.sinkDescriptor);
		this.sanitizers = getConfig(this.sanitizerDescriptor);
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
