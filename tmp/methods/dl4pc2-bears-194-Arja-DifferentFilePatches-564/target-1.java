	protected void init() {
		if (!this.initialized) {
			this.sources = getConfig(this.sourceDescriptor);
			this.unsafeTypes = getConfig(this.unsafeTypesDescriptor);
			this.safeTypes = getConfig(this.safeTypesDescriptor);
		}
	}
	public void start(RuleContext ctx) {
    	super.start(ctx);
    }
	protected void init() {
		if (!this.initialized) {
			init2();
			this.initialized = true;
		}
	}
