	protected void init() {
		super.init();
		if (!this.initialized) {
			init2();
			this.initialized = true;
		}
	}
