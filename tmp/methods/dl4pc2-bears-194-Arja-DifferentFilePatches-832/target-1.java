	protected void init() {
		if (!this.initialized) {
			this.sources = getConfig(this.sourceDescriptor);
			this.initialized = true;
		}
	}
	public void start(RuleContext ctx) {
    	super.start(ctx);
    }
	protected void init() {
		super.init();
		if (!this.initialized) {
			this.initialized = true;
		}
	}
