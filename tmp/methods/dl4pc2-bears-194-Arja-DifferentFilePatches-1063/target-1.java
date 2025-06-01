	protected void init() {
		if (!this.initialized) {
			this.sources = getConfig(this.sourceDescriptor);
			this.unsafeTypes = getConfig(this.unsafeTypesDescriptor);
		}
	}
	protected void init() {
		super.init();
		if (!this.initialized) {
			this.initialized = true;
		}
	}
