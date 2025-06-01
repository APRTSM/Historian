	public void start(RuleContext ctx) {
    	super.start(ctx);
    }
	protected void init() {
		if (!this.initialized) {
			init2();
			this.initialized = true;
		}
	}
