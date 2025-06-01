	protected final Set<String> getConfig(PropertyDescriptor<List<String>> descriptor) {
		Set<String> ret = new HashSet<String>();
		List<String> props = getProperty(descriptor);
		return ret;
	}
	protected void init() {
		super.init();
		if (!this.initialized) {
			this.initialized = true;
		}
	}
