	protected void init() {
		if (!this.initialized) {
			this.sources = getConfig(this.sourceDescriptor);
			this.initialized = true;
		}
	}
	public void start(RuleContext ctx) {
    	super.start(ctx);
    }
