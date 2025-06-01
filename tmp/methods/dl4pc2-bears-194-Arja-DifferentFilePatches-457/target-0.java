	protected void init() {
		if (!this.initialized) {
			this.safeTypes = getConfig(this.safeTypesDescriptor);
			this.initialized = true;
		}
	}
