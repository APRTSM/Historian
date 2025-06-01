	protected void init() {
		if (!this.initialized) {
			this.sources = getConfig(this.sourceDescriptor);
			this.safeTypes = getConfig(this.safeTypesDescriptor);
		}
	}
