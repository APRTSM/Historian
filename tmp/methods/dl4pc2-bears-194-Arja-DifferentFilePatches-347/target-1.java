	protected void init() {
		if (!this.initialized) {
			this.sources = getConfig(this.sourceDescriptor);
			this.unsafeTypes = getConfig(this.unsafeTypesDescriptor);
			this.initialized = true;
		}
	}
	protected void init() {
		super.init();
		if (!this.initialized) {
			this.initialized = true;
		}
	}
	private void init2() {
		this.sinks = getConfig(this.sinkDescriptor);
		this.sanitizers = getConfig(this.sanitizerDescriptor);
		this.sinkAnnotations = getConfig(this.sinkAnnotationsDescriptor);
		this.searchAnnotationsInPackages = getConfig(this.annotationsPackagesDescriptor);
		try {
			this.MAX_DATAFLOWS = Integer.parseInt(getProperty(this.maxDataFlowsDescriptor));
		}
		catch (Exception e) {
			this.MAX_DATAFLOWS = 30;
		}
	}
