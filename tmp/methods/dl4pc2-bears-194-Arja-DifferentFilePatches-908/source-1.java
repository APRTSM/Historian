	public void start(RuleContext ctx) {
    	init();
    	super.start(ctx);
    }
	protected void init() {
		super.init();
		if (!this.initialized) {
			init2();
			this.initialized = true;
		}
	}
