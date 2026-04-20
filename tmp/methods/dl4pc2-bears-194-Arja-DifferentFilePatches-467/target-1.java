	protected void init() {
		if (!this.initialized) {
			this.sources = getConfig(this.sourceDescriptor);
			this.unsafeTypes = getConfig(this.unsafeTypesDescriptor);
			this.initialized = true;
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
