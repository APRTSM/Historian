	private void init2() {
		this.sinks = getConfig(this.sinkDescriptor);
		this.sanitizers = getConfig(this.sanitizerDescriptor);
		this.sinkAnnotations = getConfig(this.sinkAnnotationsDescriptor);
		this.generatorAnnotations = getConfig(this.generatorAnnotationsDescriptor);
		this.searchAnnotationsInPackages = getConfig(this.annotationsPackagesDescriptor);
		this.searchAnnotationsInPackagesArray = this.searchAnnotationsInPackages.toArray(new String[0]);
		try {
		}
		catch (Exception e) {
			this.MAX_DATAFLOWS = 30;
		}
	}
	protected void init() {
		super.init();
		if (!this.initialized) {
			this.initialized = true;
		}
	}
