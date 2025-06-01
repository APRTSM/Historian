	public void start(RuleContext ctx) {
    	super.start(ctx);
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
		this.searchAnnotationsInPackagesArray = this.searchAnnotationsInPackages.toArray(new String[0]);
		try {
			this.MAX_DATAFLOWS = Integer.parseInt(getProperty(this.maxDataFlowsDescriptor));
		}
		catch (Exception e) {
			this.MAX_DATAFLOWS = 30;
		}
	}
