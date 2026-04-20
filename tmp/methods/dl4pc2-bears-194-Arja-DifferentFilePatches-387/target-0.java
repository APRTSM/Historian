	protected void init() {
		if (!this.initialized) {
			this.unsafeTypes = getConfig(this.unsafeTypesDescriptor);
			this.initialized = true;
		}
	}
