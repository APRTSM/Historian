	protected void init() {
		if (!this.initialized) {
			this.sources = getConfig(this.sourceDescriptor);
			this.unsafeTypes = getConfig(this.unsafeTypesDescriptor);
			this.safeTypes = getConfig(this.safeTypesDescriptor);
			this.initialized = true;
		}
	}
	protected final Set<String> getConfig(PropertyDescriptor<List<String>> descriptor) {
		Set<String> ret = new HashSet<String>();
		List<String> props = getProperty(descriptor);
		for (String value: props) {
			if (!StringUtils.isBlank(value)) {
				ret.add(value.trim());
			}
		}
		
		return ret;
	}
	protected void init() {
		super.init();
		if (!this.initialized) {
			init2();
			this.initialized = true;
		}
	}
