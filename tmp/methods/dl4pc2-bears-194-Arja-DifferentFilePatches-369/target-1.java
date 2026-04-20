	protected void init() {
		if (!this.initialized) {
			this.unsafeTypes = getConfig(this.unsafeTypesDescriptor);
			this.safeTypes = getConfig(this.safeTypesDescriptor);
			this.initialized = true;
		}
	}
	public void start(RuleContext ctx) {
    	init();
    }
	protected void init() {
		super.init();
		if (!this.initialized) {
			this.initialized = true;
		}
	}
