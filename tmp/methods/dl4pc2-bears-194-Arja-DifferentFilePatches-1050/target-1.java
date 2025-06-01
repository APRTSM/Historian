	public void start(RuleContext ctx) {
    	init();
    }
	protected void init() {
		if (!this.initialized) {
			init2();
			this.initialized = true;
		}
	}
