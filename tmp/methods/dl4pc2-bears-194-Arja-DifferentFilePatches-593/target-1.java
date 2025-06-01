	protected void init() {
		if (!this.initialized) {
			this.sources = getConfig(this.sourceDescriptor);
			this.safeTypes = getConfig(this.safeTypesDescriptor);
		}
	}
	public void start(RuleContext ctx) {
    	init();
    }
	protected void init() {
		if (!this.initialized) {
			init2();
			this.initialized = true;
		}
	}
