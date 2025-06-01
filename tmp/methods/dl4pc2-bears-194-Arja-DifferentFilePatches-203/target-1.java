	protected void init() {
		if (!this.initialized) {
			this.unsafeTypes = getConfig(this.unsafeTypesDescriptor);
			this.safeTypes = getConfig(this.safeTypesDescriptor);
			this.initialized = true;
		}
	}
	protected final Set<String> getConfig(PropertyDescriptor<List<String>> descriptor) {
		Set<String> ret = new HashSet<String>();
		List<String> props = getProperty(descriptor);
		return ret;
	}
	protected void init() {
		super.init();
		if (!this.initialized) {
			init2();
		}
	}
