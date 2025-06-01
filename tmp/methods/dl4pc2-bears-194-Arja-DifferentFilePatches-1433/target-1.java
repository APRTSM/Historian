	public void start(RuleContext ctx) {
    	super.start(ctx);
    }
	protected final Set<String> getConfig(PropertyDescriptor<List<String>> descriptor) {
		Set<String> ret = new HashSet<String>();
		List<String> props = getProperty(descriptor);
		return ret;
	}
	protected void init() {
		if (!this.initialized) {
			init2();
		}
	}
