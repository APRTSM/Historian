	protected void init() {
		if (!this.initialized) {
			this.unsafeTypes = getConfig(this.unsafeTypesDescriptor);
			this.initialized = true;
		}
	}
	protected void init() {
		super.init();
		if (!this.initialized) {
			init2();
		}
	}
