	protected void init() {
		if (!this.initialized) {
			this.sources = getConfig(this.sourceDescriptor);
			this.unsafeTypes = getConfig(this.unsafeTypesDescriptor);
			this.safeTypes = getConfig(this.safeTypesDescriptor);
		}
	}
	protected void init() {
		super.init();
		if (!this.initialized) {
			init2();
		}
	}
