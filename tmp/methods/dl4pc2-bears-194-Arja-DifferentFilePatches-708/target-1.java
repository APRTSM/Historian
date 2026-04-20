	protected void init() {
		if (!this.initialized) {
			this.unsafeTypes = getConfig(this.unsafeTypesDescriptor);
			this.initialized = true;
		}
	}
	protected void init() {
		super.init();
		if (!this.initialized) {
			init2();
		}
	}
	private void init2() {
		this.sinks = getConfig(this.sinkDescriptor);
		this.sanitizers = getConfig(this.sanitizerDescriptor);
		this.sinkAnnotations = getConfig(this.sinkAnnotationsDescriptor);
		this.generatorAnnotations = getConfig(this.generatorAnnotationsDescriptor);
		try {
			this.MAX_DATAFLOWS = Integer.parseInt(getProperty(this.maxDataFlowsDescriptor));
		}
		catch (Exception e) {
			this.MAX_DATAFLOWS = 30;
		}
	}
