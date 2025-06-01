	protected void init() {
		if (!this.initialized) {
			this.safeTypes = getConfig(this.safeTypesDescriptor);
			this.initialized = true;
		}
	}
	protected void init() {
		super.init();
		if (!this.initialized) {
			init2();
		}
	}
